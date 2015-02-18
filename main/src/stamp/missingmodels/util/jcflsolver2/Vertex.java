package stamp.missingmodels.util.jcflsolver2;

/*
import stamp.missingmodels.util.jcflsolver2.EdgeCollection.EdgeList;
import stamp.missingmodels.util.jcflsolver2.EdgeCollection.Edge;
*/

public class Vertex {
	final EdgeCollection[] inEdges;
	final EdgeCollection[] outEdges;
	final int id;

	private static int nodeCount = 0;
	private static final boolean OUT_EDGES_NEXT = true;

	public Vertex(int numSymbols) {
		this.inEdges = new EdgeCollection[numSymbols];
		this.outEdges = new EdgeCollection[numSymbols];
		this.id = ++nodeCount;
	}
	
	public EdgeCollection getOutEdges(int kind) {
		EdgeCollection edges = outEdges[kind];
		if(edges == null) {
			edges = EdgeCollection.EMPTY_EDGES;
		}
		return edges;
	}
	
	public EdgeCollection getInEdges(int kind) {
		EdgeCollection edges = inEdges[kind];
		if(edges == null) {
			edges = EdgeCollection.EMPTY_EDGES;
		}
		return edges;
	}
 
	Edge addOutEdge(Edge edge) {
		int kind = edge.symbolInt;
		EdgeCollection edges = outEdges[kind];
		if(edges == null) {
			edges = new EdgeSet(OUT_EDGES_NEXT);
			outEdges[kind] = edges;
		}
		return edges.add(edge);
	}

	void addInputOutEdge(Edge edge) {
		int kind = edge.symbolInt;
		EdgeCollection edges = outEdges[kind];
		if(edges == null) {
			edges = new EdgeSet(OUT_EDGES_NEXT);
			outEdges[kind] = edges;
		}
		edges.add(edge);
	}
	
	void addInEdge(Edge edge) {
		int kind = edge.symbolInt;
		EdgeCollection edges = inEdges[kind];
		if(edges == null) {
			edges = new EdgeList(!OUT_EDGES_NEXT);
			inEdges[kind] = edges;
		}
		edges.add(edge);
	}
}