package stamp.missingmodels.util.cflsolver.solver;

import java.util.LinkedList;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph2;
import stamp.missingmodels.util.cflsolver.graph.Graph2.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph2.Vertex;

public class ReachabilitySolver2 {
	private final ContextFreeGrammarOpt c;
	private final Graph2 gb;
	private final LinkedList<Edge> worklist = new LinkedList<Edge>();
	private final TypeFilter t;
	
	public static class TypeFilter {
		private final ReachabilitySolver.TypeFilter t;
		
		public TypeFilter(ReachabilitySolver.TypeFilter t) {
			this.t = t;
		}
		
		public boolean filter(Vertex source, Vertex sink, int symbolInt) {
			return symbolInt != this.t.flowSymbolInt || this.t.filter.get(source.name).contains(sink.name);
		}
	}
	
	public ReachabilitySolver2(Graph g, ReachabilitySolver.TypeFilter t) {
		this.c = new ContextFreeGrammarOpt(g.getContextFreeGrammar());
		this.gb = new Graph2(this.c);
		this.t = new TypeFilter(t);
		for(Graph.Edge edge : g.getEdges()) {
			Edge newEdge = this.gb.addEdge(edge.source.name, edge.sink.name, edge.getSymbol(), edge.field.field);
			this.worklist.add(newEdge);
		}
	}
	
	private void addEdgeHelper(Vertex source, Vertex sink, int symbolInt, int field) {
		// add the edge if the edge is new or if the weight is smaller
		if(field != -2 && !this.gb.containsEdge(source, sink, symbolInt, field)) {
			this.worklist.add(this.gb.addEdge(source, sink, symbolInt, field));
		}
	}
	
	public static int produce(UnaryProduction unaryProduction, int field) {
		return unaryProduction.ignoreFields ? -1 : field;
	}

	public static int produce(BinaryProduction binaryProduction, int firstField, int secondField) {
		if(binaryProduction.ignoreFields) {
			return -1;
		} else if(firstField == secondField) {
			return -1;
		} else if(firstField == -1) {
			return secondField;
		} else if(secondField == -1) {
			return firstField;
		} else {
			return -2;
		}
	}
	
	private void addEdge(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = unaryProduction.target;
		
		// check filter
		if(!this.t.filter(source, sink, symbolInt)) {
			return;
		}
		
		// get edge data
		int field = produce(unaryProduction, input.field);
		
		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		int symbolInt = binaryProduction.target;
		
		// check filter
		if(!this.t.filter(source, sink, symbolInt)) {
			return;
		}
		
		// get edge data
		int field = produce(binaryProduction, firstInput.field, secondInput.field);
		
		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field);
	}
	
	private void solve() {
		System.out.println("Computing transitive closure...");
		long time = System.currentTimeMillis();
		int i = 0;
		while(!this.worklist.isEmpty()) {
			Edge edge = this.worklist.removeFirst();
			i++;
			// <-, ->
			for(UnaryProduction unaryProduction : this.c.unaryProductionsByInput[edge.symbolInt]) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : this.c.binaryProductionsByFirstInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput) : intermediate.getOutgoingEdges(binaryProduction.secondInput);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : this.c.binaryProductionsBySecondInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput) : intermediate.getIncomingEdges(binaryProduction.firstInput);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
		}
		long totalTime = System.currentTimeMillis() - time;
		System.out.println("Done computing transitive closure in: " + totalTime + "ms");
		System.out.println("Num productions: " + i);
		System.out.println("Rate: " + ((double)i/totalTime) + " edges/ms");
	}
	
	private boolean solved = false;	
	public Graph2 getResult() {
		if(!this.solved) {
			this.solve();
			this.solved = true;
		}
		return this.gb;
	}
}
