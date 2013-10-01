package stamp.harnessgen;

import java.io.*;
import java.util.jar.*;
import java.util.*;

import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SootField;
import soot.Scene;
import soot.Modifier;
import soot.VoidType;
import soot.IntType;
import soot.ArrayType;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.JasminClass;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;
import soot.jimple.IntConstant;
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
		App app = new App(classPath, androidJar, outDir);

		File driverDir = new File(driverDirName, "edu/stanford/stamp/harness");
		driverDir.mkdirs();

		Main main = new Main(androidJar);
		main.addFields(app);
		main.generateCode(app);
		main.finish(new File(driverDir, "Main.class"));
	}

	private final SootClass sClass;
	private final Chain units;
	private final Chain<Local> locals;
	private int count = 0;

	private Main(String androidJar)
	{
		StringBuilder options = new StringBuilder();
		options.append("-allow-phantom-refs");
		options.append(" -dynamic-class edu.stanford.stamp.harness.ApplicationDriver");
		options.append(" -soot-classpath "+androidJar);
		if(!Options.v().parse(options.toString().split(" ")))
			throw new RuntimeException("Option parse error");
		Scene.v().loadNecessaryClasses();

		sClass = new SootClass("edu.stanford.stamp.harness.Main", Modifier.PUBLIC);
		sClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		Scene.v().addClass(sClass);
		
		SootMethod method = new SootMethod("main",
								Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),
								VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
		sClass.addMethod(method);

		method.setActiveBody(Jimple.v().newBody(method));
		units = method.getActiveBody().getUnits();
		locals = method.getActiveBody().getLocals();

		Local arg = Jimple.v().newLocal("l0", ArrayType.v(RefType.v("java.lang.String"), 1));
		locals.add(arg);
		units.add(Jimple.v().newIdentityStmt(arg, Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0)));
	}

	private void finish(File file) throws Exception
	{
		units.add(Jimple.v().newReturnVoidStmt());

		//write the class
        OutputStream streamOut = new JasminOutputStream(new FileOutputStream(file));
        PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
        JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
        jasminClass.print(writerOut);
        writerOut.flush();
        streamOut.close();
	}

	private SootClass getSootClass(String className)
	{
		//SootClass klass = new SootClass("android.view.View", Modifier.PUBLIC);
		//klass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		//Scene.v().addClass(klass);           
		//return klass;
		return Scene.v().getSootClass(className);
	}

	public static String componentFieldNameFor(String componentName)
	{
		return componentName.replace('.', '$');
	}

	private void addFields(App app)
	{
		SootMethod clinit = new SootMethod("<clinit>", Collections.EMPTY_LIST, VoidType.v(), Modifier.STATIC);
		sClass.addMethod(clinit);
		
		Type classType = RefType.v("java.lang.Class");
		Type stringType = RefType.v("java.lang.String");
		SootMethodRef forNameMethod = Scene.v().getMethod("<java.lang.Class: java.lang.Class forName(java.lang.String)>").makeRef();

		clinit.setActiveBody(Jimple.v().newBody(clinit));
		Chain<Unit> stmts = clinit.getActiveBody().getUnits();
		Chain<Local> ls = clinit.getActiveBody().getLocals();
		int n = 0;

		for(String comp : app.components.keySet()){
			//add fields corresponding to components
			SootField f = new SootField(componentFieldNameFor(comp), 
										classType, 
										Modifier.FINAL | Modifier.STATIC | Modifier.PUBLIC);
			sClass.addField(f);

			//initialize them in the clinit
			Local l = Jimple.v().newLocal("c"+n++, classType);
			ls.add(l);
			Local s = Jimple.v().newLocal("s"+n++, stringType);
			ls.add(s);
			stmts.add(Jimple.v().newAssignStmt(s, StringConstant.v(comp)));
			//can't support stringconst as an argument now.
			//stmts.add(Jimple.v().newAssignStmt(l, Jimple.v().newStaticInvokeExpr(forNameMethod, StringConstant.v(comp))));
			stmts.add(Jimple.v().newAssignStmt(l, Jimple.v().newStaticInvokeExpr(forNameMethod, s)));
			stmts.add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(f.makeRef()), l));
		}
		stmts.add(Jimple.v().newReturnVoidStmt());
	}

	private void generateCode(App app)
	{
		//new each component
		count = 0;
		for(Map.Entry<String,List<String>> entry : app.components.entrySet()){
			String comp = entry.getKey();
			System.out.println("Component: "+comp);

			Local c = init(comp, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

			//call callbacks declared in xml layout files
			List<String> callbacks = entry.getValue();
			for(String cbName : callbacks){
				SootMethod cb = new SootMethod(cbName, Arrays.asList(new Type[]{getSootClass("android.view.View").getType()}), VoidType.v(), Modifier.PUBLIC);
				getSootClass(comp).addMethod(cb);
				units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(c, cb.makeRef(), NullConstant.v())));
			}
		}
        
		//invoke callCallbacks method
		SootMethod callCallbacks = Scene.v().getMethod("<edu.stanford.stamp.harness.ApplicationDriver: void callCallbacks()>");
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(callCallbacks.makeRef())));

		//initialize custom guis
		for(Map.Entry<String,List<String>> entry : app.customGUIs.entrySet()){
			String guiClassName = entry.getKey();
			System.out.println("Custom GUI: "+guiClassName);
			List<String> inits = entry.getValue();
			
			for(String init : inits){
				//System.out.println(init);
				if(init.equals("<init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V")){
					List<Type> paramTypes = Arrays.asList(new Type[]{getSootClass("android.content.Context").getType(), 
																	 getSootClass("android.util.AttributeSet").getType(), 
																	 IntType.v()});
					List<Value> argTypes = Arrays.asList(new Value[]{NullConstant.v(),
																	 NullConstant.v(),
																	 IntConstant.v(0)});
					init(guiClassName, paramTypes, argTypes);
				} else if(init.equals("<init>(Landroid/content/Context;Landroid/util/AttributeSet;)V")){
					List<Type> paramTypes = Arrays.asList(new Type[]{getSootClass("android.content.Context").getType(), 
																	 getSootClass("android.util.AttributeSet").getType()});
					List<Value> argTypes = Arrays.asList(new Value[]{NullConstant.v(),
																	 NullConstant.v()});
					init(guiClassName, paramTypes, argTypes);
				}
			}
		}
	}

	private Local init(String className, List<Type> paramTypes, List<Value> args)
	{
		SootClass klass = Scene.v().getSootClass(className);
		SootMethod init = new SootMethod("<init>", paramTypes, VoidType.v(), Modifier.PUBLIC);
		klass.addMethod(init);
		Local c = Jimple.v().newLocal("c"+count++, klass.getType());
		locals.add(c);
		units.add(Jimple.v().newAssignStmt(c, Jimple.v().newNewExpr(klass.getType())));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(c, init.makeRef(), args)));	
		return c;
	}
}
