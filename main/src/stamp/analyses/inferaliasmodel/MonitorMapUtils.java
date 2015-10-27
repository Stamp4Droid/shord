package stamp.analyses.inferaliasmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.analyses.AllocNode;
import shord.analyses.CastVarNode;
import shord.analyses.DomH;
import shord.analyses.DomM;
import shord.analyses.DomV;
import shord.analyses.LocalVarNode;
import shord.analyses.ParamVarNode;
import shord.analyses.RetVarNode;
import shord.analyses.SiteAllocNode;
import shord.analyses.StringConstNode;
import shord.analyses.StringConstantVarNode;
import shord.analyses.StubAllocNode;
import shord.analyses.ThisVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.callgraph.Edge;
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.Monitor;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;

public class MonitorMapUtils {
	public static class VariableMap {
		private final MultivalueMap<Local,Stmt> localToDef = new MultivalueMap<Local,Stmt>();
		private final MultivalueMap<Local,Stmt> localToUse = new MultivalueMap<Local,Stmt>();
		private final Map<Stmt,SootMethod> stmtToMethod = new HashMap<Stmt,SootMethod>();
		private final Map<Local,SootMethod> localToMethod = new HashMap<Local,SootMethod>();
		private final Map<CastExpr,Stmt> castToStmt = new HashMap<CastExpr,Stmt>();
		private final MultivalueMap<Pair<SootMethod,String>,Stmt> strToStmt = new MultivalueMap<Pair<SootMethod,String>,Stmt>();
		private final MultivalueMap<SootMethod,Stmt> callers = new MultivalueMap<SootMethod,Stmt>();
		
		public Set<Stmt> getDefs(Local local) {
			return this.localToDef.get(local);
		}
		
		public Set<Stmt> getUses(Local local) {
			return this.localToUse.get(local);
		}
		
		public SootMethod getMethod(Stmt stmt) {
			return this.stmtToMethod.get(stmt);
		}
		
		public SootMethod getMethod(Local local) {
			return this.localToMethod.get(local);
		}
		
		public Stmt getStmt(CastExpr expr) {
			return this.castToStmt.get(expr);
		}
		
		public Set<Stmt> getStmts(SootMethod method, String str) {
			return this.strToStmt.get(new Pair<SootMethod,String>(method, str));
		}
		
		public Set<Stmt> getCallers(SootMethod method) {
			return this.callers.get(method);
		}
		
		private VariableMap() {
			Iterator<SootMethod> methods = ((DomM)ClassicProject.g().getTrgt("M")).iterator();
			while(methods.hasNext()) {
				process(methods.next());
			}
		}
		
		private void process(SootMethod method) {
			if(!method.hasActiveBody()) {
				return;
			}
			for(Unit unit : method.getActiveBody().getUnits()) {
				Stmt stmt = (Stmt)unit;
				
				// Stmt methods
				this.stmtToMethod.put(stmt, method);
				
				// Def boxes
				for(ValueBox def : stmt.getDefBoxes()) {
					if(def.getValue() instanceof Local) {
						//System.out.println("DEF: " + def.getValue() + " ## " + stmt);
						this.localToDef.add((Local)def.getValue(), stmt);
						add((Local)def.getValue(), method);
					} else {
						//System.out.println("VALUE TYPE: " + def.getValue().getClass().getName());
					}
				}
				
				// Use boxes
				for(ValueBox use : stmt.getUseBoxes()) {
					if(use.getValue() instanceof Local) {
						//System.out.println("USE: " + use.getValue() + " ## " + stmt);
						this.localToUse.add((Local)use.getValue(), stmt);
						add((Local)use.getValue(), method);
					} else {
						//System.out.println("VALUE TYPE: " + use.getValue().getClass().getName());
					}
				}
				
				// Cast expressions
				if(stmt instanceof AssignStmt) {
					Value rightOp = ((AssignStmt)stmt).getRightOp();
					if(rightOp instanceof CastExpr) {
						this.castToStmt.put((CastExpr)rightOp, stmt);
					}
				}
				
				// String constants
				for(ValueBox use : stmt.getUseBoxes()) {
					if(use.getValue() instanceof StringConstant) {
						this.strToStmt.add(new Pair<SootMethod,String>(method, ((StringConstant)use.getValue()).value), stmt);
					}
				}
				
				// Callees
				Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(stmt);
				while(edges.hasNext()) {
					this.callers.add(edges.next().getTgt().method(), stmt);
				}
			}
		}
	
