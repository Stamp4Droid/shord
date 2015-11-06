package stamp.analyses.inferaliasmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.ErrorMonitor;
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
	
	private static Set<AllocNode> libraryAlloc = null;
	private static Set<AllocNode> getLibraryAlloc() {
		if(libraryAlloc == null) {
			libraryAlloc = new HashSet<AllocNode>();
			ProgramRel relFrameworkObj = (ProgramRel)ClassicProject.g().getTrgt("FrameworkObj");
			relFrameworkObj.load();
			for(Object obj : relFrameworkObj.getAry1ValTuples()) {
				libraryAlloc.add((AllocNode)obj);
			}
			relFrameworkObj.close();
		}
		return libraryAlloc;
	}
	
	private static Set<VarNode> libraryVar = null;
	private static Set<VarNode> getLibraryVar() {
		if(libraryVar == null) {
			libraryVar = new HashSet<VarNode>();
			ProgramRel relFrameworkVar = (ProgramRel)ClassicProject.g().getTrgt("FrameworkVar");
			relFrameworkVar.load();
			for(Object obj : relFrameworkVar.getAry1ValTuples()) {
				libraryVar.add((VarNode)obj);
			}
			relFrameworkVar.close();
		}
		return libraryVar;
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
		
		private MonitorMap() {
			MultivalueMap<VarNode,VarNode> libraryVarNodeMap = getLibraryVarNodeMap();
			MultivalueMap<AllocNode,VarNode> libraryAllocNodeMap = getLibraryAllocNodeMap();
			Set<VarNode> libraryVar = getLibraryVar();
			Set<AllocNode> libraryAlloc = getLibraryAlloc();
			
			Iterator<VarNode> varIter = ((DomV)ClassicProject.g().getTrgt("V")).iterator();
			while(varIter.hasNext()) {
				VarNode var = varIter.next();
				if(libraryVar.contains(var)) {
					if(libraryVarNodeMap.containsKey(var)) {
						for(VarNode varMonitor : libraryVarNodeMap.get(var)) {
							for(Monitor monitor : processVar(varMonitor)) {
								this.varToMonitor.add(var, monitor);
							}
						}
					} else {
						this.varToMonitor.add(var, new ErrorMonitor("Library var without a map!"));
					}
				} else {
					for(Monitor monitor : processVar(var)) {
						this.varToMonitor.add(var, monitor);
					}
				}
			}
			
			Iterator<AllocNode> allocIter = ((DomH)ClassicProject.g().getTrgt("H")).iterator();
			while(allocIter.hasNext()) {
				AllocNode alloc = allocIter.next();
				if(libraryAlloc.contains(alloc)) {
					if(libraryAllocNodeMap.containsKey(alloc)) {
						for(VarNode varMonitor : libraryAllocNodeMap.get(alloc)) {
							for(Monitor monitor : processVar(varMonitor)) {
								this.allocToMonitor.add(alloc, monitor);
							}
						}
					} else {
						this.allocToMonitor.add(alloc, new ErrorMonitor("Framework alloc without a map!"));
					}
				} else {
					for(Monitor monitor : processAlloc(alloc)) {
						this.allocToMonitor.add(alloc, monitor);
					}
				}
			}
		}
		
		private static Iterable<Monitor> processVar(VarNode var) {
			VariableMap varMap = getVarMap();
			List<Monitor> monitors = new ArrayList<Monitor>();
			if(var instanceof CastVarNode) {
				Stmt stmt = varMap.getStmt(((CastVarNode)var).castExpr);
				processDefinition(monitors, (CastVarNode)var, varMap.getMethod(stmt), stmt);
			} else if(var instanceof LocalVarNode) {
				for(Stmt stmt : varMap.getDefs(((LocalVarNode)var).local)) {
					processDefinition(monitors, (LocalVarNode)var, varMap.getMethod(stmt), stmt);
				}
			} else if(var instanceof ParamVarNode) {
				ParamVarNode paramVar = (ParamVarNode)var;
				processMethodParam(monitors, (ParamVarNode)var, paramVar.method, paramVar.index);
			} else if(var instanceof RetVarNode) {
				for(Stmt stmt : varMap.getCallers(((RetVarNode)var).method)) {
					processDefinition(monitors, (RetVarNode)var, varMap.getMethod(stmt), stmt);
				}
			} else if(var instanceof StringConstantVarNode) {
				StringConstantVarNode constVar = (StringConstantVarNode)var;
				for(Stmt stmt : varMap.getStmts(constVar.method, constVar.sc)) {
					processDefinition(monitors, (StringConstantVarNode)var, varMap.getMethod(stmt), stmt);
				}
			} else if(var instanceof ThisVarNode) {
				processMethodThis(monitors, (ThisVarNode)var, ((ThisVarNode)var).method);
			}
			return monitors;
		}
		
		private static Iterable<Monitor> processAlloc(AllocNode alloc) {
			VariableMap varMap = getVarMap();
			List<Monitor> monitors = new ArrayList<Monitor>();
			if(alloc instanceof StringConstNode) {
				StringConstNode strAlloc = (StringConstNode)alloc;
				if(strAlloc.method != null) {
					for(Stmt stmt : varMap.getStmts(strAlloc.method, strAlloc.value)) {
						processDefinition(monitors, strAlloc, strAlloc.method, stmt);
					}
				} else {
					monitors.add(new ErrorMonitor("Global alloc not handled!"));
				}
			} else if(alloc instanceof StubAllocNode) {
				monitors.add(new ErrorMonitor("StubAllocs not handled!"));
			} else if(alloc instanceof SiteAllocNode) {
				SiteAllocNode siteAlloc = (SiteAllocNode)alloc;
				processDefinition(monitors, siteAlloc, varMap.getMethod((Stmt)siteAlloc.getUnit()), (Stmt)siteAlloc.getUnit());
			}
			return monitors;
		}
		
		private static void processDefinition(List<Monitor> monitors, CastVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				monitors.add(monitor);
			}
		}
		
		private static void processDefinition(List<Monitor> monitors, StringConstantVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				monitors.add(monitor);
			}
		}
		
		private static void processDefinition(List<Monitor> monitors, LocalVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				monitors.add(monitor);
			}
		}
		
		private static void processDefinition(List<Monitor> monitors, RetVarNode var, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				monitors.add(monitor);
			}
		}
		
		private static void processMethodParam(List<Monitor> monitors, ParamVarNode var, SootMethod method, int index) {
			monitors.add(InstrumentationDataWriter.getMonitorForMethodParam(method, index));
		}
		
		private static void processMethodThis(List<Monitor> monitors, ThisVarNode var, SootMethod method) {
			monitors.add(InstrumentationDataWriter.getMonitorForMethodThis(method));
		}
		
		private static void processDefinition(List<Monitor> monitors, SiteAllocNode alloc, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				monitors.add(monitor);
			}
		}
		
		public static void processDefinition(List<Monitor> monitors, StringConstNode alloc, SootMethod method, Stmt stmt) {
			for(Monitor monitor : InstrumentationDataWriter.getMonitorForDefinition(method, stmt)) {
				monitors.add(monitor);
			}
		}
	}
	
	private static MonitorMap monitorMap = null;
	private static MonitorMap getMonitorMap() {
		if(monitorMap == null) {
			monitorMap = new MonitorMap();
		}
		return monitorMap;
	}
	
	public static MultivalueMap<String,Monitor> getMonitorMapForVertices(Iterable<String> vertices) {
		DomV domV = (DomV)ClassicProject.g().getTrgt("V");
		DomH domH = (DomH)ClassicProject.g().getTrgt("H");
		MonitorMap map = getMonitorMap();
		MultivalueMap<String,Monitor> monitors = new MultivalueMap<String,Monitor>();
		for(String vertex : vertices) {
			if(vertex.startsWith("V")) {
				VarNode var = domV.get(Integer.parseInt(vertex.substring(1)));
				for(Monitor monitor : map.varToMonitor.get(var)) {
					monitors.add(vertex, monitor);
				}
			} else if(vertex.startsWith("H")) {
				AllocNode alloc = domH.get(Integer.parseInt(vertex.substring(1)));
				for(Monitor monitor : map.allocToMonitor.get(alloc)) {
					monitors.add(vertex, monitor);
				}
			} else {
				throw new RuntimeException("Unrecognized vertex: " + vertex);
			}
		}
		return monitors;
	}
	
	//
	// METHODS BELOW THIS LINE ARE NO LONGER USED
	//
	
	private static Iterable<Monitor> getMonitors() {
		MonitorMap monitorMap = getMonitorMap();
		return getMonitors(monitorMap.varToMonitor.keySet(), monitorMap.allocToMonitor.keySet());
	}
	
	private static void printMonitors(Iterable<Monitor> monitors) {
		for(Monitor monitor : monitors) {
			System.out.println(monitor.getRecord(3192));
		}
	}
	
	private static Iterable<Monitor> getMonitors(Iterable<String> vertices) {
		DomV domV = (DomV)ClassicProject.g().getTrgt("V");
		DomH domH = (DomH)ClassicProject.g().getTrgt("H");
		List<VarNode> vars = new ArrayList<VarNode>();
		List<AllocNode> allocs = new ArrayList<AllocNode>();
		for(String vertex : vertices) {
			if(vertex.startsWith("V")) {
				vars.add(domV.get(Integer.parseInt(vertex.substring(1))));
			} else if(vertex.startsWith("H")) {
				allocs.add(domH.get(Integer.parseInt(vertex.substring(1))));
			} else {
				throw new RuntimeException("Unrecognized vertex: " + vertex);
			}
		}
		return getMonitors(vars, allocs);
	}
	
	private static Iterable<Monitor> getMonitors(Iterable<VarNode> vars, Iterable<AllocNode> allocs) {
		MonitorMap monitorMap = getMonitorMap();
		List<Monitor> monitors = new ArrayList<Monitor>();
		for(VarNode var : vars) {
			/*
			System.out.println("VAR: " + var);
			for(Monitor monitor : monitorMap.varToMonitor.get(var)) {
				System.out.println(monitor.getRecord(3192));
			}
			System.out.println();
			*/
			monitors.addAll(monitorMap.varToMonitor.get(var));
		}
		
		for(AllocNode alloc : allocs) {
			/*
			System.out.println("ALLOC: " + alloc);
			for(Monitor monitor : monitorMap.allocToMonitor.get(alloc)) {
				System.out.println(monitor.getRecord(3192));
			}
			System.out.println();
			*/
			monitors.addAll(monitorMap.allocToMonitor.get(alloc));
		}
		
		return monitors;
	}
}
