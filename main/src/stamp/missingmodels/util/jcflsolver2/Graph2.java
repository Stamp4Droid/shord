package stamp.missingmodels.util.jcflsolver2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.Util.MultivalueMap;
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
	
	public static class TypeFilter {
		public final MultivalueMap<String,String> filter = new MultivalueMap<String,String>();
		public final int flowSymbolId;
		
		public TypeFilter(ContextFreeGrammarOpt c) {
			this.flowSymbolId = c.getSymbol("Flow").id;
			
			final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("ptd");
			rel.load();
			
			Iterable<int[]> res = rel.getAryNIntTuples();
			for(int[] tuple : res) {
				this.add("H" + Integer.toString(tuple[1]), "V" + Integer.toString(tuple[0]));
			}
			
			rel.close();
		}
		
		public void add(String h, String v) {
			this.filter.add(h, v);
		}
		
		public boolean filter(String source, String sink, int symbolInt) {
			return symbolInt != this.flowSymbolId || this.filter.get(source).contains(sink);
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
	
	private final Map<String,Vertex> nodes = new HashMap<String,Vertex>();
	private final ContextFreeGrammarOpt c;
	private final TypeFilter t;
	
	public Graph2(ContextFreeGrammarOpt c) {
		this.c = c;
		this.t = new TypeFilter(c);
	}

	public Graph2(ContextFreeGrammarOpt c, RelationManager relations) {
		this.c = c;
		this.t = new TypeFilter(c);
		for(int i=0; i<c.getNumLabels(); i++) {
			String symbol = c.getSymbol(i).symbol;
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				this.readRelation(relation);
			}
		}
	}
	
	public Graph2 transform(GraphTransformer transformer) {
		return transformer.transform(this.c, this.getEdges());
	}
	
	public Iterable<EdgeStruct> getEdges() {
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
	
	public Iterable<EdgeStruct> getEdges(Filter<EdgeStruct> filter) {
		return new FilteredEdgeIterator(filter);
	}

	public String toString(boolean shord) {
		StringBuilder sb = new StringBuilder();
		for(EdgeStruct edge : this.getEdges()) {
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
				return;
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
		Vertex vertex = this.nodes.get(name);
		if(vertex == null) {
			vertex = new Vertex(name, c.getNumLabels());
			this.nodes.put(name, vertex);
		}
		return vertex;
	}
	
	private boolean addEdge(String source, String sink, String symbol, Field field, short weight) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.c.getSymbol(symbol), field, (short)weight, null, null, null);		
	}
	
	private boolean addEdge(Vertex source, Vertex sink, Symbol symbol, Field field, short weight, Edge firstInput, Edge secondInput, BucketHeap worklist) {
		/*
		if(!this.t.filter(source.name, sink.name, symbolInt)) {
			return false;
		}
		*/
		
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
	
	private class EdgeIterator implements Iterator<EdgeStruct>, Iterable<EdgeStruct> {
		private Vertex[] vertices = nodes.values().toArray(new Vertex[]{});
		private int curVertex = 0;
		private int curSymbolInt = 0;
		private Iterator<Edge> current;
		
		public EdgeIterator() {
			this.current = this.vertices.length>0 && c.getNumLabels()>0 ? this.vertices[0].getIncomingEdges(0).iterator() : null;
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
				this.current = this.curVertex<this.vertices.length ? this.vertices[this.curVertex].getIncomingEdges(this.curSymbolInt).iterator() : null;
			}
		}
		
		@Override
		public EdgeStruct next() {
			Edge result = this.current.next();
			this.increment();
			return result.getStruct();
		}
		
		@Override
		public void remove() {
			throw new RuntimeException("Remove is not implemented for EdgeIterator!");
		}

		@Override
		public Iterator<EdgeStruct> iterator() {
			return this;
		}
	}
	
	public class FilteredEdgeIterator implements Iterator<EdgeStruct>, Iterable<EdgeStruct> {
		private final EdgeIterator iterator = new EdgeIterator();
		private final Filter<EdgeStruct> filter;
		private EdgeStruct curEdge;
		
		public FilteredEdgeIterator(Filter<EdgeStruct> filter) {
			this.filter = filter;
			this.increment();
		}
		
		@Override
		public boolean hasNext() {
			return this.curEdge != null;
		}
		
		@Override
		public EdgeStruct next() {
			EdgeStruct result = this.curEdge;
			this.increment();
			return result;
		}
		
		@Override
		public void remove() {
			throw new RuntimeException("Remove is not implemented for FilteredEdgeIterator!");
		}
		
		@Override
		public Iterator<EdgeStruct> iterator() {
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