		private void add(Local local, SootMethod method) {
			if(this.localToMethod.containsKey(local)) {
				if(!this.localToMethod.get(local).equals(method)) {
					throw new RuntimeException("Mismatched methods!");
				}
			}
			this.localToMethod.put(local, method);
		}
	}
	
	private static VariableMap varMap = null;
	public static VariableMap getVarMap() {
		if(varMap == null) {
			varMap = new VariableMap();
		}
		return varMap;
	}
	
	private static MultivalueMap<AllocNode,VarNode> libraryAllocNodeMap = null;
	private static MultivalueMap<AllocNode,VarNode> getLibraryAllocNodeMap() {
		if(libraryAllocNodeMap == null) {
			libraryAllocNodeMap = new MultivalueMap<AllocNode,VarNode>();
			ProgramRel relMonitorSource = (ProgramRel)ClassicProject.g().getTrgt("MonitorSource");
			relMonitorSource.load();
			for(chord.util.tuple.object.Pair<Object,Object> pair : relMonitorSource.getAry2ValTuples()) {
				libraryAllocNodeMap.add((AllocNode)pair.val1, (VarNode)pair.val0);
			}
			relMonitorSource.close();
		}
		return libraryAllocNodeMap;
	}
	
	private static MultivalueMap<VarNode,VarNode> libraryVarNodeMap = null;
	private static MultivalueMap<VarNode,VarNode> getLibraryVarNodeMap() {
		if(libraryVarNodeMap == null) {
			libraryVarNodeMap = new MultivalueMap<VarNode,VarNode>();
			ProgramRel relMonitorSink = (ProgramRel)ClassicProject.g().getTrgt("MonitorSink");
			relMonitorSink.load();
			for(chord.util.tuple.object.Pair<Object,Object> pair : relMonitorSink.getAry2ValTuples()) {
				libraryVarNodeMap.add((VarNode)pair.val1, (VarNode)pair.val0);
			}
			relMonitorSink.close();
		}
		return libraryVarNodeMap;
	}
	
	public static class MonitorMap {
		private final MultivalueMap<VarNode,Monitor> varToMonitor = new MultivalueMap<VarNode,Monitor>();
		private final MultivalueMap<AllocNode,Monitor> allocToMonitor = new MultivalueMap<AllocNode,Monitor>();
		
		public MonitorMap() {
			MultivalueMap<VarNode,VarNode> libraryVarNodeMap = getLibraryVarNodeMap();
			MultivalueMap<AllocNode,VarNode> libraryAllocNodeMap = getLibraryAllocNodeMap();
			
			Iterator<VarNode> varIter = ((DomV)ClassicProject.g().getTrgt("V")).iterator();
			List<VarNode> vars = new ArrayList<VarNode>();
			while(varIter.hasNext()) {
				VarNode var = varIter.next();
				if(libraryVarNodeMap.containsKey(var)) {
					vars.addAll(libraryVarNodeMap.get(var));
				} else {
					vars.add(var);
				}
			}
			
			Iterator<AllocNode> allocIter = ((DomH)ClassicProject.g().getTrgt("H")).iterator();
			List<AllocNode> allocs = new ArrayList<AllocNode>();
			while(allocIter.hasNext()) {
				AllocNode alloc = allocIter.next();
				if(libraryAllocNodeMap.containsKey(alloc)) {
					vars.addAll(libraryAllocNodeMap.get(alloc));
				} else {
					allocs.add(alloc);
				}
			}

			processVars(vars);
			processAllocs(allocs);
		}
		
