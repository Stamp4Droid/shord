package stamp.analyses;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.analyses.DomM;
import shord.analyses.LocalVarNode;
import shord.analyses.LocalsClassifier;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.Immediate;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import chord.project.Chord;
import chord.util.tuple.object.Pair;

/**
 * @author obastani
 * TODO: handle store ind -> array, handle functions -> boolean, .equals, etc.
 */
@Chord(name = "implicit-annotation-java",
consumes = { "V", "U" },
produces = { "Ref2RefImp", "Ref2PrimImp", "Prim2RefImp", "Prim2PrimImp" },
namesOfTypes = {},
types = {},
namesOfSigns = { "Ref2RefImp", "Ref2PrimImp", "Prim2RefImp", "Prim2PrimImp" },
signs = { "V0,V1:V0xV1", "V0,U0:V0_U0", "U0,V0:V0_U0", "U0,U1:U0xU1" })
public class ImplicitAnnotationAnalysis extends JavaAnalysis {
	private ProgramRel relRefRefImp;
	private ProgramRel relRefPrimImp;
	private ProgramRel relPrimRefImp;
	private ProgramRel relPrimPrimImp;

	private void getVarNodesInHelper(Map<Local,LocalVarNode> localToVarNodeMap, Value value, Collection<LocalVarNode> result) {
		List<ValueBox> boxes = value.getUseBoxes();
		for(ValueBox box : boxes) {
			Value childValue = box.getValue();
			System.out.println("VALUE recurse: " + childValue);
			if(value instanceof Local) {
				LocalVarNode varNode = localToVarNodeMap.get((Local)value);
				if(varNode != null) {
					result.add(varNode);
				} else {
					System.out.println("ERROR: No varnode found for local " + value);
				}
			}
			this.getVarNodesInHelper(localToVarNodeMap, childValue, result);
			// TODO: any other cases?
			// TODO: e.g. box instanceof CastExpr
		}
	}

	private Collection<LocalVarNode> getVarNodesIn(Map<Local,LocalVarNode> localToVarNodeMap, Unit unit, boolean isDef) {
		Collection<LocalVarNode> result = new HashSet<LocalVarNode>();
		List<ValueBox> boxes = isDef ? unit.getDefBoxes() : unit.getUseBoxes();
		for(ValueBox box : boxes) {
			Value value = box.getValue();
			System.out.println("VALUE " + isDef + ": " + value);
			if(value instanceof Local) {
				LocalVarNode varNode = localToVarNodeMap.get((Local)value);
				if(varNode != null) {
					result.add(varNode);
				} else {
					System.out.println("ERROR: No varnode found for local " + value);
				}
			}
			if(!isDef) {
				this.getVarNodesInHelper(localToVarNodeMap, value, result);
			}
			// TODO: any other cases?
			// TODO: e.g. box instanceof CastExpr
		}
		return result;
	}
	
	private void addLocalDependents(LocalsClassifier lc, LocalVarNode parentVar, LocalVarNode dependentVar) {
		System.out.println("LOCAL DEPENDENTS: " + parentVar.local + " -> " + dependentVar.local);
		if(lc.nonPrimLocals().contains(parentVar.local)) {
			if(lc.nonPrimLocals().contains(dependentVar.local)) {
				this.relRefRefImp.add(parentVar, dependentVar);						
			} else if(lc.primLocals().contains(dependentVar.local)) {
				this.relRefPrimImp.add(parentVar, dependentVar);
			} else {
				throw new RuntimeException("Unclassified local: " + dependentVar);
			}
		} else if(lc.primLocals().contains(parentVar.local)) {
			if(lc.nonPrimLocals().contains(dependentVar.local)) {
				this.relPrimRefImp.add(parentVar, dependentVar);
			} else if(lc.primLocals().contains(dependentVar.local)) {
				this.relPrimPrimImp.add(parentVar, dependentVar);
			} else {
				throw new RuntimeException("Unclassified local: " + dependentVar);
			}					
		} else {
			throw new RuntimeException("Unclassified local: " + parentVar);
		}
	}

	private void processDependentUnits(Map<Local,LocalVarNode> localToVarNodeMap, LocalsClassifier lc, Unit parent, Unit dependent) {
		System.out.println("UNIT DEPENDENTS: " + parent + " -> " + dependent);
		for(LocalVarNode parentVar : this.getVarNodesIn(localToVarNodeMap, parent, false)) {
			for(LocalVarNode dependentVar : this.getVarNodesIn(localToVarNodeMap, dependent, true)) {
				this.addLocalDependents(lc, parentVar, dependentVar);
			}
		}
	}

