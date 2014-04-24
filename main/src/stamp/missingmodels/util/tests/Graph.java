package stamp.missingmodels.util.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.Util.MultivalueMap;

public final class Graph {
	public static final int DEFAULT_FIELD = -1;
	public static final int ANY_FIELD = -2;
	public static final int MISMATCHED_FIELD = -3;

	public final class Vertex {
		public final Map[] incomingEdgesByLabel; // list of type Edge
		public final Map[] outgoingEdgesByLabel; // list of type Edge

		public Vertex(int numLabels) {
			this.incomingEdgesByLabel = new Map[numLabels];
			for(int i=0; i<numLabels; i++) {
				this.incomingEdgesByLabel[i] = new HashMap();
			}
			this.outgoingEdgesByLabel = new Map[numLabels];
			for(int i=0; i<numLabels; i++) {
				this.outgoingEdgesByLabel[i] = new HashMap();
			}
		}
		
		public String getName() {
			return vertexNames.get(this);
		}

		@Override
		public String toString() {
			return vertexNames.get(this);
		}
	}

	public final class Edge {
		public final Vertex source;
		public final Vertex sink;
		public final int field;
		public final int label;
		public Edge firstInput;
		public Edge secondInput;
		public short weight;

		public Edge(Vertex source, Vertex sink, int label, int field, Edge firstInput, Edge secondInput, short weight) {
			this.source = source;
			this.sink = sink;
			this.field = field;
			this.label = label;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
			this.weight = weight;
		}

		public Edge(Vertex source, Vertex sink, int label, int field, short weight) {
			this(source, sink, label, field, null, null, weight);
		}

		public Edge(Vertex source, Vertex sink, int label, int field) {
			this(source, sink, label, field, null, null, (short)0);
		}

		public Edge(Vertex source, Vertex sink, int label) {
			this(source, sink, label, DEFAULT_FIELD, null, null, (short)0);
		}

		@Override
		public String toString() {
			return this.source.toString() + " " + this.sink.toString() + " " + contextFreeGrammar.getLabelName(this.label) + " " + getFieldName(this.field) + " " + this.weight;
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
	private final ContextFreeGrammar contextFreeGrammar;
	private final int numLabels;

	private int curField = 0;

	public Graph(ContextFreeGrammar contextFreeGrammar) {
		this.contextFreeGrammar = contextFreeGrammar;
		this.numLabels = contextFreeGrammar.numLabels();
	}
	
	public ContextFreeGrammar getContextFreeGrammar() {
		return this.contextFreeGrammar;
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

	public String getVertexName(Vertex vertex) {
		return this.vertexNames.get(vertex);
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
		if(field == DEFAULT_FIELD) {
			return "none";
		}
		return this.fieldNames.get(field);
	}

	public Edge addEdge(String source, String sink, String label) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.contextFreeGrammar.getLabel(label), DEFAULT_FIELD, (short)0);
	}

	public Edge addEdge(String source, String sink, String label, String field) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.contextFreeGrammar.getLabel(label), this.getField(field), (short)0);
	}

	public Edge addEdge(String source, String sink, String label, short weight) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.contextFreeGrammar.getLabel(label), DEFAULT_FIELD, weight);
	}

	public Edge addEdge(String source, String sink, String label, String field, short weight) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.contextFreeGrammar.getLabel(label), this.getField(field), weight);
	}

	public Edge addEdge(Vertex source, Vertex sink, int label, int field, short weight) {
		if(field == MISMATCHED_FIELD) {
			return null;
		}
		Edge edge = new Edge(source, sink, label, field, weight);
		source.outgoingEdgesByLabel[label].put(edge, edge);
		sink.incomingEdgesByLabel[label].put(edge, edge);
		return edge;
	}

	// for use by reachability solver
	// call getEdge before adding edge, or the update may not occur correctly
	public void addEdge(Edge edge) {
		edge.source.outgoingEdgesByLabel[edge.label].put(edge, edge);
		edge.sink.incomingEdgesByLabel[edge.label].put(edge, edge);
	}
	public Edge getEdge(Edge edge) {
		return (Edge)edge.source.outgoingEdgesByLabel[edge.label].get(edge);
	}

	// outputs
	public Set<Edge> getEdges() {
		Set<Edge> edges = new HashSet<Edge>();
		for(Vertex vertex : this.vertices.values()) {
			for(int i=0; i<vertex.outgoingEdgesByLabel.length; i++) {
				edges.addAll(vertex.outgoingEdgesByLabel[i].keySet());
			}
		}
		return edges;
	}
	private void getPositiveWeightInputsHelper(Edge edge, Set<Edge> inputs) {
		if(edge == null) {
			return;
		} else if(edge.firstInput == null && edge.weight > (short)0) {
			inputs.add(edge);
		} else if(edge.firstInput != null) {
			this.getPositiveWeightInputsHelper(edge.firstInput, inputs);
			this.getPositiveWeightInputsHelper(edge.secondInput, inputs);
		}	
	}
	public Set<Edge> getPositiveWeightInputs(Edge edge) {
		Set<Edge> inputs = new HashSet<Edge>();
		this.getPositiveWeightInputsHelper(edge, inputs);
		return inputs;
	}
	public void getInputsHelper(Edge edge, int label, Set<Edge> inputs) {
		if(edge == null) {
			return;
		}
		if(edge.label == label) {
			inputs.add(edge);
		}
		this.getInputsHelper(edge.firstInput, label, inputs);
		this.getInputsHelper(edge.secondInput, label, inputs);
	}
	public Set<Edge> getInputs(Edge edge, String label) {
		Set<Edge> inputs = new HashSet<Edge>();
		this.getInputsHelper(edge, this.contextFreeGrammar.getLabel(label), inputs);
		return inputs;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Edge edge : this.getEdges()) {
			sb.append(edge.toString()).append("\n");
		}
		return sb.toString();
	}

	public MultivalueMap<String,Edge> getSortedEdges() {
		MultivalueMap<String,Edge> sortedEdges = new MultivalueMap<String,Edge>();
		for(Edge edge : this.getEdges()) {
			sortedEdges.add(this.contextFreeGrammar.getLabelName(edge.label), edge);
		}
		return sortedEdges;
	}
}