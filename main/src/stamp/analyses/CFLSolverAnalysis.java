package stamp.analyses;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.Graph;
import stamp.missingmodels.util.cflsolver.Graph.Edge;
import stamp.missingmodels.util.cflsolver.ReachabilitySolver;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar flowGrammar = new ContextFreeGrammar();
	static {
		flowGrammar.addUnaryProduction("Flow", "newCtxt", true);
		flowGrammar.addBinaryProduction("Flow", "Flow", "assignCtxt", false, true);
		flowGrammar.addBinaryProduction("Flow", "Flow", "assignCCtxt", false, true);
		flowGrammar.addProduction("FlowField", new String[]{"Flow", "storeCtxt", "Flow"}, new boolean[]{false, true, true});
		flowGrammar.addProduction("Flow", new String[]{"FlowField", "Flow", "loadCtxt"}, new boolean[]{false, false, true});
		flowGrammar.addBinaryProduction("FlowStatField", "Flow", "storeStatCtxt", false, true);
		flowGrammar.addBinaryProduction("Flow", "FlowStatField", "loadStatCtxt", false, true);
	}

	public static void main(String[] args) {
		System.out.println("CFG:");
		System.out.println(flowGrammar.toString());

		Graph g = new Graph(flowGrammar);
		g.addEdge("x", "o1", "newCtxt", (short)1);
		g.addEdge("z", "o2", "newCtxt", (short)1);
		g.addEdge("y", "x", "assignCtxt", (short)1);
		g.addEdge("y", "z", "storeCtxt", "f", (short)1);
		g.addEdge("w", "x", "assignCtxt", (short)1);
		g.addEdge("v", "w", "loadCtxt", "f", (short)1);

		new ReachabilitySolver().solve(flowGrammar, g);	

		System.out.println("Solution:");
		System.out.println(g.toString());
		
		MultivalueMap<String,Edge> sortedEdges = g.getSortedEdges();
		for(String label : sortedEdges.keySet()) {
			System.out.println(label + ": " + sortedEdges.get(label).size());
		}
	}

	@Override
	public void run() {
		Graph g = new Graph(flowGrammar);

		for(String label : flowGrammar.getLabelNames()) {
			for(Relation relation : ConversionUtils.getChordRelationsFor(label)) {
				try {
					// STEP 1: Load the Shord relation.
					final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getRelationName());
					rel.load();
					Iterable<int[]> res = rel.getAryNIntTuples();

					// STEP 2: Iterate over relation and add to the graph.
					for(int[] tuple : res) {
						String sourceName = relation.getSourceFromTuple(tuple);
						String sinkName = relation.getSinkFromTuple(tuple);

						if(relation.hasLabel()) {
							g.addEdge(sourceName, sinkName, label, Integer.toString(relation.getLabelFromTuple(tuple)));
						} else {
							g.addEdge(sourceName, sinkName, label);
						}
					}
					
					rel.close();
				} catch(RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		
		new ReachabilitySolver().solve(flowGrammar, g);
		//System.out.println(g.toString());

		MultivalueMap<String,Edge> sortedEdges = g.getSortedEdges();
		for(String label : sortedEdges.keySet()) {
			System.out.println(label + ": " + sortedEdges.get(label).size());
		}
	}
}
