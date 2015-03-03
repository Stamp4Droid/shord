package shord.program;

import soot.*;
import soot.options.Options;
import soot.util.Chain;
import soot.util.ArrayNumberer;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.*;
import java.io.*;

import shord.analyses.ContainerTag;

public class Program
{
	private static String INLINE_ANNOT_TYPE =
		"Ledu/stanford/stamp/annotation/Inline;";
	private static String EXC_CTOR_SIG =
		"<java.lang.RuntimeException: void <init>(java.lang.String)>";

	private static Program g;
	private SootMethod mainMethod;

	public static Program g()
	{
		if(g == null){
			g = new Program();
			g.build();
		}
		return g;
	}

	private Program(){}

	private void build()
	{
        try {
			StringBuilder options = new StringBuilder();
			options.append("-full-resolver");
			options.append(" -allow-phantom-refs");
			options.append(" -src-prec apk");
			options.append(" -p jb.tr use-older-type-assigner:true"); 
			//options.append(" -p cg implicit-entry:false");
			options.append(" -force-android-jar "+System.getProperty("user.dir"));
			options.append(" -soot-classpath "+System.getProperty("stamp.android.jar")+File.pathSeparator+System.getProperty("chord.class.path"));
			options.append(" -f jimple");
			options.append(" -d "+ System.getProperty("stamp.out.dir")+File.separator+"jimple");

			if (!Options.v().parse(options.toString().split(" ")))
				throw new CompilationDeathException(
													CompilationDeathException.COMPILATION_ABORTED,
													"Option parse error");
			//options.set_full_resolver(true);
			//options.set_allow_phantom_refs(true);
			
			//options.set_soot_classpath();

            Scene.v().loadBasicClasses();

			String mainClassName = System.getProperty("chord.main.class");
			SootClass mainClass = Scene.v().loadClassAndSupport(mainClassName);
			Scene.v().setMainClass(mainClass);

			mainMethod = mainClass.getMethod(Scene.v().getSubSigNumberer().findOrAdd("void main(java.lang.String[])"));

			Scene.v().setEntryPoints(Arrays.asList(new SootMethod[]{mainMethod}));
			Scene.v().loadDynamicClasses();

			// Get an initial call graph.
			buildCallGraph();
			// Inline model methods marked with @Inline.
			// TODO: Only search over framework methods.
			Iterator<SootMethod> meths = getMethods();
			while (meths.hasNext()) {
				SootMethod m = meths.next();
				// Search method annotations for @Inline.
				if (!m.hasTag("VisibilityAnnotationTag")) {
					continue;
				}
				boolean must_inline = false;
				VisibilityAnnotationTag tag = (VisibilityAnnotationTag)
					m.getTag("VisibilityAnnotationTag");
				for (AnnotationTag annot : tag.getAnnotations()) {
					if (annot.getType().equals(INLINE_ANNOT_TYPE)) {
						must_inline = true;
						break;
					}
				}
				if (!must_inline) {
					continue;
				}
				inline_calls_to(m);
				clear_body(m);
			}
			// Update the call graph after inlining.
			buildCallGraph();
        } catch (CompilationDeathException e) {
            if(e.getStatus()!=CompilationDeathException.COMPILATION_SUCCEEDED)
                throw e;
            else
                return;
        }
	}

	public static boolean isRealCall(Edge edge) {
		SootMethod tgt = (SootMethod) edge.tgt();
		if (tgt.isAbstract()) {
			assert false : "call tgt: " + tgt + " is abstract";
		}
		return !tgt.isPhantom() && edge.isExplicit();
	}

	private void inline_calls_to(SootMethod m) {
		// TODO: Assuming the inlining code doesn't modify the call
		// graph (otherwise our iterator might become invalid).
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> iter = cg.edgesInto(m);
		while (iter.hasNext()) {
			Edge e = iter.next();
			if (!isRealCall(e)) {
				continue;
			}
			Stmt invk = e.srcStmt();
			SootMethod caller = (SootMethod) e.getSrc();
			// Ensure all calls to m have a single target.
			Iterator<Edge> calls = cg.edgesOutOf(invk);
			int numCalls = 0;
			while (calls.hasNext()) {
				if (isRealCall(calls.next())) {
					numCalls++;
				}
			}
			assert(numCalls == 1);
			// Perform the inlining (unsafely).
			System.out.println("Inlining " + m.toString() +
							   " into " + caller.toString() +
							   " at " + invk.toString());
			SiteInliner.inlineSite(m, invk, caller);
			// Validate the new body and remove intermediate variables.
			Body body = caller.getActiveBody();
			body.validate();
			PackManager.v().getPack("jop").apply(body);
		}
	}

	// Need to update the call graph afterwards.
	private void clear_body(SootMethod m) {
		System.out.println("Clearing " + m.toString());
		JimpleBody empty_body = Jimple.v().newBody(m);
		m.setActiveBody(empty_body);
		Chain<Unit> units = empty_body.getUnits();
		RefType exc_type = RefType.v("java.lang.RuntimeException");
		// emit: java.lang.RuntimeException $r0;
		Local r0 = Jimple.v().newLocal("$r0", exc_type);
		empty_body.getLocals().add(r0);
		// emit: $r0 = new java.lang.RuntimeException;
		NewExpr new_expr = Jimple.v().newNewExpr(exc_type);
		units.add(Jimple.v().newAssignStmt(r0, new_expr));
		// emit: $r0.RuntimeException::init("Cleared")
		SootMethod ctor = Scene.v().getMethod(EXC_CTOR_SIG);
		StringConstant msg = StringConstant.v("Cleared");
		units.add(Jimple.v().newInvokeStmt
				  (Jimple.v().newVirtualInvokeExpr(r0, ctor.makeRef(), msg)));
		// emit: throw $r0;
		units.add(Jimple.v().newThrowStmt(r0));
		empty_body.validate();
	}

	public void buildCallGraph()
	{
		//run CHA
		CallGraphBuilder cg = new CallGraphBuilder(DumbPointerAnalysis.v());
		cg.build();
	}
	
	public void printAllClasses()
	{
		for(SootClass klass : Scene.v().getClasses()){
			PackManager.v().writeClass(klass);
			//System.out.println(klass.getName());
		}
	}
	
	public void printClass(String className)
	{
	}
	
	public Chain<SootClass> getClasses()
	{
		return Scene.v().getClasses();
	}
	
	public Iterator<SootMethod> getMethods()
	{
		return Scene.v().getMethodNumberer().iterator();
	}

	public ArrayNumberer<Type> getTypes()
	{
		return (ArrayNumberer<Type>) Scene.v().getTypeNumberer();
	}

	public Scene scene()
	{
		return Scene.v();
	}
	
	/*
	public ArrayNumberer<SootField> getFields()
	{
		return (ArrayNumberer<SootField>) Scene.v().getFieldNumberer();
		}*/

	public SootMethod getMainMethod()
	{
		return mainMethod;
	}

	public static SootMethod containerMethod(Stmt stmt)
	{
		for(Tag tag : stmt.getTags()){
			if(tag instanceof ContainerTag)
				return ((ContainerTag) tag).method;
		}
		return null;
	}

	public static String unitToString(Unit u) {
		SootMethod m = (u instanceof Stmt) ? containerMethod((Stmt) u) : null;
		return (m == null) ? u.toString() : u + "@" + m;
	}
}
