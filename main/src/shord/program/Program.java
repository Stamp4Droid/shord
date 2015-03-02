package shord.program;

import soot.*;
import soot.options.Options;
import soot.util.Chain;
import soot.util.ArrayNumberer;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.tagkit.Tag;
import soot.toolkits.scalar.LocalSplitter;
import soot.dexpler.DalvikThrowAnalysis;
import soot.util.NumberedSet;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.*;
import java.io.*;

import shord.analyses.ContainerTag;

import stamp.app.App;

/*
 * @author Saswat Anand
 */
public class Program
{
	private static Program g;
	private SootMethod mainMethod;
	private NumberedSet frameworkClasses;
	private App app;

	public static Program g()
	{
		if(g == null){
			g = new Program();
		}
		return g;
	}

	private Program()
	{
	}

	public void build(List<String> harnesses, String widgetsClassName)
	{
        try {
			StringBuilder options = new StringBuilder();
			options.append("-full-resolver");
			options.append(" -allow-phantom-refs");
			options.append(" -src-prec apk");
			//options.append(" -p jb.tr use-older-type-assigner:true"); 
			//options.append(" -p cg implicit-entry:false");
			options.append(" -force-android-jar "+System.getProperty("user.dir"));
			options.append(" -soot-classpath "+System.getProperty("stamp.android.jar")+File.pathSeparator+System.getProperty("chord.class.path"));
			//options.append(" -f jimple");
			options.append(" -f none");
			options.append(" -d "+ System.getProperty("stamp.out.dir")+File.separator+"jimple");

			if (!Options.v().parse(options.toString().split(" ")))
				throw new CompilationDeathException(
													CompilationDeathException.COMPILATION_ABORTED,
													"Option parse error");
			//options.set_full_resolver(true);
			//options.set_allow_phantom_refs(true);
			
			//options.set_soot_classpath();

            Scene.v().loadBasicClasses();

			for(String h : harnesses){
				Scene.v().loadClassAndSupport(h);
			}

			Scene.v().loadClassAndSupport(widgetsClassName);

			//String mainClassName = System.getProperty("chord.main.class");
			//SootClass mainClass = Scene.v().loadClassAndSupport(mainClassName);
			//Scene.v().setMainClass(mainClass);

			//mainMethod = mainClass.getMethod(Scene.v().getSubSigNumberer().findOrAdd("void main(java.lang.String[])"));

			//Scene.v().setEntryPoints(Arrays.asList(new SootMethod[]{mainMethod}));


			Scene.v().loadDynamicClasses();

			LocalSplitter localSplitter = new LocalSplitter(DalvikThrowAnalysis.v());

			for(SootClass klass : Scene.v().getClasses()){
				for(SootMethod meth : klass.getMethods()){
					if(!meth.isConcrete())
						continue;
					localSplitter.transform(meth.retrieveActiveBody());
				}
			}

        } catch (Exception e) {
			throw new Error(e);
        }
	}

	public void setMainClass(String harness)
	{
		SootClass mainClass = Scene.v().getSootClass(harness);
		mainMethod = mainClass.getMethod(Scene.v().getSubSigNumberer().findOrAdd("void main(java.lang.String[])"));
		Scene.v().setMainClass(mainClass);

		List entryPoints = new ArrayList();
		entryPoints.add(mainMethod);

		//workaround soot bug
		if(mainClass.declaresMethodByName("<clinit>"))
			entryPoints.add(mainClass.getMethodByName("<clinit>"));

		Scene.v().setEntryPoints(entryPoints);
	}

	public void buildCallGraph()
	{
		//run CHA
		Scene.v().releaseCallGraph();
		CallGraphBuilder cg = new CallGraphBuilder(DumbPointerAnalysis.v());
		cg.build();
	}

	public void runSpark()
	{
		Scene.v().releaseCallGraph();
		Scene.v().releasePointsToAnalysis();
		G.v().MethodPAG_methodToPag.clear();
		//run spark
		Transform sparkTransform = PackManager.v().getTransform( "cg.spark" );
		String defaultOptions = sparkTransform.getDefaultOptions();
		StringBuilder options = new StringBuilder();
		options.append("enabled:true");
		options.append(" verbose:true");
		//options.append(" dump-answer:true");
		options.append(" "+defaultOptions);
		System.out.println("spark options: "+options.toString());
		sparkTransform.setDefaultOptions(options.toString());
		sparkTransform.apply();	
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
	
	public App app()
	{
		if(app == null){
			String apktoolOutDir = System.getProperty("stamp.apktool.out.dir");
			String apkPath = System.getProperty("stamp.apk.path");
			app = App.readApp(apkPath, apktoolOutDir);
			app.findLayouts();
			System.out.println(app.toString());
		}
		return app;
	}

	public NumberedSet frameworkClasses()
	{
		if(frameworkClasses != null)
			return frameworkClasses;

		Scene scene = Scene.v();
		frameworkClasses = new NumberedSet(scene.getClassNumberer());
		String androidJar = System.getProperty("stamp.android.jar");
		JarFile archive;
		try{
			archive = new JarFile(androidJar);
		}catch(IOException e){
			throw new Error(e);
		}
		for (Enumeration entries = archive.entries(); entries.hasMoreElements();) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryName = entry.getName();
			int extensionIndex = entryName.lastIndexOf('.');
			if (extensionIndex >= 0) {
				String entryExtension = entryName.substring(extensionIndex);
				if (".class".equals(entryExtension)) {
					entryName = entryName.substring(0, extensionIndex);
					entryName = entryName.replace('/', '.');
					if(scene.containsClass(entryName))
						frameworkClasses.add(scene.getSootClass(entryName));
				}
			}
		}
		return frameworkClasses;
	}
	
	public boolean isFrameworkClass(SootClass k)
	{
		return frameworkClasses().contains(k);
	}
}
