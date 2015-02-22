package stamp.missingmodels.util.jcflsolver2;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.AuxProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeStruct;
import stamp.missingmodels.util.jcflsolver2.Graph2.GraphBuilder;
import stamp.missingmodels.util.jcflsolver2.Graph2.GraphTransformer;

public class ReachabilitySolver2 implements GraphTransformer {
	private GraphBuilder g;
	private BucketHeap worklist;
	
	private int count;
	private long time;
	
	public void addEdgeHelper(Vertex source, Vertex sink, int symbolInt, int field, short weight, Edge firstInput, Edge secondInput) {
		if(this.g.addEdge(source, sink, symbolInt, field, weight, firstInput, secondInput, this.worklist)) {
			this.count++;
		}
	}

	private void addEdge(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = unaryProduction.target;
		
		// get field
		int field = unaryProduction.ignoreFields ? -1 : input.field;
		
		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, input.weight, input, null);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		int symbolInt = binaryProduction.target;
		
		// add edge
		if(binaryProduction.ignoreFields || (firstInput.field == secondInput.field)) {
			this.addEdgeHelper(source, sink, symbolInt, -1, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);			
		} else if(firstInput.field == -1) {
			this.addEdgeHelper(source, sink, symbolInt, secondInput.field, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);						
		} else if(secondInput.field == -1) {
			this.addEdgeHelper(source, sink, symbolInt, firstInput.field, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);									
		}
	}

	private void addEdge(AuxProduction auxProduction, Edge input, Edge auxInput) {
		// get edge base
		Vertex source = auxProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = auxProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = auxProduction.target;
		
		// add edge
		if((input.field == -1) || (auxInput.field == -1) || (input.field == auxInput.field)) {
			int field = auxProduction.ignoreFields ? -1 : Math.max(input.field, auxInput.field);
			this.addEdgeHelper(source, sink, symbolInt, field, (short)(input.weight + auxInput.weight), input, auxInput);
		}
	}
	
	public Graph2 transform(ContextFreeGrammarOpt c, Iterable<EdgeStruct> edges) {
		this.g = new GraphBuilder(c);
		this.worklist = new BucketHeap();
		this.time = System.currentTimeMillis();
		this.count = 0;
		
		for(EdgeStruct edge : edges) {
			this.addEdgeHelper(this.g.getVertex(edge.sourceName), this.g.getVertex(edge.sinkName), c.getSymbolInt(edge.symbol), edge.field, edge.weight, null, null);
		}
		
		System.out.println("Initial num of edges = " + this.worklist.size());
		this.time = System.currentTimeMillis();
		
		while(this.worklist.size() != 0) {
			Edge edge = this.worklist.pop();
			// <-, ->
			for(UnaryProduction unaryProduction : c.unaryProductionsByInput[edge.symbolInt]) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : c.binaryProductionsByFirstInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput) : intermediate.getOutgoingEdges(binaryProduction.secondInput);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : c.binaryProductionsBySecondInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput) : intermediate.getIncomingEdges(binaryProduction.firstInput);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
			// <- <-, <- ->, -> <-, -> ->
			for(AuxProduction auxProduction : c.auxProductionsByInput[edge.symbolInt]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> auxEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? intermediate.getOutgoingEdges(auxProduction.auxInput) : intermediate.getIncomingEdges(auxProduction.auxInput);
				for(Edge auxEdge : auxEdges) {
					this.addEdge(auxProduction, edge, auxEdge);
				}
			}
			for(AuxProduction auxProduction : c.auxProductionsByAuxInput[edge.symbolInt]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> inputEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? intermediate.getIncomingEdges(auxProduction.input) : intermediate.getOutgoingEdges(auxProduction.input);
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
