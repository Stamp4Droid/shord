package stamp.missingmodels.util.cflsolver.core;

import stamp.missingmodels.util.cflsolver.core.Edge.EdgeList;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeSet;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;

public class Vertex {
	private final EdgeList[] incomingEdges;
	private final EdgeSet[] outgoingEdges;
	public final String name;
	public final int id;

	public Vertex(int id, String name, int numSymbols) {
		this.id = id;
		this.incomingEdges = new EdgeList[numSymbols];
		this.outgoingEdges = new EdgeSet[numSymbols];
		this.name = name;
	}
	
	public Iterable<Edge> getOutgoingEdges(int symbolInt) {
		EdgeSet edges = outgoingEdges[symbolInt];
		return edges == null ? Edge.EMPTY_EDGES : edges;
	}
	
	public Iterable<Edge> getIncomingEdges(int symbolInt) {
		EdgeList edges = incomingEdges[symbolInt];
		return edges == null ? Edge.EMPTY_EDGES : edges;
	}
	
	public Edge getCurrentOutgoingEdge(Edge edge) {
		EdgeSet edges = this.outgoingEdges[edge.symbol.id];
		return edges == null ? null : edges.get(edge);
	}
 
	public void addOutgoingEdge(Edge edge) {
		int symbolInt = edge.symbol.id;
		EdgeSet edges = this.outgoingEdges[symbolInt];
		if(edges == null) {
			edges = new EdgeSet();
			this.outgoingEdges[symbolInt] = edges;
		}
		edges.add(edge);
	}
	
	public void addIncomingEdge(Edge edge) {
		int symbolInt = edge.symbol.id;
		EdgeList edges = this.incomingEdges[symbolInt];
		if(edges == null) {
			edges = new EdgeList();
			this.incomingEdges[symbolInt] = edges;
		}
		edges.add(edge);
	}
	
	public String toString(boolean shord) {
		if(shord) {
			return ConversionUtils.toStringShord(this.name);
		} else {
			return this.name;
		}
	}
	
	@Override
	public String toString() {
		return this.toString(false);
	}
}