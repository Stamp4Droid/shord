package stamp.analyses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.CastExpr;
import chord.project.Chord;
import chord.util.tuple.object.Pair;

/**
 * @author obastani
 */
@Chord(name = "implicit-flow-java",
consumes = {},
produces = { "RefRefImp", "RefPrimImp", "PrimRefImp", "PrimPrimImp" },
namesOfTypes = {},
types = {},
namesOfSigns = { "RefRefImp", "RefPrimImp", "PrimRefImp", "PrimPrimImp" },
signs = { "V0,V1:V0xV1", "V0,U0:V0_U0", "U0,V0:V0_U0", "U0,U0:U0xU1" })
public class ImplicitFlowAnalysis extends JavaAnalysis {
	private ProgramRel relRefRefImp;
	private ProgramRel relRefPrimImp;
	private ProgramRel relPrimRefImp;
	private ProgramRel relPrimPrimImp;
	
	private Collection<LocalVarNode> getVarNodesIn(Map<Local,LocalVarNode> localToVarNodeMap, Unit unit, boolean isDef) {
		Collection<LocalVarNode> result = new ArrayList<LocalVarNode>();
		List<ValueBox> boxes = isDef ? unit.getDefBoxes() : unit.getUseBoxes();
		for(ValueBox box : boxes) {
			if(box instanceof Local) {
				result.add(localToVarNodeMap.get((Local)box));
			} else if(box instanceof CastExpr) {
				// TODO: do we need to handle this case?
			}
			// TODO: any other cases?
		}
		return result;
	}
	
	private void processDependentUnits(Map<Local,LocalVarNode> localToVarNodeMap, LocalsClassifier lc, Unit parent, Unit dependent) {
		for(LocalVarNode parentVar : this.getVarNodesIn(localToVarNodeMap, parent, false)) {
			for(LocalVarNode dependentVar : this.getVarNodesIn(localToVarNodeMap, dependent, true)) {
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
		}
	}
	
	private Map<SootMethod,Map<Local,LocalVarNode>> localToVarNodeMaps = null;
	private void constructLocalToVarNodeMaps() {
		this.localToVarNodeMaps = new HashMap<SootMethod,Map<Local,LocalVarNode>>();		
		ProgramRel relMV = (ProgramRel)ClassicProject.g().getTrgt("MV");
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
			}
		}
	}
	
	private Map<Local,LocalVarNode> getLocalToVarNodeMap(SootMethod method) {
		if(this.localToVarNodeMaps == null) {
			this.constructLocalToVarNodeMaps();
		}
		return this.localToVarNodeMaps.get(method);		
	}
	
	private void processMethod(SootMethod method) {
		if(!method.isConcrete()) {
			return;
		}
		if(!method.hasActiveBody()) {
			return;
		}
		
		LocalsClassifier lc = new LocalsClassifier(method.getActiveBody());
		
		ControlDependenceGraph cdgGen = new ControlDependenceGraph(method);
		Map<Unit,Set<Unit>> cdg = cdgGen.dependeeToDependentsSetMap();
		
		for(Unit parent : cdg.keySet()) {
			Set<Unit> dependents = cdg.get(parent);
			if(dependents == null) {
				continue;
			}
			
			for(Unit dependent : dependents) {
				processDependentUnits(this.getLocalToVarNodeMap(method), lc, parent, dependent);
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
		this.relRefRefImp = (ProgramRel)ClassicProject.g().getTrgt("InLabelArg");
		this.relRefPrimImp = (ProgramRel)ClassicProject.g().getTrgt("InLabelRet");
		this.relPrimRefImp = (ProgramRel)ClassicProject.g().getTrgt("OutLabelArg");
		this.relPrimPrimImp = (ProgramRel)ClassicProject.g().getTrgt("OutLabelRet");
		
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
