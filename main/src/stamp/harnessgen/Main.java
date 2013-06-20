package stamp.harnessgen;

import java.io.*;
import java.util.jar.*;
import java.util.*;

import soot.SootClass;
import soot.SootMethod;
import soot.Scene;
import soot.Modifier;
import soot.VoidType;
import soot.ArrayType;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.Local;
import soot.jimple.Jimple;
import soot.jimple.JasminClass;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.util.Chain;
import soot.util.JasminOutputStream;
import soot.options.Options;

/*
* @author Saswat Anand
*/
public class Main
{
	/*
	  args[0] - driver dir where generated driver class to be stored
	  args[1] - class path
	*/
	public static void main(String[] args) throws Exception
	{
		String driverDirName = args[0];
		String classPath = args[1];
		String androidJar = args[2];
		String outDir = args[3];
		App app;
		//if(args.length > 3){
		//	File androidManifestFile = new File(args[3]);
		//	app = new App(androidManifestFile, classPath, androidJar);
		//} else {
		app = new App(classPath, androidJar, outDir);
			//}

		
		File driverDir = new File(driverDirName, "edu/stanford/stamp/harness");
		driverDir.mkdirs();
		generateCode(new File(driverDir, "Main.class"), app, androidJar);
	}

	private static void generateCode(File file, App app, String androidJar) throws Exception
	{
		StringBuilder options = new StringBuilder();
		options.append("-allow-phantom-refs");
		options.append(" -dynamic-class edu.stanford.stamp.harness.ApplicationDriver");
		options.append(" -soot-classpath "+androidJar);
		if(!Options.v().parse(options.toString().split(" ")))
			throw new RuntimeException("Option parse error");
		Scene.v().loadNecessaryClasses();

		SootClass viewClass = new SootClass("android.view.View", Modifier.PUBLIC);
		viewClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		Scene.v().addClass(viewClass);           

		SootClass sClass = new SootClass("edu.stanford.stamp.harness.Main", Modifier.PUBLIC);
		sClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		Scene.v().addClass(sClass);
		SootMethod method = new SootMethod("main",
								Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),
								VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
		sClass.addMethod(method);

		JimpleBody body = Jimple.v().newBody(method);
		method.setActiveBody(body);
		Chain units = body.getUnits();
            
		Local arg = Jimple.v().newLocal("l0", ArrayType.v(RefType.v("java.lang.String"), 1));
		body.getLocals().add(arg);
		units.add(Jimple.v().newIdentityStmt(arg, Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0)));

		//new each component
		int count = 0;
		for(Map.Entry<String,List<String>> entry : app.components.entrySet()){
			String comp = entry.getKey();
			List<String> callbacks = entry.getValue();

			SootClass klass = Scene.v().getSootClass(comp);
			SootMethod init = new SootMethod("<init>", Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
			klass.addMethod(init);
			Local c = Jimple.v().newLocal("c"+count++, klass.getType());
			body.getLocals().add(c);
			units.add(Jimple.v().newAssignStmt(c, Jimple.v().newNewExpr(klass.getType())));
			units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(c, init.makeRef())));			
			System.out.println("Klass: "+klass);
			//call callbacks declared in xml layout files
			for(String cbName : callbacks){
				SootMethod cb = new SootMethod(cbName, Arrays.asList(new Type[]{viewClass.getType()}), VoidType.v(), Modifier.PUBLIC);
				klass.addMethod(cb);
				units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(c, cb.makeRef(), NullConstant.v())));
			}
		}
        
		//invoke callCallbacks method
		SootMethod callCallbacks = Scene.v().getMethod("<edu.stanford.stamp.harness.ApplicationDriver: void callCallbacks()>");
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(callCallbacks.makeRef())));
        
		units.add(Jimple.v().newReturnVoidStmt());

		//write the class
        OutputStream streamOut = new JasminOutputStream(new FileOutputStream(file));
        PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
        JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
        jasminClass.print(writerOut);
        writerOut.flush();
        streamOut.close();
	}

	/*
	private static void generateCode(File file, App app) throws Exception
	{
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		writer.println("package edu.stanford.stamp.harness;\n");
		writer.println("import edu.stanford.stamp.harness.ApplicationDriver;\n");
		writer.println("public class Main");
		writer.println("{");
		writer.println("  public static void main(String[] args)");
		writer.println("  {");
		for(String activity : app.components()){
			writer.println("    new "+activity+"();"); 
		}
		writer.println("\n");
		writer.println("    ApplicationDriver driver = ApplicationDriver.getInstance();");
		writer.println("    driver.callCallbacks();");
		writer.println("  }");
		writer.println("}");
		writer.close();
	}
	*/
}