package graph;

public final class Edge {
	public final Vertex source;
	public final Vertex sink;
	public final int label;
	public final int field;
	
	public Edge(Vertex source, Vertex sink, int label, int field) {
		this.source = source;
		this.sink = sink;
		this.label = label;
		this.field = field;
	}
	
	public Edge(Vertex source, Vertex sink, int label) {
		this.source = source;
		this.sink = sink;
		this.label = label;
		this.field = -1;
	}
}
