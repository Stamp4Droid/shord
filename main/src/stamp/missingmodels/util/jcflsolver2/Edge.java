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
		this.label = -1;
	}
	
	public final int label;

	public Edge(int kind, Vertex source, Vertex sink, int label) {
		this.source = source;
		this.sink = sink;
		this.symbolInt = kind;
		this.label = label;
	}

	public int hashCode() {
		return this.source.hashCode() + this.sink.hashCode() + this.symbolInt + this.label;
	}

	public boolean equals(Object o) {
		Edge other = (Edge)o;
		return this.source == other.source && this.sink == other.sink && this.symbolInt == other.symbolInt && this.label == other.label;
	}

    public EdgeData getData(Graph g) {
    	return new EdgeData(this.source.name, this.sink.name, g.kindToSymbol(symbolInt), weight, Integer.toString(this.label));
    }
}