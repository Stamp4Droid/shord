package stamp.analyses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.analyses.LocalVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.HashReversibleGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import chord.util.tuple.object.Trio;
/** 
    Intra-procedural Control Dependence Graph

	Based on the algorithm given Section 6 in
    Representation and Analysis of Software, Mary Jean Harrold, Greg Rothermel, Alex Orso

	@author Saswat Ananad
	@author Osbert Bastani
 */
public class ExceptionalControlDependenceGraph {
	private static Object getImmediateDominator(DominatorTree domTree, Object node) {
		DominatorNode dominator = domTree.getParentOf(domTree.getDode(node));
		return dominator == null ? null : dominator.getGode();
	}
	
	public static class ExceptionDependeeStruct {
		public final Unit invokeUnit;
		public final Unit targetUnit;
		public final VarNode var;
		public ExceptionDependeeStruct(Unit invokeUnit, Unit targetUnit, VarNode var) {
			this.invokeUnit = invokeUnit;
			this.targetUnit = targetUnit;
			this.var = var;
		}
	}
	
	// the unit where the var node is defined
	private static Map<VarNode,Unit> varNodesByDefUnit;
	public static Unit getUnit(VarNode var) {
		if(varNodesByDefUnit == null) {
			// STEP 0: Initialize
			varNodesByDefUnit = new HashMap<VarNode,Unit>();
			// STEP 1: Iterate over methods
			ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("ci_reachableM");
			relReachableM.load();
			for(Object obj : relReachableM.getAry1ValTuples()) {
				SootMethod method = (SootMethod)obj;
				if(!method.hasActiveBody()) {
					continue;
				}
				// STEP 1a: Get the var node -> local map
				Map<Local,LocalVarNode> map = LocalsToVarNodeMap.getLocalToVarNodeMap(method);
				if(map == null) {
					continue;
				}
				// STEP 1b: Iterate over units in the body and construct the var node -> unit map
				for(Unit unit : method.getActiveBody().getUnits()) {
					for(ValueBox valueBox : unit.getDefBoxes()) {
						if(!(valueBox.getValue() instanceof Local)) {
							continue;
						}
						VarNode curVar = map.get((Local)valueBox.getValue());
						if(varNodesByDefUnit.get(curVar) != null) {
							throw new RuntimeException("Duplicate definition for variable: " + curVar);
						}
						varNodesByDefUnit.put(curVar, unit);
					}
				}	
			}
			// STEP 2: Cleanup
			relReachableM.close();
		}
		return varNodesByDefUnit.get(var);
	}
	
	private static MultivalueMap<SootMethod,ExceptionDependeeStruct> exceptionDependeeStructs;
	public static Iterable<ExceptionDependeeStruct> getExceptionDependeeStructs(SootMethod method) {
		if(exceptionDependeeStructs == null) {
			// STEP 0: Setup
			exceptionDependeeStructs = new MultivalueMap<SootMethod,ExceptionDependeeStruct>();
			
			// STEP 1: Get MI
			Map<Unit,SootMethod> mi = new HashMap<Unit,SootMethod>();
			ProgramRel relMI = (ProgramRel)ClassicProject.g().getTrgt("MI");
			relMI.load();
			for(chord.util.tuple.object.Pair<Object,Object> pair : relMI.getAry2ValTuples()) {
				if(mi.get((Unit)pair.val1) != null) {
					throw new RuntimeException("Duplicate method for unit: " + pair.val1);
				}
				mi.put((Unit)pair.val1, (SootMethod)pair.val0);
			}
			relMI.close();

			// STEP 2a: Process handler dependees
			ProgramRel relHandlerDependee = (ProgramRel)ClassicProject.g().getTrgt("HandlerDependee");
			relHandlerDependee.load();
			for(Trio<Object,Object,Object> trio : relHandlerDependee.getAry3ValTuples()) {
				System.out.println("HANDLER DEPENDEE: " + trio.val0 + " -> " + trio.val1 + " -> " + trio.val2);
				exceptionDependeeStructs.add(mi.get((Unit)trio.val0), new ExceptionDependeeStruct((Unit)trio.val0, getUnit((VarNode)trio.val2), (VarNode)trio.val1));
			}
			relHandlerDependee.close();
			
			// STEP 2b: Process super exit dependees
			ProgramRel relSuperExitDependee = (ProgramRel)ClassicProject.g().getTrgt("SuperExitDependee");
			relSuperExitDependee.load();
			for(chord.util.tuple.object.Pair<Object,Object> pair : relSuperExitDependee.getAry2ValTuples()) {
				System.out.println("SUPER EXIT DEPENDEE: " + pair.val0 + " -> " + pair.val1);
				exceptionDependeeStructs.add(mi.get((Unit)pair.val0), new ExceptionDependeeStruct((Unit)pair.val0, null, (VarNode)pair.val1));
			}
			relSuperExitDependee.close();

			// STEP 2c: Process prim handler dependees
			ProgramRel relHandlerDependeePrim = (ProgramRel)ClassicProject.g().getTrgt("HandlerDependeePrim");
			relHandlerDependeePrim.load();
			for(Trio<Object,Object,Object> trio : relHandlerDependeePrim.getAry3ValTuples()) {
				System.out.println("HANDLER DEPENDEE PRIM: " + trio.val0 + " -> " + trio.val1 + " -> " + trio.val2);
				exceptionDependeeStructs.add(mi.get((Unit)trio.val0), new ExceptionDependeeStruct((Unit)trio.val0, getUnit((VarNode)trio.val2), (VarNode)trio.val1));
			}
			relHandlerDependeePrim.close();
			
			// STEP 2b: Process prim super exit dependees
			ProgramRel relSuperExitDependeePrim = (ProgramRel)ClassicProject.g().getTrgt("SuperExitDependeePrim");
			relSuperExitDependeePrim.load();
			for(chord.util.tuple.object.Pair<Object,Object> pair : relSuperExitDependeePrim.getAry2ValTuples()) {
				System.out.println("SUPER EXIT DEPENDEE PRIM: " + pair.val0 + " -> " + pair.val1);
				exceptionDependeeStructs.add(mi.get((Unit)pair.val0), new ExceptionDependeeStruct((Unit)pair.val0, null, (VarNode)pair.val1));
			}
			relSuperExitDependeePrim.close();
		}
		return exceptionDependeeStructs.get(method);
	}
	
