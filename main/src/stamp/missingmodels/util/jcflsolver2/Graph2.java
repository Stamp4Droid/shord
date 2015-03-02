package stamp.missingmodels.util.jcflsolver2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar.Symbol;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeStruct;
import stamp.missingmodels.util.jcflsolver2.Edge.Field;
import stamp.missingmodels.util.jcflsolver2.RelationManager.Relation;

public class Graph2 {
	public static class GraphBuilder {
		private final Graph2 graph;
		
		public GraphBuilder(ContextFreeGrammarOpt c) {
			this.graph = new Graph2(c);
		}
		
		public Graph2 getGraph() {
			return this.graph;
		}
		
		public boolean addEdge(EdgeStruct edge) {
			return this.addEdge(edge.sourceName, edge.sinkName, edge.symbol, Field.getField(edge.field), edge.weight);
		}
		
		public boolean addEdge(String source, String sink, String symbol, Field field, short weight) {
			return this.graph.addEdge(source, sink, symbol, field, weight);
		}
		
		public boolean addEdge(Vertex source, Vertex sink, Symbol symbol, Field field, short weight, Edge firstInput, Edge secondInput, BucketHeap worklist) {
			return this.graph.addEdge(source, sink, symbol, field, weight, firstInput, secondInput, worklist);
		}
		
		public Vertex getVertex(String name) {
			return this.graph.getVertex(name);
		}
	}
	
	public interface GraphTransformer {
		public Graph2 transform(ContextFreeGrammarOpt c, Iterable<EdgeStruct> edges);
	}
	
	public static abstract class EdgeTransformer implements GraphTransformer {
		public abstract void process(GraphBuilder gb, EdgeStruct edgeStruct);
		
		public Graph2 transform(ContextFreeGrammarOpt c, Iterable<EdgeStruct> edges) {
			GraphBuilder gb = new GraphBuilder(c);
			for(EdgeStruct edge : edges) {
				this.process(gb, edge);
			}
			return gb.getGraph();
		}
	}
	
	private final Map<String,Vertex> vertices = new HashMap<String,Vertex>();
	private final ContextFreeGrammarOpt c;
	
	public Graph2(ContextFreeGrammarOpt c) {
		this.c = c;
	}

	public Graph2(ContextFreeGrammarOpt c, RelationManager relations) {
		this.c = c;
		for(int i=0; i<c.getNumLabels(); i++) {
			String symbol = c.getSymbol(i).symbol;
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				this.readRelation(relation);
			}
		}
	}
	
	public Graph2 transform(GraphTransformer transformer) {
		return transformer.transform(this.c, this.getEdgeStructs());
	}
	
	public Iterable<EdgeStruct> getEdgeStructs() {
		return new EdgeStructIterator();
	}
	
	public Iterable<Edge> getEdges() {
		return new EdgeIterator();
	}
	
	public ContextFreeGrammarOpt getContextFreeGrammarOpt() {
		return this.c;
	}
	
	public static interface Filter<T> {
		public boolean filter(T t);
	}
	
	public static class FilterTransformer extends EdgeTransformer {
		private final Filter<EdgeStruct> filter;
		
		public FilterTransformer(Filter<EdgeStruct> filter) {
			this.filter = filter;
		}

		@Override
		public void process(GraphBuilder gb, EdgeStruct edgeStruct) {
			if(this.filter.filter(edgeStruct)) {
				gb.addEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, Field.getField(edgeStruct.field), edgeStruct.weight);
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
	
	private void readRelation(Relation relation) {
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getName());
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			if(!relation.filter(tuple)) {
				continue;
			}
		
			String source = relation.getSource(tuple);
			String sink = relation.getSink(tuple);
			String symbol = relation.getSymbol();
			Field field = Field.getField(relation.getField(tuple).field);
			//Context context = relation.getContext(tuple);
			short weight = (short)relation.getWeight(tuple);
			
			this.addEdge(source, sink, symbol, field, weight);
		}
		
		rel.close();
	}
	
	public Vertex getVertex(String name) {
		Vertex vertex = this.vertices.get(name);
		if(vertex == null) {
			vertex = new Vertex(name, c.getNumLabels());
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
	
	private boolean addEdge(String source, String sink, String symbol, Field field, short weight) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.c.getSymbol(symbol), field, weight, null, null, null);		
	}
	
	private boolean addEdge(Vertex source, Vertex sink, Symbol symbol, Field field, short weight, Edge firstInput, Edge secondInput, BucketHeap worklist) {
		Edge edge = new Edge(symbol, source, sink, field);
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
			this.current = this.vertexList.length>0 && c.getNumLabels()>0 ? this.vertexList[0].getIncomingEdges(0).iterator() : null;
			this.increment();
		}
		
		@Override
		public boolean hasNext() {
			return this.current != null;
		}
		
		private void incrementIndex() {
			if(++this.curSymbolInt == c.getNumLabels()) {
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
	
	public class FilteredEdgeIterator implements Iterator<Edge>, Iterable<Edge> {
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