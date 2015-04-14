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
import soot.jimple.NullConstant;
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
	private Chain<Unit> units;
	private Chain<Local> locals;
	private SootClass stampInflaterClass;
	private CallGraph callGraph;

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
				this.units = body.getUnits();
				this.locals = body.getLocals();
				System.out.println("gui-fixing "+method.getSignature());
				process();
			}
		}
	}
	
	private void process()
	{
		Iterator<Unit> uit = units.snapshotIterator();
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
			
			if(processInflate(stmt))
				continue;

			if(processFindViewById(stmt))
				continue;
		}
	}

	private boolean processFindViewById(Stmt stmt)
	{
		if(!stmt.containsInvokeExpr() || !(stmt instanceof DefinitionStmt))
			return false;

		InvokeExpr ie = stmt.getInvokeExpr();
		String calleeSubsig = ie.getMethod().getSubSignature();
		if(!calleeSubsig.equals("android.view.View findViewById(int)"))
		   return false;
		   
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
			return false;

		String targetClassName = target.getDeclaringClass().getName();
		if(!targetClassName.equals("android.app.Activity") &&
		   !targetClassName.equals("android.app.Dialog") &&
		   !targetClassName.equals("android.view.View"))
			return false; 

		Set<String> widgetMethNames = getWidgetMethNames(ie.getArg(0), stmt, body.getMethod());
		if(widgetMethNames.isEmpty())
			return false;                                

		Local inflaterLocal;
		inflaterLocal = Jimple.v().newLocal("stamp$inflater", stampInflaterClass.getType());
		locals.add(inflaterLocal);
				
		SootFieldRef inflaterFld = target.getDeclaringClass().getFieldByName("stamp_inflater").makeRef();
		Stmt loadStmt = Jimple.v().newAssignStmt(inflaterLocal, Jimple.v().newInstanceFieldRef(((InstanceInvokeExpr) ie).getBase(), inflaterFld));
		units.insertBefore(loadStmt, stmt);

		DefinitionStmt ds = (DefinitionStmt) stmt;
		Local leftOp = (Local) ds.getLeftOp();
		for(String subsig : widgetMethNames){
			SootMethodRef m = stampInflaterClass.getMethod("android.view.View "+subsig+"()").makeRef();
			Stmt invkStmt = Jimple.v().newAssignStmt(leftOp, Jimple.v().newVirtualInvokeExpr(inflaterLocal, m, Collections.EMPTY_LIST));
			units.insertBefore(invkStmt, stmt);
			System.out.println("replacing "+stmt + " by "+invkStmt+" in "+body.getMethod().getSignature());
		}
		units.remove(stmt);
		return true;
	}
	
	private boolean processInflate(Stmt stmt)
	{
		if(!stmt.containsInvokeExpr())
			return false;

		InvokeExpr ie = stmt.getInvokeExpr();
		String calleeSubsig = ie.getMethod().getSubSignature();
		boolean inflate = 
			calleeSubsig.equals("android.view.View inflate(int,android.view.ViewGroup)") ||
			calleeSubsig.equals("android.view.View inflate(int,android.view.ViewGroup,boolean)");
		boolean setContentView = 
			calleeSubsig.equals("void setContentView(int)");

		if(!inflate && !setContentView)
			return false;

		Iterator<Edge> edgeIt = callGraph.edgesOutOf(stmt);
		if(!edgeIt.hasNext()) System.out.println("no outgoing call edge from "+stmt);
		SootMethod target = null;
		while(edgeIt.hasNext()){
			if(target == null){
				target = (SootMethod) edgeIt.next().getTgt();
				//if(setContentView) System.out.println("setContentView "+target.getSignature());
			} else{
				//multple outgoing edges
				System.out.println("TODO: multiple outgoing edges from "+stmt);
				target = null;
				break;
			}
		}
		if(target == null)
			return false;
		System.out.println("target: "+target.getSignature()+" at "+stmt);
		String targetClassName = target.getDeclaringClass().getName();
		SootFieldRef inflaterFld = null;
		Local base = null;
		Local contextLocal = null;
		Stmt loadCtxtStmt = null;
		if(setContentView){
			if(targetClassName.equals("android.app.Activity") ||
			   targetClassName.equals("android.app.Dialog")){
			
				inflaterFld = target.getDeclaringClass().getFieldByName("stamp_inflater").makeRef();
				base = (Local) ((InstanceInvokeExpr) ie).getBase();
				contextLocal = base;
			}
		} else if(inflate){
			if(targetClassName.equals("android.view.LayoutInflater")){
				inflaterFld = Scene.v().getSootClass("android.view.View").getFieldByName("stamp_inflater").makeRef();
				if(stmt instanceof DefinitionStmt)
					base = (Local) ((DefinitionStmt) stmt).getLeftOp();
				else {
					Value viewGroupArg = ie.getArg(1);
					if(viewGroupArg instanceof Local)
						base = (Local) viewGroupArg; 
				}
				if(base != null){
					contextLocal = Jimple.v().newLocal("stamp$context", RefType.v("android.content.Context"));
					locals.add(contextLocal);
					SootFieldRef contextFld = Scene.v().getSootClass("android.view.LayoutInflater").getFieldByName("context").makeRef();
					loadCtxtStmt = Jimple.v().newAssignStmt(contextLocal, Jimple.v().newInstanceFieldRef((Local) ((InstanceInvokeExpr) ie).getBase(), contextFld));
					
				}
			}
		} else
			assert false;
		
		if(base == null)
			return false;

		for(Integer layoutId : reachingDefsFor(ie.getArg(0), stmt, body.getMethod())){
			SootClass inflaterSubclass = Scene.v().getSootClass("stamp.harness.LayoutInflater$"+layoutId);
			
			Local inflaterLocal = Jimple.v().newLocal("stamp$inflater$"+layoutId, inflaterSubclass.getType());
			locals.add(inflaterLocal);
			
			Stmt newStmt = Jimple.v().newAssignStmt(inflaterLocal, Jimple.v().newNewExpr((RefType) inflaterSubclass.getType()));
			
			SootMethodRef initRef = inflaterSubclass.getMethod("void <init>(android.content.Context)").makeRef();
			Stmt initCallStmt = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(inflaterLocal, initRef, contextLocal));
			
			Stmt storeStmt = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(base, inflaterFld), inflaterLocal);

			units.insertAfter(storeStmt, stmt);			
			units.insertAfter(initCallStmt, stmt);
			units.insertAfter(newStmt, stmt);
		}
		if(loadCtxtStmt != null)
			units.insertAfter(loadCtxtStmt, stmt);
			  
		return true;
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

	private Set<Integer> reachingDefsFor(Value arg, Stmt stmt, SootMethod method)
	{
		if(arg instanceof Constant){
			return Collections.<Integer> singleton(((IntConstant) arg).value);
		} else {
			return iprda.computeReachingDefsFor((Local) arg, stmt, method);
		}
	}
}