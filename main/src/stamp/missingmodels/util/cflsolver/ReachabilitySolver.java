package stamp.missingmodels.util.cflsolver;

import java.util.List;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.Graph.Edge;
import stamp.missingmodels.util.cflsolver.Graph.Vertex;

public class ReachabilitySolver {
	private int getField(int field, boolean ignoreFields) {
		return ignoreFields ? Graph.DEFAULT_FIELD : field;
	}
	
	private int getField(int firstField, int secondField, boolean ignoreFields) {
		if(ignoreFields) {
			return Graph.DEFAULT_FIELD;
		}
		if(firstField == secondField) {
			return Graph.DEFAULT_FIELD;
		}
		if(firstField == Graph.DEFAULT_FIELD) {
			return secondField;
		}
		if(secondField == Graph.DEFAULT_FIELD) {
			return firstField;
		}
		return Graph.MISMATCHED_FIELD;
	}
	
	private void getEdge(Graph graph, Vertex source, Vertex sink, int label, int field, Edge firstInput, Edge secondInput, short weight, Heap<Edge> worklist) {
		if(field == Graph.MISMATCHED_FIELD) {
			return;
		}
		Edge edge = graph.new Edge(source, sink, label, field, firstInput, secondInput, weight);
		Edge curEdge = graph.getEdge(edge);
		if(curEdge == null) {
			graph.addEdge(edge);
			worklist.add(edge, edge.weight);
		} else if(curEdge.weight > weight) {
			worklist.remove(curEdge, curEdge.weight);
			curEdge.firstInput = firstInput;
			curEdge.secondInput = secondInput;
			curEdge.weight = weight;
			worklist.add(curEdge, curEdge.weight);
		}
	}
	
	public void solve(ContextFreeGrammar c, Graph g) {
		this.solve(c, g, new BucketHeap<Edge>());
	}
	
	public void solve(ContextFreeGrammar c, Graph g, Heap<Edge> worklist) {
		// Initialize the worklist
		//LinkedList<Edge> worklist = new LinkedList<Edge>(); // list of type Edge
		for(Edge edge : g.getEdges()) {
			worklist.add(edge, edge.weight);
		}
		
		// Process edges in worklist until empty  
		while(!(worklist.size() == 0)) {
			Edge edge = worklist.pop();
			// <-
			// ->
			for(UnaryProduction unaryProduction : (List<UnaryProduction>)c.unaryProductionsByInput.get(edge.label)) {
				if(unaryProduction.isInputBackwards) {
					this.getEdge(g, edge.sink, edge.source, unaryProduction.target, this.getField(edge.field, unaryProduction.ignoreFields), edge, null, edge.weight, worklist);
				} else {
					this.getEdge(g, edge.source, edge.sink, unaryProduction.target, this.getField(edge.field, unaryProduction.ignoreFields), edge, null, edge.weight,worklist);
				}
			}
			for(BinaryProduction binaryProduction : (List<BinaryProduction>)c.binaryProductionsByFirstInput.get(edge.label)) {
				// <- <-
				// <- ->
				// -> <-
				// -> ->
				if(binaryProduction.isFirstInputBackwards && binaryProduction.isSecondInputBackwards) {
					for(Edge secondEdge : (Set<Edge>)edge.source.incomingEdgesByLabel[binaryProduction.secondInput].keySet()) {
						this.getEdge(g, edge.sink, secondEdge.source, binaryProduction.target, this.getField(edge.field, secondEdge.field, binaryProduction.ignoreFields), edge, secondEdge, (short)(edge.weight+secondEdge.weight), worklist);
					}
				} else if(binaryProduction.isFirstInputBackwards) {
					for(Edge secondEdge : (Set<Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.secondInput].keySet()) {
						this.getEdge(g, edge.sink, secondEdge.sink, binaryProduction.target, this.getField(edge.field, secondEdge.field, binaryProduction.ignoreFields), edge, secondEdge, (short)(edge.weight+secondEdge.weight), worklist);
					}
				} else if(binaryProduction.isSecondInputBackwards) {
					for(Edge secondEdge : (Set<Edge>)edge.sink.incomingEdgesByLabel[binaryProduction.secondInput].keySet()) {
						this.getEdge(g, edge.source, secondEdge.source, binaryProduction.target, this.getField(edge.field, secondEdge.field, binaryProduction.ignoreFields), edge, secondEdge, (short)(edge.weight+secondEdge.weight), worklist);
					}
				} else {
					for(Edge secondEdge : (Set<Edge>)edge.sink.outgoingEdgesByLabel[binaryProduction.secondInput].keySet()) {
						this.getEdge(g, edge.source, secondEdge.sink, binaryProduction.target, this.getField(edge.field, secondEdge.field, binaryProduction.ignoreFields), edge, secondEdge, (short)(edge.weight+secondEdge.weight), worklist);
					}
				}
				
			}
			for(BinaryProduction binaryProduction : (List<BinaryProduction>)c.binaryProductionsBySecondInput.get(edge.label)) {
				// <- <-
				// <- ->
				// -> <-
				// -> ->
				if(binaryProduction.isFirstInputBackwards && binaryProduction.isSecondInputBackwards) {
					for(Edge firstEdge : (Set<Edge>)edge.sink.outgoingEdgesByLabel[binaryProduction.firstInput].keySet()) {
						this.getEdge(g, firstEdge.sink, edge.source, binaryProduction.target, this.getField(edge.field, firstEdge.field, binaryProduction.ignoreFields), firstEdge, edge, (short)(edge.weight+firstEdge.weight), worklist);
					}
				} else if(binaryProduction.isFirstInputBackwards) {
					for(Edge firstEdge : (Set<Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.firstInput].keySet()) {
						this.getEdge(g, firstEdge.sink, edge.sink, binaryProduction.target, this.getField(edge.field, firstEdge.field, binaryProduction.ignoreFields), firstEdge, edge, (short)(edge.weight+firstEdge.weight), worklist);
					}
				} else if(binaryProduction.isSecondInputBackwards) {
					for(Edge firstEdge : (Set<Edge>)edge.sink.incomingEdgesByLabel[binaryProduction.firstInput].keySet()) {
						this.getEdge(g, firstEdge.source, edge.source, binaryProduction.target, this.getField(edge.field, firstEdge.field, binaryProduction.ignoreFields), firstEdge, edge, (short)(edge.weight+firstEdge.weight), worklist);
					}
				} else {
					for(Edge firstEdge : (Set<Edge>)edge.source.incomingEdgesByLabel[binaryProduction.firstInput].keySet()) {
						this.getEdge(g, firstEdge.source, edge.sink, binaryProduction.target, this.getField(edge.field, firstEdge.field, binaryProduction.ignoreFields), firstEdge, edge, (short)(edge.weight+firstEdge.weight), worklist);
					}
				}
			}
		}
	}
}
