package stamp.missingmodels.util.jcflsolver2;

public class Edge
{
	public final Node from;
	public final Node to;
	public final int kind;

	Edge nextA;
	Edge nextB;
	
	public short weight;
	public Edge nextWorklist;
	public Edge prevWorklist;
	
	public Edge firstInput;
	public Edge secondInput;
	
	Edge(int kind, Node from, Node to)
	{
		this.from = from;
		this.to = to;
		this.kind = kind;
		this.label = -1;
	}
	
	public final int label;

	public Edge(int kind, Node from, Node to, int label) {
		this.from = from;
		this.to = to;
		this.kind = kind;
		this.label = label;
	}

	public int hashCode() {
		return from.hashCode() + to.hashCode() + kind + label;
	}

	public boolean equals(Object o) {
		Edge other = (Edge)o;
		return from == other.from && to == other.to && kind == other.kind && label == other.label;
	}
	
	protected boolean matchesLabel(Edge other) {
		return (other.label == -1) || (this.label == -1) || (this.label == other.label);
	}
	
	protected int label() {
		return label;
	}

	public String toString() {
		return "LabeledEdge("+from.id+", "+to.id+", "+kind+", "+label+")";
	}

    public EdgeData getData(Graph g) {
    	return new EdgeData(this.from.name, this.to.name, g.kindToSymbol(kind), weight, Integer.toString(label()));
    }
}