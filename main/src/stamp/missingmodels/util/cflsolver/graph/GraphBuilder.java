package stamp.missingmodels.util.cflsolver.graph;

import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.Vertex;

public class GraphBuilder {
	private final Graph graph;
	
	public GraphBuilder(ContextFreeGrammar contextFreeGrammar) {
		this.graph = new Graph(contextFreeGrammar);
	}

	public Graph toGraph() {
		return this.graph;
	}
	
	public Edge addEdge(Vertex source, Vertex sink, int symbolInt, Field field, Context context, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbolInt, field, context, info);
	}

	public Edge addEdge(String source, String sink, String symbol, Field field, Context context, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, field, context, info);
	}

	public Edge addEdge(String source, String sink, String symbol, Context context, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, context, info);
	}

	public Edge addEdge(String source, String sink, String symbol, Field field, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, field, Context.DEFAULT_CONTEXT, info);
	}

	public Edge addEdge(String source, String sink, String symbol, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, Context.DEFAULT_CONTEXT, info);
	}
	
	public Edge addEdge(String source, String sink, String symbol, Field field, Context context) {
		return this.graph.addEdge(source, sink, symbol, field, context, new EdgeInfo());
	}

	public Edge addEdge(String source, String sink, String symbol, Context context) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, context, new EdgeInfo());
	}

	public Edge addEdge(String source, String sink, String symbol, Field field) {
		return this.graph.addEdge(source, sink, symbol, field, Context.DEFAULT_CONTEXT, new EdgeInfo());
	}
	
	public Edge addEdge(String source, String sink, String symbol) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, Context.DEFAULT_CONTEXT, new EdgeInfo());
	}
}
