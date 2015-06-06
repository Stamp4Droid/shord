package stamp.missingmodels.util.cflsolver.util;

import java.util.HashMap;
import java.util.Map;

import shord.analyses.AllocNode;
import shord.analyses.DomH;
import shord.analyses.SiteAllocNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.processor.AliasModelsProcessor;
import stamp.missingmodels.util.processor.AliasModelsProcessor.Variable;

public class AliasModelsUtils {
	private static class Statement {
		private final String method;
		private final int offset;
		private Statement(String method, int offset) {
			this.method = method;
			this.offset = offset;
		}
		@Override
		public int hashCode() { return 31*this.method.hashCode() + this.offset; }
		@Override
		public boolean equals(Object obj) {
			Statement other = (Statement)obj;
			return this.method.equals(other.method) && this.offset == other.offset;
		}
	}
	
	private static Map<Statement,Stmt> stmtMap;
	private static Map<Stmt,SootMethod> methodMap;
	private static Map<Stmt,Pair<SiteAllocNode,Integer>> allocNodeMap;
	private static void build() {
		stmtMap = new HashMap<Statement,Stmt>();
		methodMap = new HashMap<Stmt,SootMethod>();
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("ci_reachableM");
		relReachableM.load();
		for(Object obj : relReachableM.getAry1ValTuples()) {
			SootMethod method = (SootMethod)obj;
			if(!method.hasActiveBody()) {
				continue;
			}
			int offset = 0;
			for(Unit unit : method.getActiveBody().getUnits()) {
				if(methodMap.get((Stmt)unit) != null) {
					throw new RuntimeException("Error: duplicate statement: " + unit);
				}
				stmtMap.put(new Statement(method.toString(), offset), (Stmt)unit);
				methodMap.put((Stmt)unit, method);					
				offset++;
			}
		}
		relReachableM.close();
		
		// STEP 2: Build alloc node map
		allocNodeMap = new HashMap<Stmt,Pair<SiteAllocNode,Integer>>();
		DomH domH = (DomH)ClassicProject.g().getTrgt("H");
		for(int i=0; i<domH.size(); i++) {
			AllocNode node = domH.get(i);
			if(node instanceof SiteAllocNode) {
				allocNodeMap.put((Stmt)((SiteAllocNode)node).getUnit(), new Pair<SiteAllocNode,Integer>(((SiteAllocNode)node), i));
			}
		}
	}
	
	public static void printStatementMap() {
		if(stmtMap == null) {
			build();
		}
		for(Statement stmt : stmtMap.keySet()) {
			System.out.println("STATEMENT VAL: " + stmtMap.get(stmt));
			System.out.println("STATEMENT REP: " + stmt.method + ":" + stmt.offset);
			System.out.println();
		}
	}
	
	public static void printAllocationMap() {
		if(allocNodeMap == null) {
			build();
		}
		for(Stmt stmt : allocNodeMap.keySet()) {
			System.out.println("ALLOC NODE: " + allocNodeMap.get(stmt));
			System.out.println("ALLOC NODE STMT: " + allocNodeMap.get(stmt));
			System.out.println();
		}
	}
	
	public static Stmt getStmtFor(Variable variable) {
		if(stmtMap == null) {
			build();
		}
		return stmtMap.get(new Statement(variable.method, variable.offset));
	}
	
	public static SootMethod getMethodFor(Stmt stmt) {
		if(methodMap == null) {
			build();
		}
		return methodMap.get(stmt);
	}
	
	public static Pair<SiteAllocNode,Integer> getAllocNodeFor(Stmt stmt) {
		if(allocNodeMap == null) {
			build();
		}
		return allocNodeMap.get(stmt);
	}
	
	public static Pair<VarNode,Integer> getVarNodeFor(Local local, SootMethod method) {		
		VarNode varNode = LocalsToVarNodeMap.getLocalToVarNodeMap(method).get(local);
		return new Pair<VarNode,Integer>(varNode, LocalsToVarNodeMap.getIndexFor(varNode).getY());
	}
	
	public static MultivalueMap<Pair<VarNode,Integer>,Pair<SiteAllocNode,Integer>> getPtDynRetApp(AliasModelsProcessor processor) {
		MultivalueMap<Pair<VarNode,Integer>,Pair<SiteAllocNode,Integer>> ptDyn = new MultivalueMap<Pair<VarNode,Integer>,Pair<SiteAllocNode,Integer>>();
		for(Variable variable : processor.retsToAbstractObjects.keySet()) {
			for(int abstractObject : processor.retsToAbstractObjects.get(variable)) {
				if(!processor.appAbstractObjectsToAllocations.containsKey(abstractObject)) {
					continue;
				}
				Stmt stmtInvocation = getStmtFor(variable);
				if(!stmtInvocation.containsInvokeExpr()) {
					System.out.println("Expected invocation statement: " + stmtInvocation);
					continue;
				}
				if(!(stmtInvocation instanceof AssignStmt)) {
					continue;
				}
				Pair<VarNode,Integer> varNode = getVarNodeFor((Local)((AssignStmt)stmtInvocation).getLeftOp(), getMethodFor(stmtInvocation));
				Pair<SiteAllocNode,Integer> allocNode = getAllocNodeFor(getStmtFor(processor.appAbstractObjectsToAllocations.get(abstractObject)));
				ptDyn.add(varNode, allocNode);
			}
		}
		return ptDyn;
	}
}