	private Map<SootMethod,Map<Local,LocalVarNode>> localToVarNodeMaps = null;
	private void constructLocalToVarNodeMaps() {
		this.localToVarNodeMaps = new HashMap<SootMethod,Map<Local,LocalVarNode>>();

		ProgramRel relMV = (ProgramRel)ClassicProject.g().getTrgt("MV");
		relMV.load();
		for(Pair<Object,Object> pair : relMV.getAry2ValTuples()) {
			VarNode varNode = (VarNode)pair.val1;
			if(varNode instanceof LocalVarNode) {
				SootMethod method = (SootMethod)pair.val0;
				Map<Local,LocalVarNode> localToVarNodeMap = this.localToVarNodeMaps.get(method);
				if(localToVarNodeMap == null) {
					localToVarNodeMap = new HashMap<Local,LocalVarNode>();
					this.localToVarNodeMaps.put(method, localToVarNodeMap);
				}

				LocalVarNode localVarNode = (LocalVarNode)varNode;
				localToVarNodeMap.put(localVarNode.local, localVarNode);

				//System.out.println("ADDED VAR V " + localVarNode.local + " TO METHOD " + method);
			}
		}
		relMV.close();

		ProgramRel relMU = (ProgramRel)ClassicProject.g().getTrgt("MU");
		relMU.load();
		for(Pair<Object,Object> pair : relMU.getAry2ValTuples()) {
			VarNode varNode = (VarNode)pair.val1;
			if(varNode instanceof LocalVarNode) {
				SootMethod method = (SootMethod)pair.val0;
				Map<Local,LocalVarNode> localToVarNodeMap = this.localToVarNodeMaps.get(method);
				if(localToVarNodeMap == null) {
					localToVarNodeMap = new HashMap<Local,LocalVarNode>();
					this.localToVarNodeMaps.put(method, localToVarNodeMap);
				}

				LocalVarNode localVarNode = (LocalVarNode)varNode;
				localToVarNodeMap.put(localVarNode.local, localVarNode);

				//System.out.println("ADDED VAR U " + localVarNode.local + " TO METHOD " + method);
			}
		}
		relMU.close();
	}

	private Map<Local,LocalVarNode> getLocalToVarNodeMap(SootMethod method) {
		if(this.localToVarNodeMaps == null) {
			this.constructLocalToVarNodeMaps();
		}
		return this.localToVarNodeMaps.get(method);		
	}

	private void processArrayIndices(SootMethod method, Map<Local,LocalVarNode> localToVarNodeMap, LocalsClassifier lc) {
		for(Unit unit : method.getActiveBody().getUnits()) {
			Stmt s = (Stmt)unit;
			if(s.containsArrayRef()) {
				System.out.println("STATEMENT S: " + s);
				AssignStmt as = (AssignStmt) s;
				Value leftOp = as.getLeftOp();
				ArrayRef ar = s.getArrayRef();
				if(leftOp instanceof Local) {
					System.out.println("INSTANCE OF LOCAL");
					Immediate index = (Immediate)ar.getIndex();
					if(index instanceof Local) {
						LocalVarNode indexNode = localToVarNodeMap.get((Local)index);
						LocalVarNode lNode = localToVarNodeMap.get((Local)leftOp);
						if(indexNode != null && lNode != null) {
							System.out.println("INDEX DEPENDENTS!!");
							this.addLocalDependents(lc, indexNode, lNode);
						}
					}
				}
			}
		}
	}

	private void processMethod(SootMethod method) {
		if(!method.isConcrete()) {
			return;
		}
		if(!method.hasActiveBody()) {
			return;
		}

		Map<Local,LocalVarNode> localToVarNodeMap = this.getLocalToVarNodeMap(method);
		if(localToVarNodeMap == null) {
			//System.out.println("ERROR: Local to var node map not found for method " + method);
			return;
		}

		System.out.println("PROCESSING METHOD " + method);

		LocalsClassifier lc = new LocalsClassifier(method.getActiveBody());
		
		this.processArrayIndices(method, localToVarNodeMap, lc);

		Map<Unit,Set<Unit>> cdg;
		try {
			ControlDependenceGraph cdgGen = new ControlDependenceGraph(method);
			cdg = cdgGen.dependeeToDependentsSetMap();
		} catch(Exception e) {
			e.printStackTrace();
			cdg = new HashMap<Unit,Set<Unit>>();
		}
		
		for(Unit parent : cdg.keySet()) {
			Set<Unit> dependents = cdg.get(parent);
			if(dependents == null) {
				continue;
			}

			for(Unit dependent : dependents) {
				processDependentUnits(localToVarNodeMap, lc, parent, dependent);
			}
		}
	}

	private void process() {
		DomM domM = (DomM)ClassicProject.g().getTrgt("M");
		for(int i=0; i<domM.size(); i++) {
			this.processMethod(domM.get(i));
		}
	}

	public void run() {
		this.relRefRefImp = (ProgramRel)ClassicProject.g().getTrgt("Ref2RefImp");
		this.relRefPrimImp = (ProgramRel)ClassicProject.g().getTrgt("Ref2PrimImp");
		this.relPrimRefImp = (ProgramRel)ClassicProject.g().getTrgt("Prim2RefImp");
		this.relPrimPrimImp = (ProgramRel)ClassicProject.g().getTrgt("Prim2PrimImp");

		this.relRefRefImp.zero();
		this.relRefPrimImp.zero();
		this.relPrimRefImp.zero();
		this.relPrimPrimImp.zero();

		this.process();

		this.relRefRefImp.save();
		this.relRefPrimImp.save();
		this.relPrimRefImp.save();
		this.relPrimPrimImp.save();
	}
}
