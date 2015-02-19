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
}