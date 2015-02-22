package stamp.missingmodels.util.jcflsolver2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager.Relation;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeStruct;

public class Graph2 {
	public static class GraphBuilder {
		private final Graph2 graph;
		
		public GraphBuilder(ContextFreeGrammarOpt c) {
			this.graph = new Graph2(c);
		}
		
		public Graph2 getGraph() {
			return this.graph;
		}
		
		public boolean addInputEdge(String source, String sink, String symbol, int field, short weight) {
			return this.graph.addInputEdge(source, sink, symbol, field, weight);
		}
		
		public boolean addEdge(Vertex source, Vertex sink, int symbolInt, int field, short weight, Edge firstInput, Edge secondInput, BucketHeap worklist) {
			return this.graph.addEdge(source, sink, symbolInt, field, weight, firstInput, secondInput, worklist);
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
	
	private final Map<String,Vertex> nodes = new HashMap<String,Vertex>();
	private final ContextFreeGrammarOpt c;
	
	public Graph2(ContextFreeGrammarOpt c) {
		this.c = c;
	}

	public Graph2(ContextFreeGrammarOpt c, RelationManager relations) {
		this.c = c;
		for(int i=0; i<c.getNumLabels(); i++) {
			String symbol = c.getSymbol(i);
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
			int field = relation.getField(tuple).field;
			//Context context = relation.getContext(tuple);
			short weight = (short)relation.getWeight(tuple);
			
			this.addInputEdge(source, sink, symbol, field, weight);
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
	
	private boolean addInputEdge(String source, String sink, String symbol, int field, short weight) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.c.getSymbolInt(symbol), field, (short)weight, null, null, null);		
	}
	
	private boolean addEdge(Vertex source, Vertex sink, int symbolInt, int field, short weight, Edge firstInput, Edge secondInput, BucketHeap worklist) {
		Edge edge = new Edge(this.c, symbolInt, source, sink, field);
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
		
		public EdgeStruct next() {
			Edge result = this.current.next();
			this.increment();
			return result.getStruct();
		}

		public void remove() {
			throw new RuntimeException("Remove is not implemented for EdgeSet!");
		}

		@Override
		public Iterator<EdgeStruct> iterator() {
			return this;
		}
	}
}