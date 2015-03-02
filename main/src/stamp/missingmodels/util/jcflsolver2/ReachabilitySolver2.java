package stamp.missingmodels.util.jcflsolver2;

import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.Symbol;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeStruct;
import stamp.missingmodels.util.jcflsolver2.Edge.Field;
import stamp.missingmodels.util.jcflsolver2.Graph2.GraphBuilder;
import stamp.missingmodels.util.jcflsolver2.Graph2.GraphTransformer;
import stamp.missingmodels.util.jcflsolver2.TypeFilter.GraphTypeFilter;

public class ReachabilitySolver2 implements GraphTransformer {
	private GraphBuilder g;
	private BucketHeap worklist;
	private TypeFilter filter;
	
	private int count;
	private long time;
	
	private void addEdgeHelper(Vertex source, Vertex sink, Symbol symbol, Field field, short weight, Edge firstInput, Edge secondInput) {
		if(this.filter != null && !this.filter.filter(symbol, source, sink)) {
			return;
		}
		if(this.g.addEdge(source, sink, symbol, field, weight, firstInput, secondInput, this.worklist)) {
			this.count++;
		}
	}

	private void addEdge(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		Symbol symbol = unaryProduction.target;
		
		// get field
		Field field = Field.produce(unaryProduction.ignoreFields, input.field);
		
		// add edge
		this.addEdgeHelper(source, sink, symbol, field, input.weight, input, null);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		Symbol symbol = binaryProduction.target;
		
		// get field
		Field field = Field.produce(binaryProduction.ignoreFields, firstInput.field, secondInput.field);
		
		// add edge
		if(field != null) {
			this.addEdgeHelper(source, sink, symbol, field, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);						
		}
	}

	private void addEdge(AuxProduction auxProduction, Edge input, Edge auxInput) {
		// get edge base
		Vertex source = auxProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = auxProduction.isInputBackwards ? input.source : input.sink;
		Symbol symbol = auxProduction.target;
		
		// get field
		Field field = Field.produce(auxProduction.ignoreFields, input.field, auxInput.field);
		
		// add edge
		if(field != null) {
			this.addEdgeHelper(source, sink, symbol, field, (short)(input.weight + auxInput.weight), input, auxInput);
		}
	}
	
	public Graph2 transform(ContextFreeGrammarOpt c, Iterable<EdgeStruct> edges) {
		this.g = new GraphBuilder(c);
		this.worklist = new BucketHeap();
		
		this.time = System.currentTimeMillis();
		this.count = 0;
		
		for(EdgeStruct edge : edges) {
			this.addEdgeHelper(this.g.getVertex(edge.sourceName), this.g.getVertex(edge.sinkName), c.getSymbol(edge.symbol), Field.getField(edge.field), edge.weight, null, null);
		}
		
		// initialize filter after adding edges so graph vertices are initialized
		this.filter = new GraphTypeFilter(this.g.getGraph());
		
		System.out.println("Initial num of edges = " + this.worklist.size());
		this.time = System.currentTimeMillis();
		
		while(this.worklist.size() != 0) {
			Edge edge = this.worklist.pop();
			// <-, ->
			for(UnaryProduction unaryProduction : c.unaryProductionsByInput[edge.symbol.id]) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : c.binaryProductionsByFirstInput[edge.symbol.id]) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput.id) : intermediate.getOutgoingEdges(binaryProduction.secondInput.id);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : c.binaryProductionsBySecondInput[edge.symbol.id]) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput.id) : intermediate.getIncomingEdges(binaryProduction.firstInput.id);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
			// <- <-, <- ->, -> <-, -> ->
			for(AuxProduction auxProduction : c.auxProductionsByInput[edge.symbol.id]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> auxEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? intermediate.getOutgoingEdges(auxProduction.auxInput.id) : intermediate.getIncomingEdges(auxProduction.auxInput.id);
				for(Edge auxEdge : auxEdges) {
					this.addEdge(auxProduction, edge, auxEdge);
				}
			}
			for(AuxProduction auxProduction : c.auxProductionsByAuxInput[edge.symbol.id]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> inputEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? intermediate.getIncomingEdges(auxProduction.input.id) : intermediate.getOutgoingEdges(auxProduction.input.id);
				for(Edge inputEdge : inputEdges) {
					this.addEdge(auxProduction, inputEdge, edge);
				}
			}
		}

		long totalTime = System.currentTimeMillis() - this.time;
		System.out.println("Total num of edges = " + count);
		System.out.println("Time: " + totalTime);
		System.out.println("Rate: " + ((double)count/totalTime) + " edges/ms");
		
		return this.g.getGraph();
	}
}
