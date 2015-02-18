package stamp.missingmodels.util.jcflsolver2;

public class Edge {
	public final Vertex source;
	public final Vertex sink;
	public final int symbolInt;

	Edge nextA;
	Edge nextB;
	
	public short weight;
	public Edge nextWorklist;
	public Edge prevWorklist;
	
	public Edge firstInput;
	public Edge secondInput;
	
	Edge(int kind, Vertex source, Vertex sink) {
		this.source = source;
		this.sink = sink;
		this.symbolInt = kind;
		this.field = -1;
	}
	
	public final int field;

	public Edge(int kind, Vertex source, Vertex sink, int label) {
		this.source = source;
		this.sink = sink;
		this.symbolInt = kind;
		this.field = label;
	}

	public int hashCode() {
		return this.source.hashCode() + this.sink.hashCode() + this.symbolInt + this.field;
	}

	public boolean equals(Object o) {
		Edge other = (Edge)o;
		return this.source == other.source && this.sink == other.sink && this.symbolInt == other.symbolInt && this.field == other.field;
	}
}