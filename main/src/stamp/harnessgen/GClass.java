package stamp.harnessgen;

import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Scene;
import soot.Modifier;
import soot.VoidType;
import soot.Local;
import soot.IntType;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import soot.jimple.NullConstant;
import soot.util.Chain;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

import stamp.app.App;
import stamp.app.Layout;
import stamp.app.Widget;

/*
 * @author Saswat Anand
 */
public class GClass
{
	private final App app;
	private Chain units;
	private Chain<Local> locals;
	private int count = 0;

	public GClass(App app)
	{
		this.app = app;
	}

	public SootClass getFinalSootClass() throws IOException
	{
		SootClass gClass = new SootClass("stamp.harness.G", Modifier.PUBLIC);
		gClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		Scene.v().addClass(gClass);

		String widgetsListFile = System.getProperty("stamp.widgets.file");
		PrintWriter writer = new PrintWriter(new FileWriter(new File(widgetsListFile)));
		writer.println(gClass.getName());

		SootMethod clinit = new SootMethod("<clinit>", Collections.EMPTY_LIST, VoidType.v(), Modifier.STATIC);
		gClass.addMethod(clinit);
		clinit.setActiveBody(Jimple.v().newBody(clinit));
		units = clinit.getActiveBody().getUnits();
		locals = clinit.getActiveBody().getLocals();

		for(Layout layout : app.allLayouts()){
			//inflate widgets used in this comp
			for(Widget widget : layout.widgets){
				String widgetClassName = widget.getClassName();
				SootClass wClass = Scene.v().getSootClass(widgetClassName);
				
				//add a static field to hold the instance of the widget
				SootField f = new SootField(widgetFldNameFor(widget), 
											wClass.getType(), 
											Modifier.STATIC | Modifier.PUBLIC);
				
				if(!gClass.declaresField(f.getSubSignature())){
					gClass.addField(f);
					writer.println(widget.id + ","+f.getSubSignature());
				} else
					f = gClass.getField(f.getSubSignature());
				
				if(widget.isCustom()){
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
				} else {
					List<Type> paramTypes = Arrays.asList(new Type[]{RefType.v("android.content.Context")});
					List<Value> args = Arrays.asList(new Value[]{NullConstant.v()});
					Local l = init(wClass, paramTypes, args);
					units.add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(f.makeRef()), l));
				}
			}
		}
		writer.close();
		units.add(Jimple.v().newReturnVoidStmt());
		return gClass;
	}

	private Local init(SootClass klass, List<Type> paramTypes, List<Value> args)
	{
		SootMethod init = klass.getMethod("<init>", paramTypes);
		Local c = Jimple.v().newLocal("c"+count++, klass.getType());
		locals.add(c);
		units.add(Jimple.v().newAssignStmt(c, Jimple.v().newNewExpr(klass.getType())));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(c, init.makeRef(), args)));	
		return c;
	}

	public static String widgetFldNameFor(Widget w)
	{
		return w.idStr.replace(':','$').replace('/','$');
	}

}