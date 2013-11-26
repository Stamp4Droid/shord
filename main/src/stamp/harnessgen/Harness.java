package stamp.harnessgen;

import soot.SootClass;
import soot.SootMethod;
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
import soot.jimple.IntConstant;
import soot.util.Chain;
import soot.util.JasminOutputStream;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class Harness
{
	private final SootClass sClass;
	private final Chain units;
	private final Chain<Local> locals;
	private int count = 0;

	public Harness(String className)
	{
		sClass = new SootClass(className, Modifier.PUBLIC);
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

	public void addComponent(Component comp, List<Layout> layouts)
	{		
		SootClass compClass = Scene.v().getSootClass(comp.name);
		
		//call the constructor of the comp
		Local c = init(compClass, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

		for(Layout layout : layouts) {
			//call the callbacks defined in XML
			for(String cbName : layout.callbacks){
				SootMethod cbMethod = compClass.getMethod(cbName, Arrays.asList(new Type[]{RefType.v("android.view.View")}));
				units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(c, cbMethod.makeRef(), NullConstant.v())));
			}

			//initialize the custom widgets used in this comp
			for(String widgetClassName : layout.customWidgets){
				SootClass wClass = Scene.v().getSootClass(widgetClassName);
				
				//one constructor
				List<Type> paramTypes1 = Arrays.asList(new Type[]{RefType.v("android.content.Context"), 
																  RefType.v("android.util.AttributeSet"), 
																  IntType.v()});
				
				if(wClass.declaresMethod("<init>", paramTypes1)){
					List<Value> args = Arrays.asList(new Value[]{NullConstant.v(),
																 NullConstant.v(),
																 IntConstant.v(0)});
					init(wClass, paramTypes1, args);
				}
				
				//another constructor
				List<Type> paramTypes2 = Arrays.asList(new Type[]{RefType.v("android.content.Context"), 
																  RefType.v("android.util.AttributeSet")});
				if(wClass.declaresMethod("<init>", paramTypes2)){
					List<Value> args = Arrays.asList(new Value[]{NullConstant.v(),
																 NullConstant.v()});
					init(wClass, paramTypes2, args);
				}
			}
		}
	}
	
	public void writeTo(File file) throws Exception
	{
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

	private Local init(SootClass klass, List<Type> paramTypes, List<Value> args)
	{
		//SootMethod init = new SootMethod("<init>", paramTypes, VoidType.v(), Modifier.PUBLIC);
		//klass.addMethod(init);
		SootMethod init = klass.getMethod("<init>", paramTypes);
		Local c = Jimple.v().newLocal("c"+count++, klass.getType());
		locals.add(c);
		units.add(Jimple.v().newAssignStmt(c, Jimple.v().newNewExpr(klass.getType())));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(c, init.makeRef(), args)));	
		return c;
	}
}