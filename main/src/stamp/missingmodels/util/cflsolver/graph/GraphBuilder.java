package stamp.missingmodels.util.cflsolver.graph;

import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.Graph.Vertex;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.Symbol;

public class GraphBuilder {
	private final Graph graph;
	
	public GraphBuilder(ContextFreeGrammar contextFreeGrammar) {
		this.graph = new Graph(contextFreeGrammar);
	}

	public Graph toGraph() {
		return this.graph;
	}

	public Edge addEdge(EdgeStruct edgeStruct) {
		return this.graph.addEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, edgeStruct.context, new EdgeInfo());
	}
	
	public Edge addEdge(EdgeStruct edgeStruct, int weight) {
		return this.graph.addEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, edgeStruct.context, new EdgeInfo(weight));
	}
	
	public Edge addEdge(Vertex source, Vertex sink, Symbol symbol, Field field, Context context, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, field, context, info);
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
