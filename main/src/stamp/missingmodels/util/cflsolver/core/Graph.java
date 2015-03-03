package stamp.missingmodels.util.cflsolver.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.Symbol;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;

public class Graph {
	public static class GraphBuilder {
		private final Graph graph;
		
		public GraphBuilder(SymbolMap symbols) {
			this.graph = new Graph(symbols);
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
		public EdgeTransformer(SymbolMap symbols) {
			this.symbols = symbols;
		}
		public abstract void process(GraphBuilder gb, EdgeStruct edgeStruct);
		
		@Override
		public Graph transform(Iterable<EdgeStruct> edges) {
			GraphBuilder gb = new GraphBuilder(this.symbols);
			for(EdgeStruct edge : edges) {
				this.process(gb, edge);
			}
			return gb.getGraph();
		}
	}
	
	private final Map<String,Vertex> vertices = new HashMap<String,Vertex>();
	private final SymbolMap symbols;
	private int numEdges = 0;
	
	public Graph(SymbolMap symbols) {
		this.symbols = symbols;
	}
	
	public Graph transform(GraphTransformer transformer) {
		return transformer.transform(this.getEdgeStructs());
	}
	
	public Iterable<EdgeStruct> getEdgeStructs() {
		return new EdgeStructIterator();
	}
	
	public Iterable<Edge> getEdges() {
		return new EdgeIterator();
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
		
		public FilterTransformer(SymbolMap symbols, Filter<EdgeStruct> filter) {
			super(symbols);
			this.filter = filter;
		}

		@Override
		public void process(GraphBuilder gb, EdgeStruct edgeStruct) {
			if(this.filter.filter(edgeStruct)) {
				gb.addOrUpdateEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, edgeStruct.weight);
			}
		}
	}
	
	public Iterable<EdgeStruct> getEdgeStructs(Filter<EdgeStruct> filter) {
		return new FilteredEdgeStructIterator(filter);
	}
	
	public Iterable<Edge> getEdges(Filter<Edge> filter) {
		return new FilteredEdgeIterator(filter);
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
		Vertex vertex = this.vertices.get(name);
		if(vertex == null) {
			vertex = new Vertex(name, this.symbols.getNumSymbols());
			this.vertices.put(name, vertex);
		}
		return vertex;
	}
	
	public boolean containsVertex(String name) {
		return this.vertices.containsKey(name);
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
	
	private class EdgeStructIterator extends TransformIterator<Edge,EdgeStruct> implements Iterable<EdgeStruct> {
		public EdgeStructIterator() {
			super(new EdgeIterator());
		}

		@Override
		public Iterator<EdgeStruct> iterator() {
			return this;
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
	
	private class FilteredEdgeStructIterator extends TransformIterator<Edge,EdgeStruct> implements Iterable<EdgeStruct> {
		public FilteredEdgeStructIterator(Filter<EdgeStruct> filter) {
			super(new FilteredEdgeIterator(new TransformFilter<Edge,EdgeStruct>(filter) {
				@Override
				public EdgeStruct transform(Edge edge) {
					return edge.getStruct();
				}
			}));
		}

		@Override
		public Iterator<EdgeStruct> iterator() {
			return this;
		}

		@Override
		public EdgeStruct transform(Edge edge) {
			return edge.getStruct();
		}
	}
	
	private class EdgeIterator implements Iterator<Edge>, Iterable<Edge> {
		private Vertex[] vertexList = vertices.values().toArray(new Vertex[]{});
		private int curVertex = 0;
		private int curSymbolInt = 0;
		private Iterator<Edge> current;
		
		public EdgeIterator() {
			this.current = this.vertexList.length>0 && symbols.getNumSymbols()>0 ? this.vertexList[0].getIncomingEdges(0).iterator() : null;
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
				this.current = this.curVertex<this.vertexList.length ? this.vertexList[this.curVertex].getIncomingEdges(this.curSymbolInt).iterator() : null;
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

		@Override
		public Iterator<Edge> iterator() {
			return this;
		}
	}
	
	private class FilteredEdgeIterator implements Iterator<Edge>, Iterable<Edge> {
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
		
		@Override
		public Iterator<Edge> iterator() {
			return this;
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
