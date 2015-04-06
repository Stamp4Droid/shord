package stamp.harnessgen;

import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
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
import soot.jimple.ThisRef;
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
public class GenerateViewClass
{
	private final App app;
	private SootClass viewClass;

	public GenerateViewClass(App app)
	{
		this.app = app;
		viewClass = new SootClass("stamp.harness.View", Modifier.PUBLIC);
		viewClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		Scene.v().addClass(viewClass);
	}

	private void addInit()
	{
		SootMethod init = new SootMethod("<init>", Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
		viewClass.addMethod(init);
		init.setActiveBody(Jimple.v().newBody(init));
		Chain units = init.getActiveBody().getUnits();
		Local thisLocal = Jimple.v().newLocal("r0", viewClass.getType());
		init.getActiveBody().getLocals().add(thisLocal);
		units.add(Jimple.v().newIdentityStmt(thisLocal, new ThisRef(viewClass.getType())));
		SootMethodRef objectInit = Scene.v().getMethod("<java.lang.Object: void <init>()>").makeRef();
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(thisLocal, objectInit, Collections.EMPTY_LIST)));
		units.add(Jimple.v().newReturnVoidStmt());
	}

	public SootClass getFinalSootClass() throws IOException
	{
		String widgetsListFile = System.getProperty("stamp.widgets.file");
		PrintWriter writer = new PrintWriter(new FileWriter(new File(widgetsListFile)));
		writer.println(viewClass.getName());

		for(Layout layout : app.allLayouts()){
			//inflate widgets used in this comp
			for(Widget widget : layout.widgets){
				String widgetClassName = widget.getClassName();
				SootClass wClass = Scene.v().getSootClass(widgetClassName);
				
				SootMethod m = new SootMethod(widgetMethNameFor(widget), 
											  Collections.<Type> emptyList(),
											  RefType.v("android.view.View"), 
											  Modifier.PUBLIC | Modifier.FINAL);
				
				if(viewClass.declaresMethod(m.getSubSignature()))
					continue;

				viewClass.addMethod(m);
				writer.println(widget.id + ","+wClass.getType()+" "+m.getName());

				m.setActiveBody(Jimple.v().newBody(m));
				Chain units = m.getActiveBody().getUnits();
				Chain<Local> locals = m.getActiveBody().getLocals();

				Local thisLocal = Jimple.v().newLocal("r0", viewClass.getType());
				locals.add(thisLocal);
				units.add(Jimple.v().newIdentityStmt(thisLocal, new ThisRef(viewClass.getType())));
				
				List<Type> paramTypes = Arrays.asList(new Type[]{RefType.v("android.content.Context")});
				List<Value> args = Arrays.asList(new Value[]{NullConstant.v()});
				if(wClass.declaresMethod("<init>", paramTypes)){
					init(wClass, paramTypes, args, units, locals);
				} else {
					List<Type> paramTypes2 = Arrays.asList(new Type[]{RefType.v("android.content.Context"), 
																	  RefType.v("android.util.AttributeSet")});
					if(wClass.declaresMethod("<init>", paramTypes2)){
						args = Arrays.asList(new Value[]{NullConstant.v(),
														 NullConstant.v()});
						init(wClass, paramTypes2, args, units, locals);
					} else {
						List<Type> paramTypes1 = Arrays.asList(new Type[]{RefType.v("android.content.Context"), 
																		  RefType.v("android.util.AttributeSet"), 
																		  IntType.v()});
					
						if(wClass.declaresMethod("<init>", paramTypes1)){
							args = Arrays.asList(new Value[]{NullConstant.v(),
															 NullConstant.v(),
															 IntConstant.v(0)});
							init(wClass, paramTypes1, args, units, locals);
						} else {
							units.add(Jimple.v().newReturnStmt(NullConstant.v()));
						}
					}
				}
			}
		}
		writer.close();
		return viewClass;
	}

	private void init(SootClass klass, List<Type> paramTypes, List<Value> args, Chain units, Chain<Local> locals)
	{
		SootMethod init = klass.getMethod("<init>", paramTypes);
		Local c = Jimple.v().newLocal("v", klass.getType());
		locals.add(c);
		units.add(Jimple.v().newAssignStmt(c, Jimple.v().newNewExpr(klass.getType())));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(c, init.makeRef(), args)));	
		units.add(Jimple.v().newReturnStmt(c));
	}

	public static String widgetMethNameFor(Widget w)
	{
		return w.idStr.replace(':','$').replace('/','$');
	}

}
