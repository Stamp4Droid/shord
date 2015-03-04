package stamp.missingmodels.util.cflsolver.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.Symbol;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;

public class Graph {
	public static class VertexMap {
		private final Map<String,Integer> vertices = new HashMap<String,Integer>();
		private final Map<Integer,String> verticesById = new HashMap<Integer,String>();
		
		public VertexMap(Iterable<EdgeStruct> edges) {
			for(EdgeStruct edge : edges) {
				this.add(edge.sourceName);
				this.add(edge.sinkName);
			}
		}
		
		private void add(String vertex) {
			if(this.vertices.get(vertex) == null) {
				this.verticesById.put(this.vertices.size(), vertex);
				this.vertices.put(vertex, this.vertices.size());
			}
		}
		
		public int get(String name) {
			return this.vertices.get(name);
		}
		
		public String get(int id) {
			return this.verticesById.get(id);
		}
		
		public boolean contains(String name) {
			return this.vertices.containsKey(name);
		}
		
		public boolean contains(int id) {
			return this.verticesById.containsKey(id);
		}
		
		public int size() {
			return this.vertices.size();
		}
	}
	
	public static class SimpleGraphBuilder {
		private final SymbolMap symbols;
		private final List<EdgeStruct> edges = new ArrayList<EdgeStruct>();
		public SimpleGraphBuilder(SymbolMap symbols) {
			this.symbols = symbols;
		}
		public void add(EdgeStruct edge) {
			this.edges.add(edge);
		}
		public void addOrUpdateEdge(String source, String sink, String symbol, int field, short weight) {
			this.add(new EdgeStruct(source, sink, symbol, field, weight));
		}
		
		public Graph getGraph() {
			GraphBuilder gb = new GraphBuilder(new VertexMap(this.edges), this.symbols);
			for(EdgeStruct edge : this.edges) {
				gb.addOrUpdateEdge(edge);
			}
			return gb.getGraph();
		}
	}
	
	// Be careful -- this has different semantics than SimpleGraphBuilder:
	// adding edges after getGraph() will update the original graph
	public static class GraphBuilder {
		private final Graph graph;

		public GraphBuilder(VertexMap vertices, SymbolMap symbols) {
			this.graph = new Graph(vertices, symbols);
		}
		
		public Graph getGraph() {
			return this.graph;
		}
		
		public Edge getEdge(Vertex source, Vertex sink, Symbol symbol, Field field) {
			return source.getCurrentOutgoingEdge(new Edge(symbol, source, sink, field));
		}
		
		public Edge addOrUpdateEdge(EdgeStruct edge) {
			return this.addOrUpdateEdge(edge.sourceName, edge.sinkName, edge.symbol, edge.field, edge.weight);
		}
		
		public Edge addOrUpdateEdge(String source, String sink, String symbol, int field, short weight) {
			return this.addOrUpdateEdge(this.graph.getVertex(source), this.graph.getVertex(sink), this.graph.symbols.get(symbol), Field.getField(field), weight, null, null);
		}
		
		public Edge addOrUpdateEdge(Vertex source, Vertex sink, Symbol symbol, Field field, short weight, Edge firstInput, Edge secondInput) {
			Edge curEdge = this.getEdge(source, sink, symbol, field);
			if(curEdge == null) {
				Edge edge = new Edge(symbol, source, sink, field);
				edge.weight = weight;
				edge.firstInput = firstInput;
				edge.secondInput = secondInput;
				source.addOutgoingEdge(edge);
				sink.addIncomingEdge(edge);
				this.graph.numEdges++;
				return edge;
			} else if(weight<curEdge.weight) {
				curEdge.weight = weight;
				curEdge.firstInput = firstInput;
				curEdge.secondInput = secondInput;
				return curEdge;
			} else {
				return null;
			}
		}
		
		public Vertex getVertex(String name) {
			return this.graph.getVertex(name);
		}
	}
	
	public interface GraphTransformer {
		public Graph transform(Iterable<EdgeStruct> edges);
	}
	
	public static abstract class EdgeTransformer implements GraphTransformer {
		private final SymbolMap symbols;
		private final VertexMap vertices;
		public EdgeTransformer(VertexMap vertices, SymbolMap symbols) {
			this.vertices = vertices;
			this.symbols = symbols;
		}
		public abstract void process(GraphBuilder gb, EdgeStruct edgeStruct);
		
		@Override
		public Graph transform(Iterable<EdgeStruct> edges) {
			GraphBuilder gb = new GraphBuilder(this.vertices, this.symbols);
			for(EdgeStruct edge : edges) {
				this.process(gb, edge);
			}
			return gb.getGraph();
		}
	}
	
	private final VertexMap vertices;
	private final Vertex[] vertexArray;
	private final SymbolMap symbols;
	private int numEdges = 0;
	
	public Graph(VertexMap vertices, SymbolMap symbols) {
		this.vertices = vertices;
		this.vertexArray = new Vertex[this.vertices.size()];
		this.symbols = symbols;
		for(int i=0; i<this.vertices.size(); i++) {
			this.vertexArray[i] = new Vertex(i, this.vertices.get(i),  this.symbols.getNumSymbols());

		}
	}
	
	public Graph transform(GraphTransformer transformer) {
		return transformer.transform(this.getEdgeStructs());
	}
	
	public Iterable<EdgeStruct> getEdgeStructs() {
		return new Iterable<EdgeStruct>() {
			@Override
			public Iterator<EdgeStruct> iterator() {
				return new EdgeStructIterator();
			}};
	}
	
