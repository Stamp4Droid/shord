package stamp.missingmodels.util.cflsolver.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Graph {
	private final Map<String,Vertex> vertices = new HashMap<String,Vertex>();
	public final List<Edge> edges = new LinkedList<Edge>();
	private final int numLabels;
	
	public Graph(int numLabels) {
		this.numLabels = numLabels;
	}
	
	private Vertex getVertex(String name) {
		Vertex vertex = this.vertices.get(name);
		if(vertex == null) {
			vertex = new Vertex(name, this.numLabels);
			this.vertices.put(name, vertex);
		}
		return vertex;
	}
	
	public Edge addEdge(String source, String sink, int label) {
		return this.addEdge(source, sink, label, -1);
	}
	
	public Edge addEdge(String source, String sink, int label, int field) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), label, field);
	}
	
	public Edge addEdge(Vertex source, Vertex sink, int label, int field) {
		Edge edge = new Edge(source, sink, label, field);
		if(field == -2 || source.outgoingEdgesByLabel[label].contains(edge)) {
			return null;
		}
		source.outgoingEdgesByLabel[label].add(edge);
		sink.incomingEdgesByLabel[label].add(edge);
		this.edges.add(edge);
		return edge;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Edge edge : this.edges) {
			sb.append(edge.toString()).append("\n");
		}
		return sb.toString();
	}
}
