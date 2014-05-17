package stamp.missingmodels.util.cflsolver.graph;

import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;

public abstract class GraphTransformer {
	public abstract void process(GraphBuilder gb, Edge edge);
	
	public Graph transform(Graph graph) {
		GraphBuilder gb = new GraphBuilder(graph.getContextFreeGrammar());
		for(Edge edge : graph.getEdges()) {
			process(gb, edge);
		}
		return gb.toGraph();
	}
}
