package solver;

import graph.Edge;
import graph.Vertex;

import java.util.LinkedList;
import java.util.List;

import cfg.BinaryProduction;
import cfg.ContextFreeGrammar;
import cfg.UnaryProduction;

public class CFLSolver {
	private int getField(int firstField, int secondField) {
		if(firstField == secondField) {
			return -1;
		}
		if(firstField == -1) {
			return secondField;
		}
		if(secondField == -1) {
			return firstField;
		}
		return -2;
	}
	
	private Edge addEdge(Vertex source, Vertex sink, int label, int field) {
		Edge edge = new Edge(source, sink, label, field);
		source.outgoingEdgesByLabel[label].add(edge);
		sink.incomingEdgesByLabel[label].add(edge);
		return edge;
	}
	
	public void solve(ContextFreeGrammar c, List<Edge> e) {
		// Initialize the worklist
		LinkedList<Edge> worklist = new LinkedList<Edge>(); // list of type Edge
		for(Edge edge : e) {
			worklist.add(edge);
		}
		
		// Process edges in worklist until empty  
		while(!worklist.isEmpty()) {
			Edge edge = worklist.getFirst();
			// ->
			for(UnaryProduction unaryProduction : (List<UnaryProduction>)c.unaryProductionsByInput[edge.label]) {
				worklist.add(this.addEdge(edge.source, edge.sink, unaryProduction.target, edge.field));
			}
			for(BinaryProduction binaryProduction : (List<BinaryProduction>)c.binaryProductionsByFirstInput[edge.label]) {
				// <- <-
				// <- <-
				// <- ->
				// -> <-
				// -> ->
				if(binaryProduction.isFirstInputBackwards && binaryProduction.isSecondInputBackwards) {
					for(Edge secondEdge : (List<Edge>)edge.source.incomingEdgesByLabel[binaryProduction.secondInput]) {
						int field = this.getField(edge.field, secondEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(edge.sink, secondEdge.source, binaryProduction.target, field));
						}
					}
				} else if(binaryProduction.isFirstInputBackwards) {
					for(Edge secondEdge : (List<Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.secondInput]) {
						int field = this.getField(edge.field, secondEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(edge.sink, secondEdge.sink, binaryProduction.target, field));
						}
					}
				} else if(binaryProduction.isSecondInputBackwards) {
					for(Edge secondEdge : (List<Edge>)edge.sink.incomingEdgesByLabel[binaryProduction.secondInput]) {
						int field = this.getField(edge.field, secondEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(edge.source, secondEdge.source, binaryProduction.target, field));
						}
					}
				} else {
					for(Edge secondEdge : (List<Edge>)edge.sink.outgoingEdgesByLabel[binaryProduction.secondInput]) {
						int field = this.getField(edge.field, secondEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(edge.source, secondEdge.sink, binaryProduction.target, field));
						}
					}
				}
				
			}
			for(BinaryProduction binaryProduction : (List<BinaryProduction>)c.binaryProductionsBySecondInput[edge.label]) {
				// <- <-
				// <- ->
				// -> <-
				// -> ->
				if(binaryProduction.isFirstInputBackwards && binaryProduction.isSecondInputBackwards) {
					for(Edge firstEdge : (List<Edge>)edge.sink.outgoingEdgesByLabel[binaryProduction.firstInput]) {
						int field = this.getField(edge.field, firstEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(firstEdge.sink, edge.source, binaryProduction.target, field));
						}
					}
				} else if(binaryProduction.isFirstInputBackwards) {
					for(Edge firstEdge : (List<Edge>)edge.source.outgoingEdgesByLabel[binaryProduction.firstInput]) {
						int field = this.getField(edge.field, firstEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(firstEdge.sink, edge.sink, binaryProduction.target, field));
						}
					}
				} else if(binaryProduction.isSecondInputBackwards) {
					for(Edge firstEdge : (List<Edge>)edge.sink.incomingEdgesByLabel[binaryProduction.firstInput]) {
						int field = this.getField(edge.field, firstEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(firstEdge.source, edge.source, binaryProduction.target, field));
						}
					}
				} else {
					for(Edge firstEdge : (List<Edge>)edge.source.incomingEdgesByLabel[binaryProduction.firstInput]) {
						int field = this.getField(edge.field, firstEdge.field);
						if(field != -2) {
							worklist.add(this.addEdge(firstEdge.source, edge.sink, binaryProduction.target, field));
						}
					}
				}
			}
		}
	}
}
