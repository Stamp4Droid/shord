package stamp.harnessgen;

import java.io.*;
import java.util.*;

import stamp.app.App;
import stamp.app.Component;
import stamp.app.Layout;

import soot.Scene;
import soot.Value;
import soot.SootClass;
import soot.SootMethod;
import soot.Body;
import soot.Unit;
import soot.CompilationDeathException;
import soot.options.Options;
import soot.jimple.Stmt;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.jimple.AssignStmt;
import soot.Local;


/*
* @author Saswat Anand
*/
public class Main
{
	private static App app;

	public static void main(String[] args) throws Exception
	{
		String apktoolOutDir = System.getProperty("stamp.apktool.out.dir");
		String apkPath = System.getProperty("stamp.apk.path");
		String driverDirName = System.getProperty("stamp.driver.dir");
		String androidJar = System.getProperty("stamp.android.jar");
		String harnessListFile = System.getProperty("stamp.harnesslist.file");
		int numCompsPerHarness = Integer.parseInt(System.getProperty("stamp.max.harness.size"));

		app = App.readApp(apkPath, apktoolOutDir);
		List<Component> comps = app.components();
		
		initSoot(apkPath, androidJar, comps);
		
		File driverDir = new File(driverDirName, "stamp/harness");
		driverDir.mkdirs();
		
		PrintWriter writer = new PrintWriter(new FileWriter(new File(harnessListFile)));

		int numComps = comps.size();
		System.out.println("number of components = "+numComps);
		int harnessCount = 0;
		int i = 0;
		while(i < numComps){
			harnessCount++;
			String harnessClassName = "stamp.harness.Main"+harnessCount;
			writer.println(harnessClassName);
			Harness h = new Harness(harnessClassName, comps);
			for(int j = 0; j < numCompsPerHarness && i < numComps; j++, i++){
				Component comp = comps.get(i);
				findLayoutsFor(comp);
				h.addComponent(comp);
			}

			File harnessClassFile = new File(driverDirName, harnessClassName.replace('.','/').concat(".class"));
			h.writeTo(harnessClassFile);
		}
		writer.close();
	}

	private static void findLayoutsFor(Component comp)
	{
		if(comp.type != Component.Type.activity)
			return;

		SootClass activity = Scene.v().getSootClass(comp.name);
		
		for(SootMethod m : activity.getMethods()){
			if(!m.isConcrete())
				continue;
			Body body = m.retrieveActiveBody();
			SimpleLocalDefs sld = null;
			for(Unit u : body.getUnits()){
				Stmt s = (Stmt) u;
				if(!s.containsInvokeExpr())
					continue;
				InvokeExpr ie = s.getInvokeExpr();
				if(!ie.getMethod().getSignature().equals("<android.app.Activity: void setContentView(int)>"))
					continue;

				if(m.isStatic()){
					System.out.println("WARN: setContentView called in a static method "+m.getSignature());
					continue;
				} 

				Value rcvr = ((InstanceInvokeExpr) ie).getBase();
				Local thisLocal = body.getThisLocal();
				if(!rcvr.equals(thisLocal)){
					if(sld == null)
						sld = new SimpleLocalDefs(new ExceptionalUnitGraph(body));

					boolean warn = true;
					if(rcvr instanceof Local){
						warn = false;
						for(Unit def : sld.getDefsOfAt((Local) rcvr, u)){
							if(!(def instanceof AssignStmt) || !thisLocal.equals(((AssignStmt) def).getRightOp())){
								warn = true;
								break;
							}
						}
					}

					if(warn){
						System.out.println("WARN: rcvr of setContentView is not equal to ThisLocal of method "+m.getSignature());
						continue;
					}
				}

				Value arg = ie.getArg(0);
				if(arg instanceof Constant){
					int layoutId = ((IntConstant) arg).value;
					Layout layout = app.layoutWithId(layoutId);
					if(layout != null){
						comp.addLayout(layout);
						System.out.println("Layout: "+comp.name+" "+layout.fileName);
					}
					else
						System.out.println("WARN: Did not found layout for id = "+layoutId);
				} else {
					if(sld == null)
						sld = new SimpleLocalDefs(new ExceptionalUnitGraph(body));

					//System.out.println("WARN: Argument of setContentView is not constant");					
					for(Unit def : sld.getDefsOfAt((Local) arg, u)){
						if(!(def instanceof AssignStmt))
							continue;
						Value rhs = ((AssignStmt) def).getRightOp();
						if(!(rhs instanceof IntConstant))
							continue;
						int layoutId = ((IntConstant) rhs).value;
						Layout layout = app.layoutWithId(layoutId);
						if(layout != null){
							comp.addLayout(layout);
							System.out.println("Layout: "+comp.name+" "+layout.fileName);
						}
						else
							System.out.println("WARN: Did not found layout for id = "+layoutId);
					}
				}
			}
		}
	}

	private static void initSoot(String apkPath, String androidJar, List<Component> comps)
	{
        try {
			StringBuilder options = new StringBuilder();
			options.append("-allow-phantom-refs");
			options.append(" -src-prec apk");
			options.append(" -p jb.tr use-older-type-assigner:true"); 
			//options.append(" -p cg implicit-entry:false");
			options.append(" -force-android-jar "+System.getProperty("user.dir"));
			options.append(" -soot-classpath "+androidJar+File.pathSeparator+apkPath);
			//options.append(" -f jimple");
			options.append(" -f none");

			if (!Options.v().parse(options.toString().split(" ")))
				throw new CompilationDeathException(
													CompilationDeathException.COMPILATION_ABORTED,
													"Option parse error");
            Scene.v().loadBasicClasses();

			Scene.v().loadClassAndSupport("edu.stanford.stamp.harness.ApplicationDriver");
			for(Component c : comps){
				Scene.v().loadClassAndSupport(c.name);
			}

			Scene.v().loadDynamicClasses();
        } catch (Exception e) {
			throw new Error(e);
        }
	}
}
