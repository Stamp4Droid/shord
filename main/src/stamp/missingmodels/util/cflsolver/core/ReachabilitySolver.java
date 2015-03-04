package stamp.missingmodels.util.cflsolver.core;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.Symbol;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;
import stamp.missingmodels.util.cflsolver.core.Graph.Filter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.VertexMap;

public class ReachabilitySolver implements GraphTransformer {
	private final ContextFreeGrammarOpt contextFreeGrammar;
	private final VertexMap vertices;
	private final Filter<Edge> filter;
	
	private GraphBuilder graph;
	private BucketHeap worklist;
	
	public ReachabilitySolver(VertexMap vertices, ContextFreeGrammarOpt contextFreeGrammar, Filter<Edge> filter) {
		this.vertices = vertices;
		this.contextFreeGrammar = contextFreeGrammar;
		this.filter = filter;
	}
	
	public ReachabilitySolver(VertexMap vertices, ContextFreeGrammarOpt contextFreeGrammar) {
		this(vertices, contextFreeGrammar, new Filter<Edge>() {
			@Override
			public boolean filter(Edge edge) { return true; }});
	}
	
	private void addEdgeHelper(Vertex source, Vertex sink, Symbol symbol, Field field, short weight, Edge firstInput, Edge secondInput) {
		if(field == null) {
			return;
		}
		Edge curEdge = this.graph.getEdge(source, sink, symbol, field);
		Edge edge = this.graph.addOrUpdateEdge(source, sink, symbol, field, weight, firstInput, secondInput);
		if(edge != null && !this.filter.filter(edge)) {
			return;
		}
		if(curEdge == null) {
			this.worklist.push(edge);
		} else if(edge != null) {
			this.worklist.update(edge);
		}
	}

	private void addEdge(UnaryProduction unaryProduction, Edge input) {
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		Symbol symbol = unaryProduction.target;
		Field field = Field.produce(unaryProduction.ignoreFields, input.field);
		this.addEdgeHelper(source, sink, symbol, field, input.weight, input, null);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		Symbol symbol = binaryProduction.target;
		Field field = Field.produce(binaryProduction.ignoreFields, firstInput.field, secondInput.field);
		this.addEdgeHelper(source, sink, symbol, field, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);						
	}

	private void addEdge(AuxProduction auxProduction, Edge input, Edge auxInput) {
		Vertex source = auxProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = auxProduction.isInputBackwards ? input.source : input.sink;
		Symbol symbol = auxProduction.target;
		Field field = Field.produce(auxProduction.ignoreFields, input.field, auxInput.field);
		this.addEdgeHelper(source, sink, symbol, field, (short)(input.weight + auxInput.weight), input, auxInput);
	}
	
	@Override
	public Graph transform(Iterable<EdgeStruct> edges) {
		long time = System.currentTimeMillis();
		
		this.graph = new GraphBuilder(this.vertices, this.contextFreeGrammar.getSymbols());
		this.worklist = new BucketHeap();
		
		for(EdgeStruct edge : edges) {
			this.addEdgeHelper(this.graph.getVertex(edge.sourceName), this.graph.getVertex(edge.sinkName), this.contextFreeGrammar.getSymbols().get(edge.symbol), Field.getField(edge.field), edge.weight, null, null);
		}
		
		System.out.println("Initial num of edges = " + this.worklist.size());
		
		while(this.worklist.size()>0) {
			Edge edge = this.worklist.pop();
			// <-, ->
			for(UnaryProduction unaryProduction : this.contextFreeGrammar.unaryProductionsByInput[edge.symbol.id]) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : this.contextFreeGrammar.binaryProductionsByFirstInput[edge.symbol.id]) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput.id) : intermediate.getOutgoingEdges(binaryProduction.secondInput.id);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : this.contextFreeGrammar.binaryProductionsBySecondInput[edge.symbol.id]) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput.id) : intermediate.getIncomingEdges(binaryProduction.firstInput.id);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
			// <- <-, <- ->, -> <-, -> ->
			for(AuxProduction auxProduction : this.contextFreeGrammar.auxProductionsByInput[edge.symbol.id]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> auxEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? intermediate.getOutgoingEdges(auxProduction.auxInput.id) : intermediate.getIncomingEdges(auxProduction.auxInput.id);
				for(Edge auxEdge : auxEdges) {
					this.addEdge(auxProduction, edge, auxEdge);
				}
			}
			for(AuxProduction auxProduction : this.contextFreeGrammar.auxProductionsByAuxInput[edge.symbol.id]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> inputEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? intermediate.getIncomingEdges(auxProduction.input.id) : intermediate.getOutgoingEdges(auxProduction.input.id);
				for(Edge inputEdge : inputEdges) {
					this.addEdge(auxProduction, inputEdge, edge);
				}
			}
		}

		long totalTime = System.currentTimeMillis() - time;
		System.out.println("Time: " + totalTime);
		System.out.println("Number of edges: " + this.graph.getGraph().getNumEdges());
		System.out.println("Rate: " + (double)this.graph.getGraph().getNumEdges()/totalTime);
		
		return this.graph.getGraph();
	}
}
