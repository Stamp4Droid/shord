package stamp.missingmodels.util.cflsolver.graph;

import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.ObjectContext;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.Graph.Vertex;

public class GraphBuilder {
	private final Graph graph;
	
	public GraphBuilder(ContextFreeGrammar contextFreeGrammar) {
		this.graph = new Graph(contextFreeGrammar);
	}

	public Graph toGraph() {
		return this.graph;
	}

	public Edge addEdge(EdgeStruct edgeStruct) {
		return this.graph.addEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, edgeStruct.context, edgeStruct.objectContext, new EdgeInfo());
	}
	
	public Edge addEdge(EdgeStruct edgeStruct, int weight) {
		return this.graph.addEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, edgeStruct.context, edgeStruct.objectContext, new EdgeInfo(weight));
	}
	
	public Edge addEdge(Vertex source, Vertex sink, int symbolInt, Field field, Context context, ObjectContext objectContext, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbolInt, field, context, objectContext, info);
	}

	public Edge addEdge(String source, String sink, String symbol, Field field, Context context, ObjectContext objectContext, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, field, context, objectContext, info);
	}

	public Edge addEdge(String source, String sink, String symbol, Context context, ObjectContext objectContext, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, context, objectContext, info);
	}

	public Edge addEdge(String source, String sink, String symbol, Field field, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, field, Context.DEFAULT_CONTEXT, ObjectContext.DEFAULT_OBJECT_CONTEXT, info);
	}

	public Edge addEdge(String source, String sink, String symbol, EdgeInfo info) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, Context.DEFAULT_CONTEXT, ObjectContext.DEFAULT_OBJECT_CONTEXT, info);
	}
	
	public Edge addEdge(String source, String sink, String symbol, Field field, Context context, ObjectContext objectContext) {
		return this.graph.addEdge(source, sink, symbol, field, context, objectContext, new EdgeInfo());
	}

	public Edge addEdge(String source, String sink, String symbol, Context context, ObjectContext objectContext) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, context, objectContext, new EdgeInfo());
	}

	public Edge addEdge(String source, String sink, String symbol, Field field) {
		return this.graph.addEdge(source, sink, symbol, field, Context.DEFAULT_CONTEXT, ObjectContext.DEFAULT_OBJECT_CONTEXT, new EdgeInfo());
	}
	
	public Edge addEdge(String source, String sink, String symbol) {
		return this.graph.addEdge(source, sink, symbol, Field.DEFAULT_FIELD, Context.DEFAULT_CONTEXT, ObjectContext.DEFAULT_OBJECT_CONTEXT, new EdgeInfo());
	}
}