		private void processVars(Iterable<VarNode> vars) {
			VariableMap varMap = getVarMap();
			for(VarNode var : vars) {
				System.out.println("VAR: " + var);
				if(var instanceof CastVarNode) {
					Stmt stmt = varMap.getStmt(((CastVarNode)var).castExpr);
					this.processDefinition((CastVarNode)var, varMap.getMethod(stmt), stmt);
				} else if(var instanceof LocalVarNode) {
					for(Stmt stmt : varMap.getDefs(((LocalVarNode)var).local)) {
						this.processDefinition((LocalVarNode)var, varMap.getMethod(stmt), stmt);
					}
				} else if(var instanceof ParamVarNode) {
					ParamVarNode paramVar = (ParamVarNode)var;
					this.processMethodParam((ParamVarNode)var, paramVar.method, paramVar.index);
				} else if(var instanceof RetVarNode) {
					for(Stmt stmt : varMap.getCallers(((RetVarNode)var).method)) {
						this.processDefinition((RetVarNode)var, varMap.getMethod(stmt), stmt);
					}
				} else if(var instanceof StringConstantVarNode) {
					StringConstantVarNode constVar = (StringConstantVarNode)var;
					for(Stmt stmt : varMap.getStmts(constVar.method, constVar.sc)) {
						this.processDefinition((StringConstantVarNode)var, varMap.getMethod(stmt), stmt);
					}
				} else if(var instanceof ThisVarNode) {
					this.processMethodThis((ThisVarNode)var, ((ThisVarNode)var).method);
				}
				System.out.println();
			}
		}
		
		private void processAllocs(Iterable<AllocNode> allocs) {
			VariableMap varMap = getVarMap();
			for(AllocNode alloc : allocs) {
				System.out.println("ALLOC: " + alloc);
				if(alloc instanceof StringConstNode) {
					StringConstNode strAlloc = (StringConstNode)alloc;
					if(strAlloc.method != null) {
						for(Stmt stmt : varMap.getStmts(strAlloc.method, strAlloc.value)) {
							this.processDefinition(strAlloc, strAlloc.method, stmt);
						}
					} else {
						System.out.println("Global alloc");
					}
				} else if(alloc instanceof StubAllocNode) {
					System.out.println("Stub alloc");
				} else if(alloc instanceof SiteAllocNode) {
					SiteAllocNode siteAlloc = (SiteAllocNode)alloc;
					this.processDefinition(siteAlloc, varMap.getMethod((Stmt)siteAlloc.getUnit()), (Stmt)siteAlloc.getUnit());
				}
			}
		}
		
		private void processDefinition(CastVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				this.varToMonitor.add(var, monitor);
			}
		}
		
		private void processDefinition(StringConstantVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				this.varToMonitor.add(var, monitor);
			}
		}
		
		private void processDefinition(LocalVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				this.varToMonitor.add(var, monitor);
			}
		}
		
		private void processDefinition(RetVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				this.varToMonitor.add(var, monitor);
			}
		}
		
		private void processMethodParam(ParamVarNode var, SootMethod method, int index) {
			this.varToMonitor.add(var, InstrumentationDataWriter.getMonitorForMethodParam(method, index));
		}
		
		private void processMethodThis(ThisVarNode var, SootMethod method) {
			this.varToMonitor.add(var, InstrumentationDataWriter.getMonitorForMethodThis(method));
		}
		
		private void processDefinition(SiteAllocNode alloc, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				this.allocToMonitor.add(alloc, monitor);
			}
		}
		
		public void processDefinition(StringConstNode alloc, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				this.allocToMonitor.add(alloc, monitor);
			}
		}
	}	
}
