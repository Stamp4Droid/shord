package stamp.missingmodels.util.cflsolver.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;

public final class Graph {
	public static final class Vertex {
		public final String name;
		protected final Map<Edge,EdgeInfo>[] outgoingEdgesBySymbol;
		protected final List<Edge>[] incomingEdgesBySymbol;
		
		private Vertex(String name, int numSymbol) {
			this.name = name;
			this.outgoingEdgesBySymbol = new Map[numSymbol];
			this.incomingEdgesBySymbol = new List[numSymbol];
			for(int i=0; i<numSymbol; i++) {
				this.outgoingEdgesBySymbol[i] = new HashMap<Edge,EdgeInfo>();
				this.incomingEdgesBySymbol[i] = new LinkedList<Edge>();
			}
		}

		public Set<Edge> getOutgoingEdges(int symbol) {
			return Collections.unmodifiableSet(this.outgoingEdgesBySymbol[symbol].keySet());
		}
		
		public List<Edge> getIncomingEdges(int symbol) {
			return Collections.unmodifiableList(this.incomingEdgesBySymbol[symbol]);
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	public static final class EdgeInfo {
		public final Edge firstInput;
		public final Edge secondInput;
		public final int weight;
		
		public EdgeInfo(Edge firstInput, Edge secondInput, int weight) {
			this.firstInput = firstInput;
			this.secondInput = secondInput;
			this.weight = weight;
		}
		
		public EdgeInfo(Edge input, int weight) {
			this(input, null, weight);
		}
		
		public EdgeInfo(int weight) {
			this(null, null, weight);
		}
		
		public EdgeInfo(Edge firstInput, Edge secondInput) {
			this(firstInput, secondInput, 0);
		}
		
		public EdgeInfo(Edge input) {
			this(input, null, 0);
		}
		
		public EdgeInfo() {
			this(null, null, 0);
		}
	}
	
	public final static class EdgeStruct {
		public final String sourceName;
		public final String sinkName;
		public final String symbol;
		public final Field field;
		public final Context context;
		
		public EdgeStruct(String sourceName, String sinkName, String symbol, Field field, Context context) {
			this.sourceName = sourceName;
			this.sinkName = sinkName;
			this.symbol = symbol;
			this.field = field;
			this.context = context;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.sourceName).append("-");
			sb.append(this.symbol).append("[");
			sb.append(this.field.toString()).append("][");
			sb.append(this.context).append("]");
			sb.append("-").append(this.sinkName);
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((context == null) ? 0 : context.hashCode());
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			result = prime * result
					+ ((sinkName == null) ? 0 : sinkName.hashCode());
			result = prime * result
					+ ((sourceName == null) ? 0 : sourceName.hashCode());
			result = prime * result
					+ ((symbol == null) ? 0 : symbol.hashCode());
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
			EdgeStruct other = (EdgeStruct) obj;
			if (context == null) {
				if (other.context != null)
					return false;
			} else if (!context.equals(other.context))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			if (sinkName == null) {
				if (other.sinkName != null)
					return false;
			} else if (!sinkName.equals(other.sinkName))
				return false;
			if (sourceName == null) {
				if (other.sourceName != null)
					return false;
			} else if (!sourceName.equals(other.sourceName))
				return false;
			if (symbol == null) {
				if (other.symbol != null)
					return false;
			} else if (!symbol.equals(other.symbol))
				return false;
			return true;
		}
	}

	public final class Edge {
		public final Vertex source;
		public final Vertex sink;
		public final int symbolInt;
		public final Field field;
		public final Context context;
		
		private Edge(Vertex source, Vertex sink, int symbolInt, Field field, Context context) {
			this.source = source;
			this.sink = sink;
			this.symbolInt = symbolInt;
			this.field = field;
			this.context = context;
		}
		
		public String getSymbol() {
			return contextFreeGrammar.getSymbol(this.symbolInt);
		}
		
		public EdgeInfo getInfo() {
			return this.source.outgoingEdgesBySymbol[this.symbolInt].get(this);
		}
		
