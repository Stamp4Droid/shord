package shord.program;

import soot.*;
import soot.options.Options;
import soot.util.Chain;
import soot.util.ArrayNumberer;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import java.util.*;

public class Program
{
	private static Program g = new Program();
	private SootMethod mainMethod;

	public static Program g()
	{
		return g;
	}
	
	public void build()
	{
        try {
			StringBuilder options = new StringBuilder();
			options.append("-full-resolver");
			options.append(" -allow-phantom-refs");
			//options.append(" -p cg implicit-entry:false");
			options.append(" -soot-classpath "+System.getProperty("chord.class.path"));
			
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
			runCHA();

			Iterator<MethodOrMethodContext> mit = Scene.v().getReachableMethods().listener();
			while(mit.hasNext()){
				SootMethod m = (SootMethod) mit.next();
				System.out.println("reach: "+m.getSignature());
			}

			//for(SootClass klass : Scene.v().getClasses())
			//	System.out.println(klass.getName());
        } catch (CompilationDeathException e) {
            if(e.getStatus()!=CompilationDeathException.COMPILATION_SUCCEEDED)
                throw e;
            else
                return;
        }
	}

	void runCHA()
	{
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
	
	public ReachableMethods getMethods()
	{
		return Scene.v().getReachableMethods();
	}

	public ArrayNumberer<Type> getTypes()
	{
		return (ArrayNumberer<Type>) Scene.v().getTypeNumberer();
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