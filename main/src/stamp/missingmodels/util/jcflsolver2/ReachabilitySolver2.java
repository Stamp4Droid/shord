package stamp.missingmodels.util.jcflsolver2;

import java.util.HashSet;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.AuxProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.UnaryProduction;

public class ReachabilitySolver2 {
	private final Graph2 g;
	
	public ReachabilitySolver2(Graph2 g) {
		this.g = new Graph2(g.c);
		int i=0;
		for(Edge edge : g) {
			this.addEdgeHelper(this.g.getVertex(edge.source.name), this.g.getVertex(edge.sink.name), edge.symbolInt, edge.field, edge.weight, null, null);
			i++;
		}
		System.out.println("Initial edges 2: " + i);
	}

	private BucketHeap worklist = new BucketHeap();
	
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
		int field = unaryProduction.ignoreFields ? -1 : Math.max(input.field, -1);
		
		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, input.weight, input, null);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		int symbolInt = binaryProduction.target;
		
		// add edge
		if((firstInput.field == -1) || (secondInput.field == -1) || (firstInput.field == secondInput.field)) {
			int field = binaryProduction.ignoreFields ? -1 : Math.max(firstInput.field, secondInput.field);
			this.addEdgeHelper(source, sink, symbolInt, field, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);
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
	
	public Graph2 process() {
		System.out.println("Initial num of edges = " + this.worklist.size());
		this.time = System.currentTimeMillis();
		
		while(this.worklist.size() != 0) {
			Edge edge = this.worklist.pop();
			// <-, ->
			for(UnaryProduction unaryProduction : this.g.c.unaryProductionsByInput[edge.symbolInt]) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : this.g.c.binaryProductionsByFirstInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput) : intermediate.getOutgoingEdges(binaryProduction.secondInput);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : this.g.c.binaryProductionsBySecondInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput) : intermediate.getIncomingEdges(binaryProduction.firstInput);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
			// <- <-, <- ->, -> <-, -> ->
			for(AuxProduction auxProduction : this.g.c.auxProductionsByInput[edge.symbolInt]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> auxEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? intermediate.getOutgoingEdges(auxProduction.auxInput) : intermediate.getIncomingEdges(auxProduction.auxInput);
				for(Edge auxEdge : auxEdges) {
					this.addEdge(auxProduction, edge, auxEdge);
				}
			}
			for(AuxProduction auxProduction : this.g.c.auxProductionsByAuxInput[edge.symbolInt]) {
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
		
		return this.g;
	}
}
