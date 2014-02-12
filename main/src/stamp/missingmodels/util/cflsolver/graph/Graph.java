package stamp.missingmodels.util.cflsolver.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.cfg.ContextFreeGrammar;

public final class Graph {
	public final class Vertex {
		public final Set[] incomingEdgesByLabel; // list of type Edge
		public final Set[] outgoingEdgesByLabel; // list of type Edge

		public Vertex(int numLabels) {
			this.incomingEdgesByLabel = new Set[numLabels];
			for(int i=0; i<numLabels; i++) {
				this.incomingEdgesByLabel[i] = new HashSet();
			}
			this.outgoingEdgesByLabel = new Set[numLabels];
			for(int i=0; i<numLabels; i++) {
				this.outgoingEdgesByLabel[i] = new HashSet();
			}
		}
		
		@Override
		public String toString() {
			return vertexNames.get(this);
		}
	}

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

		@Override
		public String toString() {
			return this.source.toString() + " " + this.sink.toString() + " " + contextFreeGrammar.getLabelString(this.label) + " " + getFieldName(this.field);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + field;
			result = prime * result + label;
			result = prime * result + ((sink == null) ? 0 : sink.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Edge other = (Edge) obj;
			if (field != other.field)
				return false;
			if (label != other.label)
				return false;
			if (sink == null) {
				if (other.sink != null)
					return false;
			} else if (!sink.equals(other.sink))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			return true;
		}
	}

	private final Map<String,Vertex> vertices = new HashMap<String,Vertex>();
	private final Map<Vertex,String> vertexNames = new HashMap<Vertex,String>(); 
	private final Map<String,Integer> fields = new HashMap<String,Integer>();
	private final Map<Integer,String> fieldNames = new HashMap<Integer,String>();
	public final List<Edge> edges = new LinkedList<Edge>();
	private final ContextFreeGrammar contextFreeGrammar;
	private final int numLabels;

	private int curField = 0;

	public Graph(ContextFreeGrammar contextFreeGrammar) {
		this.contextFreeGrammar = contextFreeGrammar;
		this.numLabels = contextFreeGrammar.numLabels();
	}

	public Vertex getVertex(String name) {
		Vertex vertex = this.vertices.get(name);
		if(vertex == null) {
			vertex = new Vertex(this.numLabels);
			this.vertices.put(name, vertex);
			this.vertexNames.put(vertex, name);
		}
		return vertex;
	}

	public int getField(String field) {
		Integer intField = this.fields.get(field);
		if(intField == null) {
			intField = curField++;
			this.fields.put(field, intField);
			this.fieldNames.put(intField, field);
		}
		return intField;
	}
	
	public String getFieldName(int field) {
		if(field == -1) {
			return "none";
		}
		return this.fieldNames.get(field);
	}

	public Edge addEdge(String source, String sink, String label) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.contextFreeGrammar.getLabel(label), -1);
	}

	public Edge addEdge(String source, String sink, String label, String field) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.contextFreeGrammar.getLabel(label), this.getField(field));
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
