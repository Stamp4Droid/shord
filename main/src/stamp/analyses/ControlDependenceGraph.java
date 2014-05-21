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
    private Map<Block,Set<Block>> nodeToDependees = new HashMap();
    
    public ControlDependenceGraph(SootMethod method)
    {
		BlockGraph cfg = new ExceptionalBlockGraph(method.retrieveActiveBody());
		
		for(Block block : cfg.getBlocks())
			nodeToDependees.put(block, new HashSet());
		
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
				
				Set<Block> ancestorsA = new HashSet();
				Block parent = a;
				while(parent != null){
					ancestorsA.add(parent);
					parent = (Block) getImmediateDominator(domlysis, parent);
					//System.out.println("!! " + parent);
				}
				
				for(Block b : succs){
					Set<Block> marked = new HashSet();
					Block l = b;
					do{
						if(ancestorsA.contains(l)){
							// l is the least common ancestors
							//System.out.println("LCA: " + l );
							break;
						}
						else{
							marked.add(l);
							//System.out.print("Immediate dominator of " + l + " is ");
							l = (Block) getImmediateDominator(domlysis, l);
							assert l != null;
							//System.out.println(l);
						}
					}while(true);
					if(l == a)
						marked.add(l);
					
					for(Block node : marked)
						nodeToDependees.get(node).add(a);
				}
			}
		}
    }

	public Map<Unit,Set<Unit>> dependeeToDependentsSetMap()
	{
		Map<Unit,Set<Unit>> result = new HashMap();
		for(Map.Entry<Block,Set<Block>> e : nodeToDependees.entrySet()){
			Block block = e.getKey();
			Set<Block> dependees = e.getValue();
			for(Block dependee : dependees){
				Unit t = dependee.getTail();
				if(!t.branches()) 
					assert false: dependee + " does not branch!";
				Set<Unit> dependents = result.get(t);
				if(dependents == null){
					dependents = new HashSet();
					result.put(t, dependents);
				}
				for(Iterator<Unit> uit = block.iterator(); uit.hasNext();){
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
