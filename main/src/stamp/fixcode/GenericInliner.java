package stamp.fixcode;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.util.Chain;

import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import chord.project.Chord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Chord(name="gen-inline")
public class GenericInliner extends JavaAnalysis {
	private static String INLINE_ANNOT_TYPE =
		"Ledu/stanford/stamp/annotation/Inline;";
	private static String EXC_CTOR_SIG =
		"<java.lang.RuntimeException: void <init>(java.lang.String)>";

	List<SootMethod> toInline = new ArrayList<SootMethod>();
	Map<SootMethod,Set<SootMethod>> inlineRevCG =
		new HashMap<SootMethod,Set<SootMethod>>();
	Set<SootMethod> inlineSites = new HashSet<SootMethod>();

	public void run() {
		// Get an initial call graph.
		Program.g().runCHA();
		// Collect all model methods marked with @Inline.
		// TODO: Only search over framework methods.
		Iterator<SootMethod> meths = Scene.v().getMethodNumberer().iterator();
		while (meths.hasNext()) {
			SootMethod m = meths.next();
			if (!m.hasTag("VisibilityAnnotationTag")) {
				continue;
			}
			VisibilityAnnotationTag tag = (VisibilityAnnotationTag)
				m.getTag("VisibilityAnnotationTag");
			for (AnnotationTag annot : tag.getAnnotations()) {
				if (annot.getType().equals(INLINE_ANNOT_TYPE)) {
					assert !Program.g().isStub(m) : "Can't inline stubs";
					toInline.add(m);
					break;
				}
			}
		}
		// Enumerate call graph dependencies among these methods.
		CallGraph cg = Scene.v().getCallGraph();
		for (SootMethod m : toInline) {
			Set<SootMethod> s = new HashSet<SootMethod>();
			Iterator<Edge> eIter = cg.edgesInto(m);
			while (eIter.hasNext()) {
				Edge e = eIter.next();
				if (!isRealCall(e)) {
					continue;
				}
				SootMethod c = (SootMethod) e.getSrc();
				if (toInline.contains(c)) {
					s.add(c);
				} else {
					inlineSites.add(c);
				}
			}
			inlineRevCG.put(m, s);
		}
		// Sort methods to inline such that any method m will get inlined
		// before any of its callers.
		TopoSorter<SootMethod> sorter =
			new TopoSorter<SootMethod>(toInline, inlineRevCG);
		boolean noCycles = sorter.sort();
		assert(noCycles);
		toInline = sorter.result();
		// Perform the inlining.
		for (SootMethod m : toInline) {
			inlineCallsTo(m);
			clearBody(m);
		}
		// Validate the bodies of the final inline sites, and remove
		// intermediate variables.
		for (SootMethod m : inlineSites) {
			Body body = m.getActiveBody();
			body.validate();
			CopyPropagator.v().transform(body);
			DeadAssignmentEliminator.v().transform(body);
			UnusedLocalEliminator.v().transform(body);
		}
		// CAUTION: We don't rebuild the call graph at this point, we assume
		// that the next phase will do that.
	}

	private boolean isRealCall(Edge edge) {
		SootMethod tgt = (SootMethod) edge.tgt();
		if (tgt.isAbstract()) {
			assert false : "call tgt: " + tgt + " is abstract";
		}
		return !tgt.isPhantom() && edge.isExplicit();
	}

	private void inlineCallsTo(SootMethod m) {
		// TODO: Assuming the inlining code doesn't modify the call
		// graph (otherwise our iterator might become invalid).
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> eIter = cg.edgesInto(m);
		while (eIter.hasNext()) {
			Edge e = eIter.next();
			if (!isRealCall(e)) {
				continue;
			}
			Stmt invk = e.srcStmt();
			SootMethod caller = (SootMethod) e.getSrc();
			// Ensure all calls to m have a single non-stub target.
			Iterator<Edge> cIter = cg.edgesOutOf(invk);
			while (cIter.hasNext()) {
				Edge c = cIter.next();
				if (c == e || !isRealCall(c) || Program.g().isStub(c.tgt())) {
					continue;
				}
				System.err.println
					("ERROR: Tried to inline at " + invk.toString() + " in " +
					 caller.toString() + " but it has multiple targets:");
				System.err.println(m);
				System.err.println(c.tgt());
				throw new RuntimeException();
			}
			// Perform the inlining (unsafely).
			System.out.println("Inlining " + m.toString() +
							   " into " + caller.toString() +
							   " at " + invk.toString());
			assert caller.getActiveBody().getUnits().contains(invk)
				: "The invocation statement is missing from the caller";
			SootInliner.inlineSite(m, invk, caller);
		}
	}

	// Need to update the call graph afterwards.
	private void clearBody(SootMethod m) {
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
}
