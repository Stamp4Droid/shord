package stamp.missingmodels.util.cflsolver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.analyses.AllocNode;
import shord.analyses.DomH;
import shord.analyses.DomI;
import shord.analyses.DomM;
import shord.analyses.LocalVarNode;
import shord.analyses.SiteAllocNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.RefLikeType;
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
import stamp.missingmodels.util.processor.AliasModelsTraceReader;
import stamp.missingmodels.util.processor.AliasModelsTraceReader.Variable;

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
		
		private static Map<SootMethod,Integer> methodMap; // DomM
		private static Set<SootMethod> stubs; // ProgramRel Stub
		private static Map<Statement,Stmt> stmtMap; // Statement (above) -> Jimple statement
		private static Map<Stmt,SootMethod> stmtToMethodMap; // Stmt -> containing SootMethod
		private static Map<Stmt,SiteAllocNode> stmtToAllocNodeMap; // (alloc) Stmt -> alloc node (DomH)
		private static Map<AllocNode,Integer> allocNodeMap; // DomH
		private static Map<Unit,Integer> invokeStmtMap; // DomI
		private static MultivalueMap<Unit,SootMethod> callgraph; // ProgramRel IM
		private static boolean built = false;
		private static void build() {
			if(built) { return; }
			built = true;
			
			// STEP 1: Build method relation
			DomM domM = (DomM)ClassicProject.g().getTrgt("M");
			methodMap = new HashMap<SootMethod,Integer>();
			for(int i=0; i<domM.size(); i++) {
				methodMap.put(domM.get(i), i);
			}
			ProgramRel relStub = (ProgramRel)ClassicProject.g().getTrgt("Stub");
			relStub.load();
			stubs = new HashSet<SootMethod>();
			for(Object obj : relStub.getAry1ValTuples()) {
				stubs.add((SootMethod)obj);
			}
			relStub.close();
			
			// STEP 2: Build relations of reachable methods and contained statements
			stmtMap = new HashMap<Statement,Stmt>();
			stmtToMethodMap = new HashMap<Stmt,SootMethod>();
			ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("ci_reachableM");
			relReachableM.load();
			for(Object obj : relReachableM.getAry1ValTuples()) {
				SootMethod method = (SootMethod)obj;
				if(!method.hasActiveBody()) {
					continue;
				}
				int offset = 0;
				for(Unit unit : method.getActiveBody().getUnits()) {
					if(stmtToMethodMap.get((Stmt)unit) != null) {
						throw new RuntimeException("Error: duplicate statement: " + unit);
					}
					stmtMap.put(new Statement(method.toString(), offset), (Stmt)unit);
					stmtToMethodMap.put((Stmt)unit, method);					
					offset++;
				}
			}
			relReachableM.close();
			
			// STEP 2: Build alloc node map
			stmtToAllocNodeMap = new HashMap<Stmt,SiteAllocNode>();
			allocNodeMap = new HashMap<AllocNode,Integer>();
			DomH domH = (DomH)ClassicProject.g().getTrgt("H");
			for(int i=0; i<domH.size(); i++) {
				AllocNode node = domH.get(i);
				allocNodeMap.put(node, i);
				if(node instanceof SiteAllocNode) {
					stmtToAllocNodeMap.put((Stmt)((SiteAllocNode)node).getUnit(), (SiteAllocNode)node);
				}
			}
			
			// STEP 3: Build callgraph
			invokeStmtMap = new HashMap<Unit,Integer>();
			DomI domI = (DomI)ClassicProject.g().getTrgt("I");
			for(int i=0; i<domI.size(); i++) {
				invokeStmtMap.put(domI.get(i), i);
			}
			ProgramRel relIM = (ProgramRel)ClassicProject.g().getTrgt("chaIM");
			relIM.load();
			callgraph = new MultivalueMap<Unit,SootMethod>();
			for(chord.util.tuple.object.Pair<Object,Object> pair : relIM.getAry2ValTuples()) {
				callgraph.add((Unit)pair.val0, (SootMethod)pair.val1);
			}
			relIM.close();
		}
		
		public static void printStatementMap() {
			build();
			for(Statement stmt : stmtMap.keySet()) {
				System.out.println("STATEMENT VAL: " + stmtMap.get(stmt));
				System.out.println("STATEMENT REP: " + stmt.method + ":" + stmt.offset);
				System.out.println();
			}
		}
		
		public static void printAllocationMap() {
			build();
			for(Stmt stmt : stmtToAllocNodeMap.keySet()) {
				System.out.println("ALLOC NODE: " + stmtToAllocNodeMap.get(stmt));
				System.out.println("ALLOC NODE STMT: " + stmtToAllocNodeMap.get(stmt));
				System.out.println();
			}
		}
		
		public static Stmt getStmtFor(Variable variable) {
			build();
			return stmtMap.get(new Statement(variable.method, variable.offset));
		}
		
		public static SootMethod getMethodFor(Stmt stmt) {
			build();
			return stmtToMethodMap.get(stmt);
		}
		
		public static SiteAllocNode getAllocNodeFor(Stmt stmt) {
			build();
			return stmtToAllocNodeMap.get(stmt);
		}
		
		public static LocalVarNode getVarNodeFor(Local local, SootMethod method) {
			Map<Local,LocalVarNode> localToVarNodeMap = LocalsToVarNodeMap.getLocalToVarNodeMap(method);
			return localToVarNodeMap == null ? null : localToVarNodeMap.get(local);
		}
		
		// String is "V" or "U"
		public static Pair<String,Integer> getIndexFor(VarNode varNode) {
			return LocalsToVarNodeMap.getIndexFor(varNode);
		}
		
		public static Iterable<SootMethod> getCallTargetFor(Stmt invokeStmt) {
			build();
			return callgraph.get(invokeStmt);
		}
		
		public static boolean isStub(SootMethod method) {
			build();
			return stubs.contains(method);
		}
		
		public static Stmt getInvokeStmtOrNullFor(Variable variable) {
			Stmt stmtInvocation = getStmtFor(variable);
			if(stmtInvocation == null) {
				System.out.println("Expected statement: " + variable);
				return null;
			}
			if(!stmtInvocation.containsInvokeExpr()) {
				System.out.println("Expected invocation statement: " + stmtInvocation);
				return null;
			}
			if(!(stmtInvocation instanceof AssignStmt)) {
				System.out.println("Expected assignment statement: " + stmtInvocation);
				return null;
			}
			return stmtInvocation;
		}
		
		public static MultivalueMap<VarNode,SiteAllocNode> getPtDynRetApp(AliasModelsTraceReader processor) {
			MultivalueMap<VarNode,SiteAllocNode> ptDyn = new MultivalueMap<VarNode,SiteAllocNode>();
			for(Variable variable : processor.retsToAbstractObjects.keySet()) {
				for(int abstractObject : processor.retsToAbstractObjects.get(variable)) {
					if(!processor.appAbstractObjectsToAllocations.containsKey(abstractObject)) {
						continue;
					}
					Stmt stmtInvocation = getInvokeStmtOrNullFor(variable);
					if(stmtInvocation == null) {
						continue;
					}
					VarNode varNode = getVarNodeFor((Local)((AssignStmt)stmtInvocation).getLeftOp(), getMethodFor(stmtInvocation));
					SiteAllocNode allocNode = getAllocNodeFor(getStmtFor(processor.appAbstractObjectsToAllocations.get(abstractObject)));
					if(varNode == null || allocNode == null) {
						continue;
					}
					ptDyn.add(varNode, allocNode);
				}
			}
			return ptDyn;
		}
		
		public static MultivalueMap<VarNode,SootMethod> getPhantomObjectDyn(AliasModelsTraceReader processor) {
			MultivalueMap<VarNode,SootMethod> phantomObjectDyn = new MultivalueMap<VarNode,SootMethod>();
			for(Variable variable : processor.retsToAbstractObjects.keySet()) {
				for(int abstractObject : processor.retsToAbstractObjects.get(variable)) {
					if(!processor.frameworkAbstractObjects.contains(abstractObject)) {
						continue;
					}
					Stmt stmtInvocation = getInvokeStmtOrNullFor(variable);
					if(stmtInvocation == null) {
						continue;
					}
					LocalVarNode varNode = getVarNodeFor((Local)((AssignStmt)stmtInvocation).getLeftOp(), getMethodFor(stmtInvocation));
					if(varNode == null) {
						continue;
					}
					if(!(varNode.local.getType() instanceof RefLikeType)) {
						continue;
					}
					for(SootMethod method : getCallTargetFor(stmtInvocation)) {
						if(isStub(method)) {
							phantomObjectDyn.add(varNode, method);
						}
					}
				}
			}
			return phantomObjectDyn;
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