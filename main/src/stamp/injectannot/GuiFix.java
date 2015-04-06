package stamp.injectannot;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Body;
import soot.SootFieldRef;
import soot.Unit;
import soot.Local;
import soot.Value;
import soot.RefType;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.AssignStmt;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.Chain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import stamp.app.App;
import stamp.app.Layout;
import stamp.app.Widget;
import shord.project.analyses.JavaAnalysis;
import shord.program.Program;

import stamp.analyses.InterProcReachingDefAnalysis;

import chord.project.Chord;
/*
 * @author Saswat Anand
*/
@Chord(name="gui-fix")
public class GuiFix extends JavaAnalysis
{
	private Map<Integer,List<String>> viewIdToWidgetMeths = new HashMap();
	private InterProcReachingDefAnalysis iprda = null;
	private Body body;
	private SootClass stampViewClass;

	public GuiFix()
	{
		String widgetsListFile = System.getProperty("stamp.widgets.file");
		BufferedReader reader;
		try{
			reader = new BufferedReader(new FileReader(widgetsListFile));
			stampViewClass = Scene.v().getSootClass(reader.readLine().trim());
			//System.out.println("stampViewClass: "+stampViewClass.getName());
			//for(SootMethod m : stampViewClass.getMethods())
			//	System.out.println("+ "+m.getSignature());
			String line;
			while((line = reader.readLine()) != null){
				String[] tokens = line.split(",");
				int id = Integer.parseInt(tokens[0]);
				String widgetSubsig = tokens[1].split(" ")[1];
				if(id >= 0){
					List<String> ws = viewIdToWidgetMeths.get(id);
					if(ws == null){
						ws = new ArrayList();
						viewIdToWidgetMeths.put(id, ws);
					}
					ws.add(widgetSubsig);
				}
			}
			reader.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	public void run()
	{
		this.iprda = new InterProcReachingDefAnalysis();
		
		Scene.v().getSootClass("android.view.View").setSuperclass(stampViewClass);

		Program prog = Program.g();
		for(SootClass klass : prog.getClasses()){
			if(prog.isFrameworkClass(klass))
				continue;
			for(SootMethod method : klass.getMethods()){
				if(!method.isConcrete())
					continue;
				this.body = method.retrieveActiveBody();
				process();
			}
		}
	}
	
	private void process()
	{
		CallGraph callGraph = Scene.v().getCallGraph();
		Chain<Unit> units = body.getUnits();
		Chain<Local> locals = body.getLocals();
		Iterator<Unit> uit = units.snapshotIterator();
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
			if(!stmt.containsInvokeExpr() || !(stmt instanceof AssignStmt))
				continue;
			InvokeExpr ie = stmt.getInvokeExpr();
			SootMethod callee = ie.getMethod();
			if(!callee.getSubSignature().equals("android.view.View findViewById(int)"))
				continue;

			Iterator<Edge> edgeIt = callGraph.edgesOutOf(stmt);
			SootMethod target = null;
			while(edgeIt.hasNext()){
				if(target == null)
					target = (SootMethod) edgeIt.next().getTgt();
				else{
					//multple outgoing edges
					System.out.println("TODO: multiple outgoing edges from "+stmt);
					target = null;
					break;
				}
			}
			if(target == null)
				continue;

			Set<String> widgetMethNames = getWidgetMethNames(ie.getArg(0), stmt, body.getMethod());
			if(widgetMethNames.isEmpty())
				continue;

			Local viewLocal = null;
			String targetClassName = target.getDeclaringClass().getName();
			if(targetClassName.equals("android.app.Activity") ||
			   targetClassName.equals("android.app.Dialog") ||
			   targetClassName.equals("android.view.Window")){
				viewLocal = Jimple.v().newLocal("stamp$view", RefType.v("android.view.View"));
				locals.add(viewLocal);
				Local base = (Local) ((InstanceInvokeExpr) ie).getBase();
				SootFieldRef viewFld = target.getDeclaringClass().getFieldByName("view").makeRef();
				Stmt viewLoadStmt = Jimple.v().newAssignStmt(viewLocal, Jimple.v().newInstanceFieldRef(base, viewFld));
				units.insertBefore(viewLoadStmt, stmt);
			} else if(targetClassName.equals("android.view.View"))
				viewLocal = (Local) ((InstanceInvokeExpr) ie).getBase();
			else
				continue;

			Local leftOp = (Local) ((AssignStmt) stmt).getLeftOp();
			for(String subsig : widgetMethNames){
				SootMethodRef m = stampViewClass.getMethod("android.view.View "+subsig+"()").makeRef();
				Stmt invkStmt = Jimple.v().newAssignStmt(leftOp, Jimple.v().newVirtualInvokeExpr(viewLocal, m, Collections.EMPTY_LIST));
				units.insertBefore(invkStmt, stmt);
			}
			units.remove(stmt);
		}
	}

	private Set<String> getWidgetMethNames(Value arg, Stmt stmt, SootMethod method)
	{
		Set<String> widgetMeths = new HashSet();
		if(arg instanceof Constant){
			int viewId = ((IntConstant) arg).value;
			List<String> ws = viewIdToWidgetMeths.get(viewId);
			if(ws != null)
				widgetMeths.addAll(ws);
		} else {
			Set<Integer> viewIds = iprda.computeReachingDefsFor((Local) arg, stmt, method);
			for(Integer viewId : viewIds){
				List<String> ws = viewIdToWidgetMeths.get(viewId);
				if(ws != null)
					widgetMeths.addAll(ws);
			}
		}
		return widgetMeths;
	}

}