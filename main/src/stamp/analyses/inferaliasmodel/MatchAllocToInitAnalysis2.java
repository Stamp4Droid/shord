package stamp.analyses.inferaliasmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import chord.util.tuple.object.Pair;

public class MatchAllocToInitAnalysis2 {
	public static class MultivalueMap<K,V> extends HashMap<K,List<V>> {
		private static final long serialVersionUID = -6390444829513305915L;

		public void add(K k, V v) {
			ensure(k).add(v);
		}
		
		public List<V> ensure(K k) {
			List<V> vSet = super.get(k);
			if(vSet == null) {
				super.put(k, vSet = new ArrayList<V>());
			}
			return vSet;
		}

		@Override
		public List<V> get(Object k) {
			List<V> vSet = super.get(k);
			return Collections.unmodifiableList(vSet == null ? new ArrayList<V>() : vSet);
		}
	}
	
	private static Map<Body,MultivalueMap<Stmt,Stmt>> matchAllocToInits = new HashMap<Body,MultivalueMap<Stmt,Stmt>>();
	private static void initMatchAllocToInits(Body body) {
		MultivalueMap<Stmt,Stmt> newStmtToInvokeInitStmts = new MultivalueMap<Stmt,Stmt>();
		SimpleLocalDefs ld = new SimpleLocalDefs(new ExceptionalUnitGraph(body));
		for(Unit unit : body.getUnits()) {
			Stmt initInvokeStmt = (Stmt)unit;
			if(!initInvokeStmt.containsInvokeExpr()) {
				continue;
			}
			InvokeExpr ie = initInvokeStmt.getInvokeExpr();
			if(!ie.getMethod().getName().equals("<init>")) {
				continue;
			}
			Local rcvr = (Local)((InstanceInvokeExpr)ie).getBase();
			List<Pair<Local,Stmt>> workList = new LinkedList<Pair<Local,Stmt>>();
			workList.add(new Pair<Local,Stmt>(rcvr, initInvokeStmt));
			Set<Pair<Local,Stmt>> visited = new HashSet<Pair<Local,Stmt>>();
			while(!workList.isEmpty()) {
				Pair<Local,Stmt> p = workList.remove(0);
				if(visited.contains(p)) {
					continue;
				}
				visited.add(p);
				
				Local local = p.val0;
				Stmt useStmt = p.val1;
				
				for(Unit stmt : ld.getDefsOfAt(local, useStmt)) {
					if(stmt instanceof DefinitionStmt){
						DefinitionStmt ds = (DefinitionStmt) stmt;
						Value leftOp = ds.getLeftOp();
						Value rightOp = ds.getRightOp();
						assert local.equals(leftOp);
						if(rightOp instanceof NewExpr) {
							newStmtToInvokeInitStmts.add(ds, initInvokeStmt);
						} else if(rightOp instanceof Local) {
							workList.add(new Pair<Local,Stmt>((Local)rightOp, ds));
						}
					}				
				}
			}
		}
		matchAllocToInits.put(body, newStmtToInvokeInitStmts);
	}
	
	public static MultivalueMap<Stmt,Stmt> getMatchAllocToInit(Body body) {
		if(!matchAllocToInits.containsKey(body)) {
			initMatchAllocToInits(body);
		}
		return matchAllocToInits.get(body);
	}
	
}
