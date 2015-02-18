package stamp.missingmodels.util.jcflsolver2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Graph {
	public abstract void process(Edge edge);

	public abstract int numKinds();

	public abstract int symbolToKind(String symbol);

	public abstract String kindToSymbol(int kind);

	public abstract String[] outputRels();

	public abstract short kindToWeight(int kind);

	public abstract boolean useReps();

	public Algorithm algo;

	//public Collection<Node> nodes = new ArrayList();
	public Map<String,Node> nodes = new HashMap<String,Node>();

	public Graph() {
		if(this.useReps()) {
			this.algo = new RepsAlgo(this);
		} else {
			this.algo = new KnuthsAlgo(this);
		}
	}

	public final void addNode(Node node) {
		nodes.put(node.name, node);
	}

	public final void addEdge(Node from, Node to, int kind, Edge edgeA, boolean addLabel) {
		int label = addLabel ? Math.max(edgeA.label(), -1) : -1;
		this.addEdgeInternal(from, to, kind, addLabel, label, edgeA.weight, edgeA, null);
	}

	public final void addEdge(Node from, Node to, int kind, Edge edgeA, Edge edgeB, boolean addLabel) {
		if(!edgeA.matchesLabel(edgeB))
			return;
		int label = addLabel ? Math.max(edgeA.label(), edgeB.label()) : -1;
		this.addEdgeInternal(from, to, kind, addLabel, label, (short)(edgeA.weight + edgeB.weight), edgeA, edgeB);
	}

	public final void setAlgo(Algorithm algo) {
		this.algo = algo;
	}

	public final Collection<Node> allNodes() {
		return nodes.values();
	}

	public Node getNode(String name) {
		Node node = this.nodes.get(name);
		if(node == null) {
			node = new Node(name, numKinds());
			this.nodes.put(name, node);
		}
		return node;
	}

	public void addInputEdge(String from, String to, int kind, int label) {
		this.addWeightedInputEdge(from, to, kind, label, (short)0);
	}

	public void addWeightedInputEdge(String from, String to, int kind, int label, short weight) {
		assert label >= 0;

		Node fromNode = getNode(from);
		Node toNode = getNode(to);

		Edge newEdge = new LabeledEdge(kind, fromNode, toNode, label);
		newEdge.weight = weight;
		fromNode.addInputOutEdge(newEdge);
		toNode.addInEdge(newEdge);
		algo.addEdge(newEdge, null);
	}

	public void addInputEdge(String from, String to, int kind) {
		this.addWeightedInputEdge(from, to, kind, (short)0);
	}

	public void addWeightedInputEdge(String from, String to, int kind, short weight) {
		Node fromNode = getNode(from);
		Node toNode = getNode(to);

		if(kind == symbolToKind("Src2Sink")) {
			System.out.println("ERROR: adding input Src2Sink edge!");
		}
		Edge newEdge = new NonLabeledEdge(kind, fromNode, toNode);
		newEdge.weight = weight;
		fromNode.addInputOutEdge(newEdge);
		toNode.addInEdge(newEdge);
		algo.addEdge(newEdge, null);
	}

	private void addEdgeInternal(Node from, Node to, int kind, boolean addLabel, int label, short weight, Edge edgeA, Edge edgeB) {
		Edge newEdge = (addLabel && label >= 0)  ? new LabeledEdge(kind, from, to, label) : new NonLabeledEdge(kind, from, to);
		newEdge.weight = weight;

		newEdge.firstInput = edgeA;
		newEdge.secondInput = edgeB;

		Edge oldEdge = from.addOutEdge(newEdge);
		if(oldEdge == null) {
			to.addInEdge(newEdge);
		}

		algo.addEdge(newEdge, oldEdge);
	}
}