		public EdgeStruct getStruct() {
			return new EdgeStruct(this.source.name, this.sink.name, this.getSymbol(), this.field, this.context);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.source.toString()).append("-");
			sb.append(contextFreeGrammar.getSymbol(this.symbolInt)).append("[");
			sb.append(this.field.toString()).append("][");
			sb.append(this.context).append("]");
			sb.append("-").append(this.sink.toString());
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((context == null) ? 0 : context.hashCode());
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			result = prime * result + ((sink == null) ? 0 : sink.hashCode());
			result = prime * result
					+ ((source == null) ? 0 : source.hashCode());
			result = prime * result + symbolInt;
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (context == null) {
				if (other.context != null)
					return false;
			} else if (!context.equals(other.context))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
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
			if (symbolInt != other.symbolInt)
				return false;
			return true;
		}

		private Graph getOuterType() {
			return Graph.this;
		}
	}
	
	private final Map<String,Vertex> verticesByName = new HashMap<String,Vertex>();
	private final ContextFreeGrammar contextFreeGrammar;
	
	public Graph(ContextFreeGrammar contextFreeGrammar) {
		this.contextFreeGrammar = contextFreeGrammar;
	}
	
	private Vertex getVertex(String name) {
		Vertex vertex = this.verticesByName.get(name);
		if(vertex == null) {
			vertex = new Vertex(name, this.contextFreeGrammar.getNumLabels());
			this.verticesByName.put(name, vertex);
		}
		return vertex;
	}
	
	protected Edge addEdge(Vertex source, Vertex sink, int symbolInt, Field field, Context context, EdgeInfo edgeInfo) {
		Edge edge = new Edge(source, sink, symbolInt, field, context);
		if(!source.outgoingEdgesBySymbol[symbolInt].containsKey(edge)) {
			sink.incomingEdgesBySymbol[symbolInt].add(edge);
		}
		source.outgoingEdgesBySymbol[symbolInt].put(edge, edgeInfo);
		return edge;
	}
	
	protected Edge addEdge(String source, String sink, String symbol, Field field, Context context, EdgeInfo edgeInfo) {
		Vertex sourceVertex = this.getVertex(source);
		Vertex sinkVertex = this.getVertex(sink);		
		int symbolInt = this.contextFreeGrammar.getSymbolInt(symbol);
		return this.addEdge(sourceVertex, sinkVertex, symbolInt, field, context, edgeInfo);
	}
	
	public EdgeInfo getInfo(Vertex source, Vertex sink, int symbolInt, Field field, Context context) {
		Edge edge = new Edge(source, sink, symbolInt, field, context);
		return source.outgoingEdgesBySymbol[symbolInt].get(edge);
	}
	
	public EdgeInfo getInfo(String source, String sink, String symbol, Field field, Context context) {
		Vertex sourceVertex = this.getVertex(source);
		Vertex sinkVertex = this.getVertex(sink);
		int symbolInt = this.contextFreeGrammar.getSymbolInt(symbol);
		return this.getInfo(sourceVertex, sinkVertex, symbolInt, field, context);
	}

	public ContextFreeGrammar getContextFreeGrammar() {
		return this.contextFreeGrammar;
	}
	
	public static interface EdgeFilter {
		public boolean filter(Edge edge);
	}
	
	public Graph getFilteredGraph(EdgeFilter filter) {
		GraphBuilder gb = new GraphBuilder(this.contextFreeGrammar);
		for(Edge edge : this.getEdges(filter)) {
			gb.addEdge(edge.source.name, edge.sink.name, edge.getSymbol(), edge.field, edge.context, edge.getInfo());
		}
		return gb.toGraph();
	}
	
	public Set<Edge> getEdges(EdgeFilter filter) {
		Set<Edge> edges = new HashSet<Edge>();
		for(Vertex vertex : this.verticesByName.values()) {
			for(Map<Edge,EdgeInfo> outgoingEdges : vertex.outgoingEdgesBySymbol) {
				for(Edge edge : outgoingEdges.keySet()) {
					if(filter.filter(edge)) {
						edges.add(edge);
					}					
				}
			}
		}
		return edges;
	}
	
	public Set<Edge> getEdges() {
		Set<Edge> edges = new HashSet<Edge>();
		for(Vertex vertex : this.verticesByName.values()) {
			for(Map<Edge,EdgeInfo> outgoingEdges : vertex.outgoingEdgesBySymbol) {
				edges.addAll(outgoingEdges.keySet());
			}
		}
		return edges;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Edge edge : this.getEdges()) {
			sb.append(edge.toString()).append("\n");
		}
		return sb.toString();
	}
}