	private static MultivalueMap<Unit,Object> getExceptionalEdges(SootMethod method, Object superExitNode) {
		MultivalueMap<Unit,Object> edges = new MultivalueMap<Unit,Object>();
		for(ExceptionDependeeStruct struct : getExceptionDependeeStructs(method)) {
			edges.add(struct.invokeUnit, struct.targetUnit == null ? superExitNode : struct.targetUnit);
		}
		return edges;
	}
	
	public static MultivalueMap<Unit,Unit> getExceptionalControlDependenceGraph(SootMethod method) {
		// STEP 0: Setup
		MultivalueMap<Unit,Unit> dependentToDependees = new MultivalueMap<Unit,Unit>();
		UnitGraph cfg = new BriefUnitGraph(method.retrieveActiveBody());
		
		// STEP 1: Add a super exit node to the cfg
		HashReversibleGraph reversibleCFG = new HashReversibleGraph(cfg);
		List<?> tails = reversibleCFG.getTails();
		Object superExitNode = new Object();
		reversibleCFG.addNode(superExitNode);
		for(Iterator<?> it = tails.iterator(); it.hasNext();){
			reversibleCFG.addEdge(it.next(), superExitNode);
		}
		
		// STEP 2: Add exceptional edges to graph
		MultivalueMap<Unit,Object> exceptionalEdges = getExceptionalEdges(method, superExitNode);
		for(Unit unit : exceptionalEdges.keySet()) {
			for(Object obj : exceptionalEdges.get(unit)) {
				String target = obj instanceof Unit ? obj.toString() : "METHOD " + method + " EXIT";
				System.out.println("ADDING EDGE TO CFG: " + unit + " -> " + target);
				reversibleCFG.addEdge(unit, obj);
			}
		}
		
		// STEP 3: Get the dominator tree
		DominatorTree domTree = new DominatorTree(new SimpleDominatorsFinder(reversibleCFG.reverse()));
		
		// STEP 4: Compute the dependents of node unit
		for(Unit unit : cfg.getBody().getUnits()) {
			// STEP 4a: If unit has at most one successor, then nothing control-depends on unit
			List<Unit> succs = cfg.getSuccsOf(unit);
			if(succs.size() <= 1) {
				continue;
			}
			
			// STEP 4b: Compute the ancestors of unit in the post-dominator tree
			Set<Object> ancestors = new HashSet<Object>();
			Object ancestor = unit;
			while(ancestor != null) {
				ancestors.add(ancestor);
				ancestor = getImmediateDominator(domTree, ancestor);
			}

			// STEP 4c: Find the least common ancestor (lca) of unit and succ (for each succ in succs)
			for(Unit succ : succs) {
				Set<Unit> dependents = new HashSet<Unit>();
				Object lca = succ;
				while(!ancestors.contains(lca)) {
					if(lca instanceof Unit) {
						dependents.add((Unit)lca);
					}
					lca = getImmediateDominator(domTree, lca);
				}
				if(lca == unit) {
					dependents.add(unit);
				}

				for(Unit dependent : dependents) {
					System.out.println("DEPENDEE TO DEPENDENT: " + dependent + " depends on " + unit);
					dependentToDependees.add(dependent, unit);
				}
			}
		}
		
		return dependentToDependees;
	}
}

