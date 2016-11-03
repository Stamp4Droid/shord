package shord.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import shord.analyses.ContainerTag;
import soot.CompilationDeathException;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.Tag;
import soot.toolkits.exceptions.UnitThrowAnalysis;
import soot.toolkits.scalar.LocalSplitter;
import soot.util.ArrayNumberer;
import soot.util.Chain;

/*
 * @author Saswat Anand
 */
public class Program {
	private static Program g;
	private SootMethod mainMethod;
	private List<SootMethod> defaultEntryPoints = new ArrayList<SootMethod>();
	
	public static Program g() {
		if(g == null) {
			g = new Program();
		}
		return g;
	}
	
	public void build() {
        try {
			StringBuilder options = new StringBuilder();
			options.append("-full-resolver");
			options.append(" -allow-phantom-refs");
			options.append(" -soot-classpath " + Config.v().chordModelPath + File.pathSeparator + Config.v().chordClassPath);
			options.append(" -f none");
			options.append(" -d "+ Config.v().outDirName + File.separator + "jimple");
			
			if (!Options.v().parse(options.toString().split(" "))) {
				throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED, "Option parse error");
			}
			
            Scene.v().loadBasicClasses();
			SootClass mainClass = Scene.v().loadClassAndSupport(Config.v().mainClassName);
			Scene.v().setMainClass(mainClass);
			mainMethod = mainClass.getMethod(Scene.v().getSubSigNumberer().findOrAdd("void main(java.lang.String[])"));
			
			//workaround soot bug
			defaultEntryPoints.add(mainMethod);
			if(mainClass.declaresMethodByName("<clinit>")) {
				defaultEntryPoints.add(mainClass.getMethodByName("<clinit>"));
			}
			Scene.v().loadDynamicClasses();
			
			LocalSplitter localSplitter = new LocalSplitter(UnitThrowAnalysis.v());
			
			for(SootClass klass : Scene.v().getClasses()) {
				for(SootMethod meth : klass.getMethods()){
					if(!meth.isConcrete()) {
						continue;
					}
					localSplitter.transform(meth.retrieveActiveBody());
				}
			}
        } catch (Exception e) {
			throw new Error(e);
        }
	}
	
	public void runSpark() {
		Scene.v().releaseCallGraph();
		Scene.v().releasePointsToAnalysis();
		Scene.v().releaseFastHierarchy();
		G.v().MethodPAG_methodToPag.clear();
		G.v().ClassHierarchy_classHierarchyMap.clear();
		
		Scene.v().setEntryPoints(defaultEntryPoints);
		
		//run spark
		Transform sparkTransform = PackManager.v().getTransform( "cg.spark" );
		String defaultOptions = sparkTransform.getDefaultOptions();
		StringBuilder options = new StringBuilder();
		options.append("enabled:true");
		options.append(" verbose:true");
		options.append(" simulate-natives:false");//our models should take care of this
		//options.append(" dump-answer:true");
		options.append(" "+defaultOptions);
		System.out.println("spark options: "+options.toString());
		sparkTransform.setDefaultOptions(options.toString());
		sparkTransform.apply();	
	}
	
	public Chain<SootClass> getClasses() {
		return Scene.v().getClasses();
	}
	
	public Iterator<SootMethod> getMethods() {
		return Scene.v().getMethodNumberer().iterator();
	}
	
	public ArrayNumberer<Type> getTypes() {
		return (ArrayNumberer<Type>) Scene.v().getTypeNumberer();
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
