package stamp.missingmodels.util.jcflsolver2;

import stamp.missingmodels.util.jcflsolver2.EdgeCollection.EdgeList;

public class Vertex {
	final EdgeList[] incomingEdges;
	final EdgeSet[] outgoingEdges;
	final int id;

	private static int numVertices = 0;

	public Vertex(int numSymbols) {
		this.incomingEdges = new EdgeList[numSymbols];
		this.outgoingEdges = new EdgeSet[numSymbols];
		this.id = ++numVertices;
	}
	
	public EdgeCollection getOutgoingEdges(int kind) {
		EdgeCollection edges = outgoingEdges[kind];
		return edges == null ? EdgeCollection.EMPTY_EDGES : edges;
	}
	
	public EdgeCollection getIncomingEdges(int kind) {
		EdgeCollection edges = incomingEdges[kind];
		return edges == null ? EdgeCollection.EMPTY_EDGES : edges;
	}
	
	public Edge getOutgoingEdge(Edge edge) {
		int symbol = edge.symbolInt;
		EdgeSet edges = this.outgoingEdges[symbol];
		return edges == null ? null : edges.get(edge);
	}
 
	public void addOutgoingEdge(Edge edge) {
		int symbol = edge.symbolInt;
		EdgeSet edges = this.outgoingEdges[symbol];
		if(edges == null) {
			edges = new EdgeSet();
			this.outgoingEdges[symbol] = edges;
		}
		edges.add(edge);
	}
	
	public void addIncomingEdge(Edge edge) {
		int kind = edge.symbolInt;
		EdgeList edges = this.incomingEdges[kind];
		if(edges == null) {
			edges = new EdgeList();
			this.incomingEdges[kind] = edges;
		}
		edges.add(edge);
	}
}