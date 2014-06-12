package stamp.missingmodels.util.cflsolver.solver;

import java.util.Collection;

import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.ObjectContext;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.Vertex;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;

public class ReachabilitySolver {
	private final ContextFreeGrammar c;
	private final GraphBuilder gb;
	private final Heap<Edge> worklist = new BucketHeap<Edge>();
	private final TypeFilter t;
	
	public static class TypeFilter {
		private final MultivalueMap<String,String> filter = new MultivalueMap<String,String>();
		private final int flowSymbolInt;
		
		public TypeFilter(ContextFreeGrammar c) {
			this.flowSymbolInt = c.getSymbolInt("Flow");
		}
		
		public void add(String h, String v) {
			this.filter.add(h, v);
		}
		
		public boolean filter(Vertex source, Vertex sink, int symbolInt) {
			return symbolInt != this.flowSymbolInt || this.filter.get(source.name).contains(sink.name);
		}
	}
	
	public ReachabilitySolver(Graph g, TypeFilter t) {
		this.c = g.getContextFreeGrammar();
		this.gb = new GraphBuilder(this.c);
		this.t = t;
		for(Edge edge : g.getEdges()) {
			Edge newEdge = this.gb.addEdge(edge.source.name, edge.sink.name, edge.getSymbol(), edge.field, edge.context, edge.objectContext, new EdgeInfo(edge.getInfo().weight));
			this.worklist.add(newEdge, edge.getInfo().weight);
		}
	}
	
	private void addEdgeHelper(Vertex source, Vertex sink, int symbolInt, Field field, Context context, ObjectContext objectContext, EdgeInfo newInfo) {
		// make sure field and context are not null
		if(field == null || context == null || objectContext == null) {
			return;
		}
		
		// check if edge exists
		EdgeInfo curInfo = this.gb.toGraph().getInfo(source, sink, symbolInt, field, context, objectContext);
		
		// add the edge if the edge is new or if the weight is smaller
		if(curInfo == null || newInfo.weight < curInfo.weight) {
			this.worklist.add(this.gb.addEdge(source, sink, symbolInt, field, context, objectContext, newInfo), newInfo.weight);
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
		Field field = input.field.produce(unaryProduction);
		Context context = input.context.produce(unaryProduction);
		ObjectContext objectContext = input.objectContext.produce(unaryProduction);
		
		// get edge info
		EdgeInfo newInfo = new EdgeInfo(input, input.getInfo().weight);	
		
		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, context, objectContext, newInfo);
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
		Field field = firstInput.field.produce(binaryProduction, secondInput.field);
		Context context = firstInput.context.produce(binaryProduction, secondInput.context);
		ObjectContext objectContext = firstInput.objectContext.produce(binaryProduction, secondInput.objectContext);
		
		// get edge info
		EdgeInfo newInfo = new EdgeInfo(firstInput, secondInput, firstInput.getInfo().weight + secondInput.getInfo().weight);

		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, context, objectContext, newInfo);
	}

	private void addEdge(AuxProduction auxProduction, Edge input, Edge auxInput) {
		// get edge base
		Vertex source = auxProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = auxProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = auxProduction.target;
		
		// check filter
		if(!this.t.filter(source, sink, symbolInt)) {
			return;
		}
		
		// get edge data
		Field field = input.field.produce(auxProduction, auxInput.field);
		Context context = input.context.produce(auxProduction, auxInput.context);
		ObjectContext objectContext = input.objectContext.produce(auxProduction, auxInput.objectContext);
		
		// get edge info
		EdgeInfo newInfo = new EdgeInfo(input, auxInput, input.getInfo().weight + auxInput.getInfo().weight);

		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, context, objectContext, newInfo);
	}
	
	private void solve() {
		System.out.println("Computing transitive closure...");
		long time = System.currentTimeMillis();
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
			// <- <-, <- ->, -> <-, -> ->
			for(AuxProduction auxProduction : this.c.auxProductionsByInput.get(edge.symbolInt)) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? edge.sink : edge.source;
				Collection<Edge> auxEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? intermediate.getOutgoingEdges(auxProduction.auxInput) : intermediate.getIncomingEdges(auxProduction.auxInput);
				for(Edge auxEdge : auxEdges) {
					this.addEdge(auxProduction, edge, auxEdge);
				}
			}
			for(AuxProduction auxProduction : this.c.auxProductionsByAuxInput.get(edge.symbolInt)) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? edge.source : edge.sink;
				Collection<Edge> inputEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? intermediate.getIncomingEdges(auxProduction.input) : intermediate.getOutgoingEdges(auxProduction.input);
				for(Edge inputEdge : inputEdges) {
					this.addEdge(auxProduction, inputEdge, edge);
				}
			}
		}
		System.out.println("Done computing transitive closure in: " + (System.currentTimeMillis() - time) + "ms");
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
