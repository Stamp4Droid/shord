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
import soot.jimple.DefinitionStmt;
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
	private SootClass stampInflaterClass;
	private CallGraph callGraph;

	private final String[] subsigs = {"android.view.View findViewById(int)",
									  "android.view.View inflate(int resource, android.view.ViewGroup root)",
									  "android.view.View inflate(org.xmlpull.v1.XmlPullParser parser, android.view.ViewGroup root)",
									  "android.view.View inflate(int resource, android.view.ViewGroup root, boolean attachToRoot)"
	};
	
	private final String[] classNames = {"android.app.Activity", "android.app.Dialog",
										 "android.view.View", "android.view.LayoutInflater", "android.view.Window"};

	public GuiFix()
	{
		String widgetsListFile = System.getProperty("stamp.widgets.file");
		BufferedReader reader;
		try{
			reader = new BufferedReader(new FileReader(widgetsListFile));
			stampInflaterClass = Scene.v().getSootClass(reader.readLine().trim());
			//System.out.println("stampInflatorClass: "+stampInflatorClass.getName());
			//for(SootMethod m : stampInflatorClass.getMethods())
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

		callGraph = Scene.v().getCallGraph();

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
		Chain<Unit> units = body.getUnits();
		Chain<Local> locals = body.getLocals();
		Iterator<Unit> uit = units.snapshotIterator();
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
			SootMethod target = callsInflate(stmt);
			if(target == null)
				continue;

			InvokeExpr ie = stmt.getInvokeExpr();
			Set<String> widgetMethNames = getWidgetMethNames(ie.getArg(0), stmt, body.getMethod());
			if(widgetMethNames.isEmpty())
				continue;

			Local inflaterLocal;
			Local base = (Local) ((InstanceInvokeExpr) ie).getBase();
			if(target.getDeclaringClass().equals(stampInflaterClass))
				inflaterLocal = base;
			else {
				inflaterLocal = Jimple.v().newLocal("stamp$inflater", stampInflaterClass.getType());
				locals.add(inflaterLocal);
				
				SootFieldRef inflaterFld = target.getDeclaringClass().getFieldByName("stamp_inflater").makeRef();
				Stmt loadStmt = Jimple.v().newAssignStmt(inflaterLocal, Jimple.v().newInstanceFieldRef(base, inflaterFld));
				units.insertBefore(loadStmt, stmt);
			}
			
			DefinitionStmt ds = (DefinitionStmt) stmt;
			Local leftOp = (Local) ds.getLeftOp();
			for(String subsig : widgetMethNames){
				SootMethodRef m = stampInflaterClass.getMethod("android.view.View "+subsig+"()").makeRef();
				Stmt invkStmt = Jimple.v().newAssignStmt(leftOp, Jimple.v().newVirtualInvokeExpr(inflaterLocal, m, Collections.EMPTY_LIST));
				units.insertBefore(invkStmt, stmt);
				System.out.println("replacing "+stmt + " by "+invkStmt+" in "+body.getMethod().getSignature());
			}
			units.remove(stmt);
		}
	}
	
	private SootMethod callsInflate(Stmt stmt)
	{
		if(!stmt.containsInvokeExpr() || !(stmt instanceof DefinitionStmt))
			return null;

		InvokeExpr ie = stmt.getInvokeExpr();
		String calleeSubsig = ie.getMethod().getSubSignature();
		boolean match = false;
		for(String ss : subsigs){
			if(ss.equals(calleeSubsig)){
				match = true;
				break;
			}
		}
		if(!match)
			return null;

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
			return null;

		String targetClassName = target.getDeclaringClass().getName();
		for(String cname : classNames){
			if(targetClassName.equals(cname))
				return target;
		}
		return null;
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