	public Iterable<Edge> getEdges() {
		return new Iterable<Edge>() {
			@Override
			public Iterator<Edge> iterator() {
				return new EdgeIterator();
			}};
	}
	
	public VertexMap getVertices() {
		return this.vertices;
	}
	
	public SymbolMap getSymbols() {
		return this.symbols;
	}
	
	public int getNumEdges() {
		return this.numEdges;
	}
	
	public static interface Filter<T> {
		public boolean filter(T t);
	}
	
	public static class FilterTransformer extends EdgeTransformer {
		private final Filter<EdgeStruct> filter;
		
		public FilterTransformer(VertexMap vertices, SymbolMap symbols, Filter<EdgeStruct> filter) {
			super(vertices, symbols);
			this.filter = filter;
		}

		@Override
		public void process(GraphBuilder gb, EdgeStruct edgeStruct) {
			if(this.filter.filter(edgeStruct)) {
				gb.addOrUpdateEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, edgeStruct.weight);
			}
		}
	}
	
	public Iterable<EdgeStruct> getEdgeStructs(final Filter<EdgeStruct> filter) {
		return new Iterable<EdgeStruct>() {
			@Override
			public Iterator<EdgeStruct> iterator() {
				return new FilteredEdgeStructIterator(filter);
			}};
	}
	
	public Iterable<Edge> getEdges(Filter<Edge> filter) {
		return new Iterable<Edge>() {
			@Override
			public Iterator<Edge> iterator() {
				return new EdgeIterator();
			}};
	}

	public String toString(boolean shord) {
		StringBuilder sb = new StringBuilder();
		for(EdgeStruct edge : this.getEdgeStructs()) {
			sb.append(edge.toString(shord)).append("\n");
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return this.toString(false);
	}
	
	public Vertex getVertex(String name) {
		return this.vertexArray[this.vertices.get(name)];
	}
	
	public boolean containsVertex(String name) {
		return this.vertices.contains(name);
	}
	
	public int getNumVertices() {
		return this.vertices.size();
	}

	public abstract class TransformIterator<X,Y> implements Iterator<Y> {
		private final Iterator<X> iterator;
		
		public TransformIterator(Iterator<X> iterator) {
			this.iterator = iterator;
		}
		
		public abstract Y transform(X x);

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public Y next() {
			return this.transform(this.iterator.next());
		}

		@Override
		public void remove() {
			this.iterator.remove();
		}
	}
	
	private class EdgeStructIterator extends TransformIterator<Edge,EdgeStruct> {
		public EdgeStructIterator() {
			super(new EdgeIterator());
		}

		@Override
		public EdgeStruct transform(Edge edge) {
			return edge.getStruct();
		}
	}
	
	public abstract class TransformFilter<X,Y> implements Filter<X> {
		private final Filter<Y> filter;
		
		public TransformFilter(Filter<Y> filter) {
			this.filter = filter;
		}
		
		public abstract Y transform(X x);
		
		@Override
		public boolean filter(X x) {
			return this.filter.filter(this.transform(x));
		}
	}
	
	private class FilteredEdgeStructIterator extends TransformIterator<Edge,EdgeStruct> {
		public FilteredEdgeStructIterator(Filter<EdgeStruct> filter) {
			super(new FilteredEdgeIterator(new TransformFilter<Edge,EdgeStruct>(filter) {
				@Override
				public EdgeStruct transform(Edge edge) {
					return edge.getStruct();
				}
			}));
		}

		@Override
		public EdgeStruct transform(Edge edge) {
			return edge.getStruct();
		}
	}
	
	private class EdgeIterator implements Iterator<Edge> {
		private int curVertex = 0;
		private int curSymbolInt = 0;
		private Iterator<Edge> current;
		
		private EdgeIterator() {
			this.current = vertexArray.length>0 && symbols.getNumSymbols()>0 ? vertexArray[0].getIncomingEdges(0).iterator() : null;
			this.increment();
		}
		
		@Override
		public boolean hasNext() {
			return this.current != null;
		}
		
		private void incrementIndex() {
			if(++this.curSymbolInt == symbols.getNumSymbols()) {
				this.curVertex++;
				this.curSymbolInt = 0;
			}
		}
		
		private void increment() {
			while(this.current != null && !this.current.hasNext()) {
				this.incrementIndex();
				this.current = this.curVertex<vertexArray.length ? vertexArray[this.curVertex].getIncomingEdges(this.curSymbolInt).iterator() : null;
			}
		}
		
		@Override
		public Edge next() {
			Edge result = this.current.next();
			this.increment();
			return result;
		}
		
		@Override
		public void remove() {
			throw new RuntimeException("Remove is not implemented for EdgeIterator!");
		}
	}
	
	private class FilteredEdgeIterator implements Iterator<Edge> {
		private final EdgeIterator iterator = new EdgeIterator();
		private final Filter<Edge> filter;
		private Edge curEdge;
		
		private FilteredEdgeIterator(Filter<Edge> filter) {
			this.filter = filter;
			this.increment();
		}
		
		@Override
		public boolean hasNext() {
			return this.curEdge != null;
		}
		
		@Override
		public Edge next() {
			Edge result = this.curEdge;
			this.increment();
			return result;
		}
		
		@Override
		public void remove() {
			throw new RuntimeException("Remove is not implemented for FilteredEdgeIterator!");
		}
		
		private void increment() {
			while(this.iterator.hasNext()) {
				this.curEdge = this.iterator.next();
				if(this.filter.filter(this.curEdge)) {
					return;
				}
			}
			this.curEdge = null;
		}
	}
}
