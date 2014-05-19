package stamp.missingmodels.util.cflsolver.graph;

import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;

public abstract class GraphTransformer {
	public abstract void process(GraphBuilder gb, EdgeStruct edgeStruct, int weight);
	
	public Graph transform(Graph graph) {
		GraphBuilder gb = new GraphBuilder(graph.getContextFreeGrammar());
		for(Edge edge : graph.getEdges()) {
			process(gb, edge.getStruct(), edge.getInfo().weight);
		}
		return gb.toGraph();
	}
}
