package stamp.analyses;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.Body;
import soot.Local;
import soot.jimple.Stmt;
import soot.jimple.IfStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.SwitchStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.Pair;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import shord.analyses.Ctxt;

import java.util.*;
import java.io.*;

public class WidgetControlDependencyAnalysis
{
	private Map<Stmt,Set<Integer>> dependentToWidgetIds = new HashMap();
	private Map<Ctxt,String> widgetToId = new HashMap();
	private Map<String,Integer> idToNumId = new HashMap();

	WidgetControlDependencyAnalysis(Map<Ctxt,String> widgetToId)
	{
		this.widgetToId = widgetToId;
		readWidgetIds();
	}

	public Set<String> computeWidgetIds(Set<Ctxt> widgets, SootMethod meth, Stmt stmt)
	{
		Set<Integer> widgetIds;
		if(dependentToWidgetIds.containsKey(stmt))
			widgetIds = dependentToWidgetIds.get(stmt);
		else {
			WidgetControlDependencyAnalysis analysis = new WidgetControlDependencyAnalysis(meth);
			widgetIds = analysis.perform(stmt);
			dependentToWidgetIds.put(stmt, widgetIds);
		}
		System.out.print("widgetIds: ");
		if(widgetIds != null){
			System.out.print("[");
			for(Integer i : widgetIds)
				System.out.print(i+", ");
			System.out.print("]");
		} else
			System.out.println(" null");
		System.out.println(" stmt: "+stmt+"@"+meth.getSignature());
			
		Set<String> ret = new HashSet();
		for(Ctxt obj : widgets){
			String id = widgetToId.get(obj);
			if(id == null)
				id = obj.toString();
			Integer numId = idToNumId.get(id);
			boolean drop = false;//widgetIds != null && numId != null && !widgetIds.contains(numId);
			if(!drop)
				ret.add(id);
			System.out.println("identifyWidgets: widget: "+obj+" "+id+" "+drop);
		}
		return ret;
	}

	private void readWidgetIds()
	{
		String widgetsListFile = System.getProperty("stamp.widgets.file");
		BufferedReader reader;
		try{
			reader = new BufferedReader(new FileReader(widgetsListFile));
			String line;
			reader.readLine();
			while((line = reader.readLine()) != null){
				String[] tokens = line.split(",");
				int id = Integer.parseInt(tokens[0]);
				String widgetSubsig = tokens[1];
				if(id >= 0){
					idToNumId.put(widgetSubsig, id);
				}
			}
			reader.close();
		}catch(IOException e){
			throw new Error(e);
		}
	}

	private SimpleLocalDefs ld;
	private SootMethod method;

	WidgetControlDependencyAnalysis(SootMethod method)
	{
		this.method = method;
	}

	Set<Integer> perform(Stmt stmt)
	{
		ControlDependenceGraph cdg = new ControlDependenceGraph(method);
		Map<Unit,Set<Pair<Unit,Unit>>> dependentToDependeesSetMap = cdg.dependentToDependeesSetMap();

		Set<Pair<Unit,Unit>> dependees = dependentToDependeesSetMap.get(stmt);
		if(dependees.size() == 0)
			return null;

		Set<Integer> result = new HashSet();		
		List<Stmt> workList = new LinkedList();
		Set<Stmt> visited = new HashSet();
		workList.add(stmt);
		while(!workList.isEmpty()){
			Stmt dependent = workList.remove(0);
			if(visited.contains(dependent))
				continue;
			visited.add(dependent);
			dependees = dependentToDependeesSetMap.get(dependent);
			for(Pair<Unit,Unit> pair : dependees){
				Stmt dependee = (Stmt) pair.getO1();
				Stmt targetStmt = (Stmt) pair.getO2();
				Set<Integer> rs = processDependee(dependee, targetStmt);
				if(rs == null)
					return null;
				else
					result.addAll(rs);
				workList.add(dependee);
			}
		}
		return result;
	}

	
	Set<Integer> processDependee(Stmt dependee, Stmt targetStmt)
	{
		if(dependee instanceof IfStmt){
			;//TODO
		} 
		else if(dependee instanceof SwitchStmt){
			Set<Integer> result = new HashSet();
			if(dependee instanceof LookupSwitchStmt){
				int count = 0;
				LookupSwitchStmt lookupSwitchStmt = (LookupSwitchStmt) dependee;
				for(Unit target : lookupSwitchStmt.getTargets()){
					if(target.equals(targetStmt)){
						result.add(lookupSwitchStmt.getLookupValue(count));
					}
					count++;
				}
				if(lookupSwitchStmt.getDefaultTarget().equals(targetStmt)){
				}			
			}
			else if(dependee instanceof TableSwitchStmt){
				TableSwitchStmt tableSwitchStmt = (TableSwitchStmt) dependee;
				int index = tableSwitchStmt.getLowIndex();
				for(Unit target : tableSwitchStmt.getTargets()){
					if(target.equals(targetStmt)){
						result.add(index);
					}
					index++;
				}
				if(tableSwitchStmt.getDefaultTarget().equals(targetStmt)){
				}
			}
			else assert false;

			boolean flag = false;
			Body body = method.retrieveActiveBody();
			if(ld == null)
				ld = new SimpleLocalDefs(new ExceptionalUnitGraph(body));
			Local param0 = body.getParameterLocal(0);
			Local key = (Local) ((SwitchStmt) dependee).getKey();
			List<Unit> defs = ld.getDefsOfAt(key, dependee);
			if(defs.size() == 1){
				Stmt defStmt = (Stmt) defs.get(0);
				if(defStmt instanceof DefinitionStmt){
					DefinitionStmt ds = (DefinitionStmt) defStmt;
					Value leftOp = ds.getLeftOp();
					Value rightOp = ds.getRightOp();
					
					assert key.equals(leftOp);
					if(rightOp instanceof VirtualInvokeExpr){
						VirtualInvokeExpr vie = (VirtualInvokeExpr) rightOp;
						if(vie.getBase().equals(param0) && vie.getMethod().getSubSignature().equals("int getId()"))
							flag = true;
					}
				}				
			}
			
			if(flag)
				return result;
			else
				return null;
		}
		else assert false;

		return null;
	}
}