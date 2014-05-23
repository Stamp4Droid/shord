package stamp.analyses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.HashReversibleGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;

/** 
    Intra-procedural Control Dependence Graph

	Based on the algorithm given Section 6 in
    Representation and Analysis of Software, Mary Jean Harrold, Greg Rothermel, Alex Orso

	@author Saswat Ananad
 */
public class ControlDependenceGraph
{
	private Map<Object,Set<Block>> nodeToDependees = new HashMap();

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
		  }*/

		for(Block a : cfg.getBlocks()){
			// Step 1
			// if node a had more than one successors then 
			// each successor does not post-dominate a
			// So S is the set succs
			List<Block> succs = cfg.getSuccsOf(a);
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

				for(Block b : succs){
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
		for(Map.Entry<Object,Set<Block>> e : nodeToDependees.entrySet()){
			Object block = e.getKey();
			if(!(block instanceof Block))
				continue; //block is the super-exit node
			Set<Block> dependees = e.getValue();
			for(Block dependee : dependees){
				Unit t = dependee.getTail();
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
		/*
		//debug
		System.out.println(">> CDG");
		for(Map.Entry<Unit,Set<Unit>> e : result.entrySet()){
			Unit branchStmt = e.getKey();
			System.out.println(branchStmt+":");
			for(Unit s : e.getValue()){
				System.out.println("\t"+s);
			}
		}
		*/
		return result;
	}

	private Object getImmediateDominator(DominatorTree domlysis, Object node)
	{
		DominatorNode n = domlysis.getParentOf(domlysis.getDode(node));
		if(n == null) return null; else return n.getGode();
	}    
}
