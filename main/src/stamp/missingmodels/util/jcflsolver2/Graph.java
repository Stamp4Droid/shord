package stamp.missingmodels.util.jcflsolver2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager.Relation;

public class Graph implements Iterable<Edge> {
	public Map<String,Vertex> nodes = new HashMap<String,Vertex>();
	public final ContextFreeGrammarOpt c;
	
	public Graph(ContextFreeGrammarOpt c) {
		this.c = c;
	}

	public Graph(ContextFreeGrammarOpt c, RelationManager relations) {
		this.c = c;
		for(int i=0; i<c.getNumLabels(); i++) {
			String symbol = c.getSymbol(i);
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				this.readRelation(relation);
			}
		}
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
	
	public boolean addInputEdge(String source, String sink, String symbol, int field, short weight) {
		return this.addEdge(this.getVertex(source), this.getVertex(sink), this.c.getSymbolInt(symbol), field, (short)weight, null, null, null);		
		/*
		Edge edge = new Edge(symbolInt, this.getVertex(sourceName), this.getVertex(sinkName), field);
		edge.weight = weight;
		
		Edge oldEdge = this.getVertex(sourceName).getCurrentOutgoingEdge(edge);
		if(oldEdge == null) {
			this.getVertex(sourceName).addOutgoingEdge(edge);
			this.getVertex(sinkName).addIncomingEdge(edge);
		}
		*/
	}
	
	public boolean addEdge(Vertex source, Vertex sink, int symbolInt, int field, short weight, Edge firstInput, Edge secondInput, BucketHeap worklist) {
		Edge edge = new Edge(symbolInt, source, sink, field);
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
	
	private class EdgeIterator implements Iterator<Edge> {
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
		
		public Edge next() {
			Edge result = this.current.next();
			this.increment();
			return result;
		}

		public void remove() {
			throw new RuntimeException("Remove is not implemented for EdgeSet!");
		}
	}

	@Override
	public Iterator<Edge> iterator() {
		return new EdgeIterator();
	}
}