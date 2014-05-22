package stamp.analyses;

import soot.Unit;
import soot.SootMethod;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.HashReversibleGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;
import java.util.*;

/** 
    Intra-procedural Control Dependence Graph
	@author Saswat Ananad
 */
public class ControlDependenceGraph
{
	private Map<Object,Set<Object>> nodeToDependees = new HashMap();

	public ControlDependenceGraph(SootMethod method)
	{
		BlockGraph cfg = new ExceptionalBlockGraph(method.retrieveActiveBody());

		//Add a super exit node to the cfg
		HashReversibleGraph reversibleCFG = new HashReversibleGraph(cfg);

		List tails = reversibleCFG.getTails();
		if(tails.size() > 1){
			Object superExitNode = new Object();
			reversibleCFG.addNode(superExitNode);
			for(Iterator it = tails.iterator(); it.hasNext();){
				Object tail = it.next();
				//System.out.println("tail " + tail);
				reversibleCFG.addEdge(tail, superExitNode);
			}
		}

		for(Object block : reversibleCFG.getNodes())
			nodeToDependees.put(block, new HashSet());

		DominatorsFinder domfinder = new SimpleDominatorsFinder(reversibleCFG.reverse());
		DominatorTree domlysis = new DominatorTree(domfinder);
		/*
		  System.out.println("**** Postdominator Tree of " + method);
		  for(Iterator it = cfg.iterator(); it.hasNext();){
		  Block a = (Block) it.next();
		  Object b = domfinder.getImmediateDominator(a);
		  if(b instanceof Block)
		  System.out.print(((Block) b).getIndexInMethod());
		  else
		  System.out.print("Exit");
		  System.out.println("  --->  " + a.getIndexInMethod());
		  }
		 */

		for(Object a : reversibleCFG.getNodes()){
			// Step 1
			// if node a had more than one successors then 
			// each successor does not post-dominate a
			// So S is the set succs
			List succs = reversibleCFG.getSuccsOf(a);
			if(succs.size() > 1){

				// Step 2
				// for each b in S (i.e., succs) find the
				// least common ancestor of a and b

				Set ancestorsA = new HashSet();
				Object parent = a;
				while(parent != null){
					ancestorsA.add(parent);
					parent = getImmediateDominator(domlysis, parent);
				}

				for(Object b : succs){
					Set marked = new HashSet();
					Object l = b;
					do{
						if(ancestorsA.contains(l)){
							// l is the least common ancestors
							//System.out.println("LCA: " + l );
							break;
						}
						else{
							marked.add(l);
							//System.out.print("Immediate dominator of " + l + " is ");
							l = getImmediateDominator(domlysis, l);
							//System.out.println(l);
						}
					} while(true);
					if(l == a)
						marked.add(l);

					for(Object node : marked)
						nodeToDependees.get(node).add(a);
				}
			}
		}
	}

	public Map<Unit,Set<Unit>> dependeeToDependentsSetMap()
	{
		Map<Unit,Set<Unit>> result = new HashMap();
		for(Map.Entry<Object,Set<Object>> e : nodeToDependees.entrySet()){
			Object block = e.getKey();
			Set<Object> dependees = e.getValue();
			for(Object dependee : dependees){
				if(dependee instanceof Object)
					continue; //dependee is the super-exit node
				Unit t = ((Block) dependee).getTail();
				if(!t.branches()) 
					throw new RuntimeException(dependee + " does not branch!");
				Set<Unit> dependents = result.get(t);
				if(dependents == null){
					dependents = new HashSet();
					result.put(t, dependents);
				}
				for(Iterator<Unit> uit = ((Block) block).iterator(); uit.hasNext();){
					dependents.add(uit.next());
				}
			}
		}
		return result;
	}

	private Object getImmediateDominator(DominatorTree domlysis, Object node)
	{
		DominatorNode n = domlysis.getParentOf(domlysis.getDode(node));
		if(n == null) return null; else return n.getGode();
	}    
}
