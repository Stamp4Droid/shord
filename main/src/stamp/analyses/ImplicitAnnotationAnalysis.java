package stamp.analyses;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import shord.analyses.DomM;
import shord.analyses.LocalVarNode;
import shord.analyses.LocalsClassifier;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.Immediate;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import chord.project.Chord;

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
	
	// This adds that dependentVar depends on parentVar (it uses lc as a switch of ref vs. prim variables)
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

	// This iterates over use locals in the parent and def locals in the dependent and adds them as pairs
	private void processDependentUnits(Map<Local,LocalVarNode> localToVarNodeMap, LocalsClassifier lc, Unit parent, Unit dependent) {
		System.out.println("UNIT DEPENDENTS: " + parent + " -> " + dependent);
		for(LocalVarNode parentVar : LocalsToVarNodeMap.getVarNodesIn(localToVarNodeMap, parent, false)) {
			for(LocalVarNode dependentVar : LocalsToVarNodeMap.getVarNodesIn(localToVarNodeMap, dependent, true)) {
				this.addLocalDependents(lc, parentVar, dependentVar);
			}
		}
	}

	// Handle the special case of dependence i -> x in statement x = y[i]
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

		Map<Local,LocalVarNode> localToVarNodeMap = LocalsToVarNodeMap.getLocalToVarNodeMap(method);
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
