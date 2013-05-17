package shord.program;

import soot.*;
import soot.options.Options;
import soot.util.Chain;
import soot.util.ArrayNumberer;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import java.util.*;

public class Program
{
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
			//options.append(" -p cg implicit-entry:false");
			options.append(" -force-android-jar "+System.getProperty("user.dir"));
			options.append(" -soot-classpath "+System.getProperty("chord.class.path"));
			options.append(" -f none");

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
			//for(SootClass klass : Scene.v().getClasses())
			//	System.out.println(klass.getName());
        } catch (CompilationDeathException e) {
            if(e.getStatus()!=CompilationDeathException.COMPILATION_SUCCEEDED)
                throw e;
            else
                return;
        }
	}

	public void buildCallGraph()
	{
		//run CHA
		CallGraphBuilder cg = new CallGraphBuilder(DumbPointerAnalysis.v());
		cg.build();
	}
	
	public void printAllClasses()
	{
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
	
    
}