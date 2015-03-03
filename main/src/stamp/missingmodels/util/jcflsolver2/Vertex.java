package stamp.missingmodels.util.jcflsolver2;

import stamp.missingmodels.util.cflsolver.util.ConversionUtils;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeList;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeSet;

public class Vertex {
	private final EdgeList[] incomingEdges;
	private final EdgeSet[] outgoingEdges;
	public final String name;
	public final int id;

	private static int numVertices = 0;

	public Vertex(String name, int numSymbols) {
		this.incomingEdges = new EdgeList[numSymbols];
		this.outgoingEdges = new EdgeSet[numSymbols];
		this.name = name;
		this.id = ++numVertices;
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