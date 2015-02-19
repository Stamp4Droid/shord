package stamp.missingmodels.util.jcflsolver2;

import java.util.HashMap;
import java.util.Map;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;

public class Graph {
	public Map<String,Vertex> nodes = new HashMap<String,Vertex>();
	private final ContextFreeGrammarOpt c;
	private final ReachabilitySolver s;

	public Graph(ContextFreeGrammarOpt c, ReachabilitySolver s) {
		this.c = c;
		this.s = s;
	}
	
	public Vertex getVertex(String name) {
		Vertex vertex = this.nodes.get(name);
		if(vertex == null) {
			vertex = new Vertex(c.getNumLabels());
			this.nodes.put(name, vertex);
		}
		return vertex;
	}

	public void addEdge(String sourceName, String sinkName, int symbolInt, int field, short weight) {
		//this.addEdge(this.getVertex(sourceName), this.getVertex(sinkName), symbolInt, field, weight, null, null);
		Edge edge = new Edge(symbolInt, this.getVertex(sourceName), this.getVertex(sinkName), field);
		edge.weight = weight;
		if(this.getVertex(sourceName).getOutgoingEdge(edge) == null) {
			this.getVertex(sourceName).addOutgoingEdge(edge);
			this.getVertex(sinkName).addIncomingEdge(edge);
		}
		s.addEdgeToWorklist(edge, null);
	}
	
	public void addEdge(Vertex source, Vertex sink, int symbolInt, int field, short weight, Edge firstInput, Edge secondInput) {
		Edge edge = new Edge(symbolInt, source, sink, field);
		edge.weight = weight;

		edge.firstInput = firstInput;
		edge.secondInput = secondInput;

		Edge oldEdge = source.getOutgoingEdge(edge);
		if(oldEdge == null) {
			source.addOutgoingEdge(edge);
			sink.addIncomingEdge(edge);
		}

		s.addEdgeToWorklist(edge, oldEdge);
	}
}