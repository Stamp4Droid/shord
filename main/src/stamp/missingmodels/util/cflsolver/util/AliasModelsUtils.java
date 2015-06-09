package stamp.missingmodels.util.cflsolver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.processor.AliasModelsProcessor;
import stamp.missingmodels.util.processor.AliasModelsProcessor.Variable;

public class AliasModelsUtils {
	public static class ProcessorUtils {
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
					if(stmtInvocation == null) {
						continue;
					}
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
	
	public static class SynthesisUtils {
		// Returns models along given flow path
		public static List<List<EdgeStruct>> getModels(List<EdgeStruct> path) {
			List<List<EdgeStruct>> models = new ArrayList<List<EdgeStruct>>();
			List<EdgeStruct> curModel = null;
			for(EdgeStruct edge : path) {
				if(edge.weight > 0 && (edge.symbol.equals("param") || edge.symbol.equals("return")) && curModel == null) {
					curModel = new ArrayList<EdgeStruct>();
					curModel.add(edge);
				} else if(edge.weight > 0 && (edge.symbol.equals("param") || edge.symbol.equals("return")) && curModel != null) {
					curModel.add(edge);
					models.add(curModel);
					curModel = null;
				} else if(edge.weight > 0) {
					curModel.add(edge);
				}
			}
			return models;
		}
		
		// Returns models for transitive closure graphBar by running AliasModelSynthesis.synthesize
		public static List<List<EdgeStruct>> getModelsFromGraph(Graph graphBar) {
			List<List<EdgeStruct>> models = new ArrayList<List<EdgeStruct>>();
			for(Edge flowEdge : graphBar.getEdges(new Filter<Edge>() { public boolean filter(Edge edge) { return edge.symbol.symbol.equals("FlowNew"); }})) {
				if(flowEdge.weight == (short)0) {
					continue;
				}
				// STEP 1: Get edge path
				List<Pair<EdgeStruct,Boolean>> path = new ArrayList<Pair<EdgeStruct,Boolean>>();
				for(Pair<Edge,Boolean> pair : flowEdge.getPath()) {
					path.add(new Pair<EdgeStruct,Boolean>(pair.getX().getStruct(), pair.getY()));
				}
				// STEP 2: Synthesize flow path
				List<EdgeStruct> modelPath = new ArrayList<EdgeStruct>();
				for(Pair<EdgeStruct,Boolean> pair : AliasModelsSynthesis.synthesize(path)) {
					modelPath.add(pair.getX());
				}
				// STEP 3: Synthesize models
				models.addAll(getModels(modelPath));
			}
			return models;
		}
		
		// Returns vertices (M###) with models bigger than size threshold (i.e., threshold >= model - start - end)
		public static Set<String> getMethodRejects(List<List<EdgeStruct>> models, int threshold) {
			Set<String> methodRejects = new HashSet<String>();
			for(List<EdgeStruct> model : models) {
				if(model.size() > threshold + 2) {
					if(model.get(0).sourceName.startsWith("M") == model.get(0).sinkName.startsWith("M")) {
						throw new RuntimeException("First edge in model should include variable: " + model.get(0));
					}
					methodRejects.add(model.get(0).sourceName.startsWith("M") ? model.get(0).sourceName : model.get(0).sinkName);
				}
			}
			return methodRejects;
		}
	}
}
