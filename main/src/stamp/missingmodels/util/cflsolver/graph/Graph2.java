package stamp.missingmodels.util.cflsolver.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class Graph2 {
	private static final class EdgeWithData {
		private final Edge edge;
		
		private EdgeWithData(Edge edge) {
			this.edge = edge;
		}
		
		// temporary data while computing closure
		private EdgeWithData nextOutgoingEdge = null;
		private EdgeWithData nextIncomingEdge = null;
	}
	
	private static final class EdgeSet implements Iterable<Edge> {
		private static final double MAX_LOAD_FACTOR = 0.9;
		private static final int INITIAL_TABLE_SIZE = 16;

		private EdgeWithData[] table = new EdgeWithData[INITIAL_TABLE_SIZE];
		private int size = 0;

		@Override
		public Iterator<Edge> iterator() {
			return new EdgeSetIterator();
		}

		private EdgeWithData add(Edge edge) {
			EdgeWithData edgeWithData = this.get(edge);
			if(edgeWithData != null) {
				return edgeWithData;
			}
			// need to add the edge
			int numBuckets = this.table.length;
			double loadFactor = (double)(++this.size)/numBuckets;
			if (loadFactor > MAX_LOAD_FACTOR) {
				this.expandCapacity();
			}
			int index = edge.sink.hashCode()%numBuckets;
			edgeWithData = new EdgeWithData(edge);
			edgeWithData.nextOutgoingEdge = this.table[index];
			this.table[index] = edgeWithData;
			return edgeWithData;
		}
		
		private EdgeWithData get(Edge edge) {
			EdgeWithData e = this.table[edge.sink.hashCode()%this.table.length];
			while(e != null) {
				// source should already be equal
				if(e.edge.sink == edge.sink && e.edge.symbolInt == edge.symbolInt) {
					return e;
				}
				e = e.nextOutgoingEdge;
			}
			return null;
		}

		private void expandCapacity() {
			EdgeWithData[] oldTable = this.table;
			int oldNumBuckets = oldTable.length;
			int newNumBuckets = oldNumBuckets<<1;
			this.table = new EdgeWithData[newNumBuckets];
			for(int i=0; i<oldNumBuckets; i++) {
				EdgeWithData e = oldTable[i];
				while(e != null) {
					int index = e.edge.sink.hashCode()%newNumBuckets;
					EdgeWithData temp = e.nextOutgoingEdge;
					e.nextOutgoingEdge = this.table[index];
					this.table[index] = e;
					e = temp;
				}
			}
		}

		private class EdgeSetIterator implements Iterator<Edge> {
			private EdgeWithData current = null;
			private int index = 0;

			private EdgeSetIterator() {
				this.current = table[0];
				while(this.current == null && ++this.index<table.length) {
					this.current = table[this.index];
				}
			}

			public boolean hasNext() {
				return this.current != null;
			}

			public Edge next() {
				if(!this.hasNext()) {
					throw new NoSuchElementException();
				}
				EdgeWithData result = this.current;
				this.current = this.current.nextOutgoingEdge;
				while(this.current == null && ++this.index<table.length) {
					this.current = table[this.index];
				}
				return result.edge;
			}

			public void remove() {
				throw new RuntimeException("Remove is not implemented for EdgeSet!");
			}
		}
	}
	
	private static final class EdgeList implements Iterable<Edge> {
		private EdgeWithData head = null;

		public Iterator<Edge> iterator() {
			return new EdgeListIterator();
		}

		public EdgeWithData add(Edge edge) {
			EdgeWithData edgeWithData = new EdgeWithData(edge);
			edgeWithData.nextIncomingEdge = this.head;
			this.head = edgeWithData;
			return null;
		}

		private class EdgeListIterator implements Iterator<Edge> {
			private EdgeWithData current;

			public EdgeListIterator() {
				this.current = head;
			}

			public boolean hasNext() {
				return current != null;
			}

			public Edge next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				EdgeWithData result = this.current;
				this.current = this.current.nextIncomingEdge;
				return result.edge;
			}

			public void remove() {
				throw new RuntimeException("Remove is not implemented for EdgeList!");
			}
		}
	}
	
	public static final class Vertex {
		public final String name;
		private final EdgeSet[] outgoingEdgesBySymbol;
		private final EdgeList[] incomingEdgesBySymbol;
		
		private Vertex(String name, int numSymbols) {
			this.name = name;
			this.outgoingEdgesBySymbol = new EdgeSet[numSymbols];
			this.incomingEdgesBySymbol = new EdgeList[numSymbols];
			for(int i=0; i<numSymbols; i++) {
				this.outgoingEdgesBySymbol[i] = new EdgeSet();
				this.incomingEdgesBySymbol[i] = new EdgeList();
			}
		}
		
		public Iterable<Edge> getOutgoingEdges(int symbolInt) {
			return this.outgoingEdgesBySymbol[symbolInt];
		}
		
		public Iterable<Edge> getIncomingEdges(int symbolInt) {
			return this.incomingEdgesBySymbol[symbolInt];
		}
	}

	public static final class Edge {
		public final Vertex source;
		public final Vertex sink;
		public final int symbolInt;
		public final int field;
		
		private Edge(Vertex source, Vertex sink, int symbolInt, int field) {
			this.source = source;
			this.sink = sink;
			this.symbolInt = symbolInt;
			this.field = field;
		}
	}
	
	private final Map<String,Vertex> verticesByName = new HashMap<String,Vertex>();
	private final ContextFreeGrammarOpt contextFreeGrammar;
	
	public Graph2(ContextFreeGrammarOpt contextFreeGrammar) {
		this.contextFreeGrammar = contextFreeGrammar;
	}
	
	public ContextFreeGrammarOpt getContextFreeGrammar() {
		return this.contextFreeGrammar;
	}
	
	private Vertex getVertex(String name) {
		Vertex vertex = this.verticesByName.get(name);
		if(vertex == null) {
			vertex = new Vertex(name, this.contextFreeGrammar.getNumLabels());
			this.verticesByName.put(name, vertex);
		}
		return vertex;
	}
	
	public Edge addEdge(Vertex source, Vertex sink, int symbolInt, int field) {
		Edge edge = new Edge(source, sink, symbolInt, field);
		if(source.outgoingEdgesBySymbol[symbolInt].get(edge) == null) {
			sink.incomingEdgesBySymbol[symbolInt].add(edge);
			source.outgoingEdgesBySymbol[symbolInt].add(edge);
		}
		return edge;
	}
	
	public Edge addEdge(String source, String sink, String symbol, int field) {
		Vertex sourceVertex = this.getVertex(source);
		Vertex sinkVertex = this.getVertex(sink);		
		int symbolInt = this.contextFreeGrammar.getSymbolInt(symbol);
		return this.addEdge(sourceVertex, sinkVertex, symbolInt, field);
	}
	
	public boolean containsEdge(Vertex source, Vertex sink, int symbolInt, int field) {
		Edge edge = new Edge(source, sink, symbolInt, field);
		return source.outgoingEdgesBySymbol[symbolInt].get(edge) != null;
	}
	
	public boolean containsEdge(String source, String sink, String symbol, int field) {
		Vertex sourceVertex = this.getVertex(source);
		Vertex sinkVertex = this.getVertex(sink);
		int symbolInt = this.contextFreeGrammar.getSymbolInt(symbol);
		return this.containsEdge(sourceVertex, sinkVertex, symbolInt, field);
	}
	
	public Set<Edge> getEdges() {
		Set<Edge> edges = new HashSet<Edge>();
		for(Vertex vertex : this.verticesByName.values()) {
			for(EdgeSet outgoingEdges : vertex.outgoingEdgesBySymbol) {
				for(Edge edge : outgoingEdges) {
					edges.add(edge);
				}
			}
		}
		return edges;
	}
	
	public String getSymbol(int symbolInt) {
		return this.contextFreeGrammar.getSymbol(symbolInt);
	}
}
