package stamp.missingmodels.util.cflsolver.solver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.Vertex;

public class ReachabilitySolver {
	private final ContextFreeGrammar c;
	private final GraphBuilder gb;
	private final LinkedList<Edge> worklist = new LinkedList<Edge>();
	
	public ReachabilitySolver(Graph g) {
		this.c = g.getContextFreeGrammar();
		this.gb = new GraphBuilder(this.c);
		for(Edge edge : g.getEdges()) {
			Edge newEdge = this.gb.addEdge(edge.source.name, edge.sink.name, edge.getSymbol(), edge.field, edge.context, new EdgeInfo(edge.getInfo().weight));
			this.worklist.add(newEdge);
		}
	}
	
	private void addEdgeHelper(Vertex source, Vertex sink, int symbolInt, Field field, Context context, EdgeInfo newInfo) {
		// make sure field and context are not null
		if(field == null || context == null) {
			return;
		}
		
		// check if edge exists
		EdgeInfo curInfo = this.gb.toGraph().getInfo(source, sink, symbolInt, field, context);
		
		// add the edge if the edge is new or if the weight is smaller
		if(curInfo == null) {
			this.worklist.add(this.gb.addEdge(source, sink, symbolInt, field, context, newInfo));
			//this.worklist.add(this.gb.addEdge(source, sink, symbolInt, data, newInfo), newData.weight);
		} else if(curInfo.weight > newInfo.weight) {
			this.gb.addEdge(source, sink, symbolInt, field, context, newInfo);
			//this.worklist.update(this.gb.addEdge(source, sink, symbolInt, data, newInfo), newData.weight);
		}		
	}
	
	private void addEdge(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = unaryProduction.target;
		
		// get edge data
		Field field = input.field.produce(unaryProduction);
		Context context = input.context.produce(unaryProduction);
		
		// get edge info
		EdgeInfo newInfo = new EdgeInfo(input, input.getInfo().weight);	
		
		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, context, newInfo);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		int symbolInt = binaryProduction.target;
		
		// get edge data
		Field field = firstInput.field.produce(binaryProduction, secondInput.field);
		Context context = firstInput.context.produce(binaryProduction, secondInput.context);
		
		// get edge info
		EdgeInfo newInfo = new EdgeInfo(firstInput, secondInput, firstInput.getInfo().weight + secondInput.getInfo().weight);

		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, context, newInfo);
	}
	
	private void solve() {
		while(!this.worklist.isEmpty()) {
			Edge edge = this.worklist.removeFirst();
			// <-, ->
			for(UnaryProduction unaryProduction : this.c.unaryProductionsByInput.get(edge.symbolInt)) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : this.c.binaryProductionsByFirstInput.get(edge.symbolInt)) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Collection<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput) : intermediate.getOutgoingEdges(binaryProduction.secondInput);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : this.c.binaryProductionsBySecondInput.get(edge.symbolInt)) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Collection<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput) : intermediate.getIncomingEdges(binaryProduction.firstInput);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
		}
	}
	
	private boolean solved = false;	
	public Graph getResult() {
		if(!this.solved) {
			this.solve();
			this.solved = true;
		}
		return this.gb.toGraph();
	}
}
