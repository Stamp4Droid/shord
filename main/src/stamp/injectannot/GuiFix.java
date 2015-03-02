package stamp.injectannot;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Body;
import soot.SootField;
import soot.Unit;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.Constant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.AssignStmt;
import soot.util.Chain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

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
	private Map<Integer,List<String>> viewIdToWidgetFlds = new HashMap();
	private InterProcReachingDefAnalysis iprda = null;
	private Body body;
	private SootClass gClass;

	public GuiFix()
	{
		String widgetsListFile = System.getProperty("stamp.widgets.file");
		BufferedReader reader;
		try{
			reader = new BufferedReader(new FileReader(widgetsListFile));
			String gClassName = reader.readLine();
			this.gClass = Scene.v().getSootClass(gClassName);

			String line;
			while((line = reader.readLine()) != null){
				String[] tokens = line.split(",");
				int id = Integer.parseInt(tokens[0]);
				String widgetSubsig = tokens[1];
				if(id >= 0){
					List<String> ws = viewIdToWidgetFlds.get(id);
					if(ws == null){
						ws = new ArrayList();
						viewIdToWidgetFlds.put(id, ws);
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
		Iterator<Unit> uit = units.snapshotIterator();
		while(uit.hasNext()){
			Stmt stmt = (Stmt) uit.next();
			if(!stmt.containsInvokeExpr() || !(stmt instanceof AssignStmt))
				continue;
			InvokeExpr ie = stmt.getInvokeExpr();
			SootMethod callee = ie.getMethod();
			if(!callee.getSubSignature().equals("android.view.View findViewById(int)"))
				continue;

			Set<String> widgetFlds = getWidgetFlds(ie.getArg(0), stmt, body.getMethod());
			if(widgetFlds.isEmpty())
				continue;
			Local leftOp = (Local) ((AssignStmt) stmt).getLeftOp();
			for(String subsig : widgetFlds){
				SootField fld = gClass.getField(subsig);
				Stmt loadStmt = Jimple.v().newAssignStmt(leftOp, Jimple.v().newStaticFieldRef(fld.makeRef()));
				units.insertBefore(loadStmt, stmt);
			}
			units.remove(stmt);
		}
	}

	private Set<String> getWidgetFlds(Value arg, Stmt stmt, SootMethod method)
	{
		Set<String> widgetFlds = new HashSet();
		if(arg instanceof Constant){
			int viewId = ((IntConstant) arg).value;
			List<String> ws = viewIdToWidgetFlds.get(viewId);
			if(ws != null)
				widgetFlds.addAll(ws);
		} else {
			Set<Integer> viewIds = iprda.computeReachingDefsFor((Local) arg, stmt, method);
			for(Integer viewId : viewIds){
				List<String> ws = viewIdToWidgetFlds.get(viewId);
				if(ws != null)
					widgetFlds.addAll(ws);
			}
		}
		return widgetFlds;
	}

}