package stamp.missingmodels.util.cflsolver.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.Symbol;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;
import stamp.missingmodels.util.cflsolver.core.Util.Counter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.util.IOUtils;

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
		
		private void add(String name) {
			if(!this.vertices.containsKey(name)) {
				this.verticesById.put(this.vertices.size(), name);
				this.vertices.put(name, this.vertices.size());
			}
		}
		
		public int get(String name) {
			Integer vertex = this.vertices.get(name);
			if(vertex == null) {
				throw new RuntimeException("Missing vertex: " + name);
			}
			return vertex;
		}
		
		public String get(int id) {
			return this.verticesById.get(id);
		}
		
		public boolean contains(String name) {
			return this.vertices.containsKey(name);
		}
		
		public int size() {
			return this.vertices.size();
		}
	}
	
	public static Graph getGraph(VertexMap vertices, SymbolMap symbols, Iterable<EdgeStruct> edges) {
		GraphBuilder gb = new GraphBuilder(vertices, symbols);
		for(EdgeStruct edge : edges) {
			if(vertices.contains(edge.sourceName) && vertices.contains(edge.sinkName)) {
				gb.addOrUpdateEdge(edge);
			}
		}
		return gb.getGraph();
	}
	
	public static Graph getGraph(SymbolMap symbols, Iterable<EdgeStruct> edges) {
		VertexMap vertices = new VertexMap(edges);
		GraphBuilder gb = new GraphBuilder(vertices, symbols);
		for(EdgeStruct edge : edges) {
			if(vertices.contains(edge.sourceName) && vertices.contains(edge.sinkName)) {
				gb.addOrUpdateEdge(edge);
			}
		}
		return gb.getGraph();
	}
	
	// Note: Modifying GraphBuilder after getGraph will change the previously returned graph
	public static class GraphBuilder {
		private final Graph graph;
		private final Counter<String> counts = new Counter<String>();
		private int numEdges = 0;
		
		public GraphBuilder(VertexMap vertices, SymbolMap symbols) {
			this.graph = new Graph(vertices, symbols);
		}
		
		public Graph getGraph() {
			return this.graph;
		}
		
		public Edge getEdge(Vertex source, Vertex sink, Symbol symbol, Field field) {
			return new Edge(symbol, source, sink, field);
		}
		
		public Edge getCurrentEdge(Vertex source, Vertex sink, Symbol symbol, Field field) {
			return source.getCurrentOutgoingEdge(new Edge(symbol, source, sink, field));
		}
		
		public Edge addOrUpdateEdge(EdgeStruct edge) {
			return this.addOrUpdateEdge(edge.sourceName, edge.sinkName, edge.symbol, edge.field, edge.weight);
		}
		
		public Edge addOrUpdateEdge(String source, String sink, String symbol, int field, short weight) {
			return this.addOrUpdateEdge(this.graph.getVertex(source), this.graph.getVertex(sink), this.graph.symbols.get(symbol), Field.getField(field), weight, null, null);
		}
		
		public Edge addOrUpdateEdge(Vertex source, Vertex sink, Symbol symbol, Field field, short weight, Edge firstInput, Edge secondInput) {
			Edge curEdge = this.getCurrentEdge(source, sink, symbol, field);
			if(curEdge == null) {
				Edge edge = new Edge(symbol, source, sink, field);
				edge.weight = weight;
				edge.firstInput = firstInput;
				edge.secondInput = secondInput;
				source.addOutgoingEdge(edge);
				sink.addIncomingEdge(edge);
				this.numEdges++;
				this.counts.increment(symbol.symbol);
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
		
		public int getNumEdges() {
			return this.numEdges;
		}

		public int getCount(String symbol) {
			return this.counts.getCount(symbol);
		}
	}
	
	public static class GraphEdgeFilter implements Filter<Edge> {
		private final Graph graph;
		private final boolean[] symbols;
		
		public GraphEdgeFilter(VertexMap vertices, SymbolMap symbolMap, Iterable<EdgeStruct> edges) {
			this.symbols = new boolean[symbolMap.getNumSymbols()];
			for(EdgeStruct edge : edges) {
				this.symbols[symbolMap.get(edge.symbol).id] = true;
			}
			this.graph = Graph.getGraph(vertices, symbolMap, edges);
		}
		
		@Override
		public boolean filter(Edge edge) {
			Vertex source = this.graph.getVertex(edge.source.id);
			Vertex sink = this.graph.getVertex(edge.sink.id);
			return !this.symbols[edge.symbol.id] || source.getCurrentOutgoingEdge(new Edge(edge.symbol, source, sink, edge.field)) != null;
		}
	}
	
	public static class GraphVertexFilter implements Filter<Edge> {
		private final boolean[] filter;
		public GraphVertexFilter(VertexMap vertices, Iterable<String> rejectedVertices) {
			this.filter = new boolean[vertices.size()];
			for(int i=0; i<vertices.size(); i++) {
				this.filter[i] = true;
			}
			for(String rejectedVertex : rejectedVertices) {
				this.filter[vertices.get(rejectedVertex)] = false;
			}
		}

		@Override
		public boolean filter(Edge t) {
			return this.filter[t.source.id] && this.filter[t.sink.id];
		}
	}
	
	public interface GraphTransformer {
		public Graph transform(Iterable<EdgeStruct> edges);
	}
	
	public static abstract class EdgeTransformer implements GraphTransformer {
		private final VertexMap vertices;
		private final SymbolMap symbols;
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
	
	private final Vertex[] vertexArray;
	private final VertexMap vertices;
	private final SymbolMap symbols;
	
	public Graph(VertexMap vertices, SymbolMap symbols) {
		this.vertices = vertices;
		this.symbols = symbols;
		this.vertexArray = new Vertex[this.vertices.size()];
		for(int i=0; i<this.vertices.size(); i++) {
			this.vertexArray[i] = new Vertex(i, this.vertices.get(i), this.symbols.getNumSymbols());
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
	
	public Vertex getVertex(int id) {
		return this.vertexArray[id];
	}
	
	public VertexMap getVertices() {
		return this.vertices;
	}
	
	public SymbolMap getSymbols() {
		return this.symbols;
	}
	
	public Iterable<EdgeStruct> getEdgeStructs(final Filter<EdgeStruct> filter) {
		return new Iterable<EdgeStruct>() {
			@Override
			public Iterator<EdgeStruct> iterator() {
				return new FilteredEdgeStructIterator(filter);
			}};
	}
	
	public Iterable<Edge> getEdges(final Filter<Edge> filter) {
		return new Iterable<Edge>() {
			@Override
			public Iterator<Edge> iterator() {
				return new FilteredEdgeIterator(filter);
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

	public static abstract class TransformIterator<X,Y> implements Iterator<Y> {
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
	
	public static abstract class TransformFilter<X,Y> implements Filter<X> {
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
		
		public EdgeIterator() {
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
		
		public FilteredEdgeIterator(Filter<Edge> filter) {
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
