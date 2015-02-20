package stamp.missingmodels.util.jcflsolver2;

import java.util.HashMap;
import java.util.Map;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;

public class Graph {
	public Map<String,Vertex> nodes = new HashMap<String,Vertex>();
	private final ContextFreeGrammarOpt c;

	public Graph(ContextFreeGrammarOpt c) {
		this.c = c;
	}
	
	public Vertex getVertex(String name) {
		Vertex vertex = this.nodes.get(name);
		if(vertex == null) {
			vertex = new Vertex(c.getNumLabels());
			this.nodes.put(name, vertex);
		}
		return vertex;
	}
	
	public boolean addEdge(Vertex source, Vertex sink, int symbolInt, int field, short weight, Edge firstInput, Edge secondInput, BucketHeap worklist) {
		Edge edge = new Edge(symbolInt, source, sink, field);
		edge.weight = weight;

		edge.firstInput = firstInput;
		edge.secondInput = secondInput;

		Edge oldEdge = source.getCurrentOutgoingEdge(edge);
		if(oldEdge == null) {
			source.addOutgoingEdge(edge);
			sink.addIncomingEdge(edge);
			if(worklist != null) {
				worklist.push(edge);
			}
			return true;
		} else {
			if(edge.weight < oldEdge.weight) {
				oldEdge.weight = edge.weight;
				oldEdge.firstInput = edge.firstInput;
				oldEdge.secondInput = edge.secondInput;
				if(worklist != null) {
					worklist.update(oldEdge);
				}
			}
			return false;
		}		
	}
}