package shord.program;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import shord.analyses.ContainerTag;
import soot.CompilationDeathException;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.options.Options;
import soot.tagkit.Tag;
import soot.util.ArrayNumberer;
import soot.util.Chain;

public class Program {
	private static Program g;
	private SootMethod mainMethod;
	
	public static Program g() {
		if(g == null) {
			g = new Program();
			g.build();
		}
		return g;
	}
	
	private Program() {}

	private void build() {
        try {
			StringBuilder options = new StringBuilder();
			options.append("-full-resolver");
			options.append(" -allow-phantom-refs");
			options.append(" -src-prec apk");
			options.append(" -keep-line-number");
			options.append(" -p jb.tr use-older-type-assigner:true"); 
			//options.append(" -p cg implicit-entry:false");
			options.append(" -force-android-jar " + System.getProperty("user.dir"));
			options.append(" -soot-classpath " + System.getProperty("stamp.android.jar") + File.pathSeparator+System.getProperty("chord.class.path"));
			//options.append(" -f jimple");
			options.append(" -f none");
			options.append(" -d "+ System.getProperty("stamp.out.dir") + File.separator + "jimple");

			if(!Options.v().parse(options.toString().split(" "))) {
				throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED, "Option parse error");
			}
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
			
        } catch (CompilationDeathException e) {
            if(e.getStatus() != CompilationDeathException.COMPILATION_SUCCEEDED) {
                throw e;
            }
        }
	}

	public void buildCallGraph() {
		new CallGraphBuilder(DumbPointerAnalysis.v()).build(); // CHA
	}
	
	public void printAllClasses() {
		for(SootClass klass : Scene.v().getClasses()){
			PackManager.v().writeClass(klass);
		}
	}
	
	public void printClass(String className) {}
	
	public Chain<SootClass> getClasses() {
		return Scene.v().getClasses();
	}
	
	public Iterator<SootMethod> getMethods() {
		return Scene.v().getMethodNumberer().iterator();
	}

	public ArrayNumberer<Type> getTypes() {
		return (ArrayNumberer<Type>)Scene.v().getTypeNumberer();
	}

	public Scene scene() {
		return Scene.v();
	}
	
	public SootMethod getMainMethod() {
		return mainMethod;
	}

	public static SootMethod containerMethod(Stmt stmt) {
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
