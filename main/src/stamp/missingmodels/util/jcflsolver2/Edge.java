package stamp.missingmodels.util.jcflsolver2;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Edge {
	public final Vertex source;
	public final Vertex sink;
	public final int symbolInt;
	public final int field;
	
	public short weight;
	public Edge firstInput;
	public Edge secondInput;
	
	public Edge nextWorklist;
	public Edge prevWorklist;

	public Edge nextOutgoingEdge;
	public Edge nextIncomingEdge;
	
	Edge(int symbolInt, Vertex source, Vertex sink) {
		this(symbolInt, source, sink, -1);
	}

	public Edge(int symbolInt, Vertex source, Vertex sink, int field) {
		this.source = source;
		this.sink = sink;
		this.symbolInt = symbolInt;
		this.field = field;
	}

	public static final Iterable<Edge> EMPTY_EDGES = new Iterable<Edge>() {
		private final Iterator<Edge> emptyIterator = new Iterator<Edge>() {
			public boolean hasNext() { return false; }
			public Edge next() { throw new NoSuchElementException(); }
			public void remove() { throw new RuntimeException("Remove not implemented!"); }
		};
		public Iterator<Edge> iterator() {
			return emptyIterator;
		}
	};

	public static final class EdgeList implements Iterable<Edge> {
		private Edge head = null;

		public Iterator<Edge> iterator() {
			return new EdgeListIterator();
		}

		public void add(Edge edge) {
			Edge edgeWithData = edge;
			edgeWithData.nextIncomingEdge = this.head;
			this.head = edgeWithData;
		}

		private class EdgeListIterator implements Iterator<Edge> {
			private Edge current;

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
				Edge result = this.current;
				this.current = this.current.nextIncomingEdge;
				return result;
			}

			public void remove() {
				throw new RuntimeException("Remove is not implemented for EdgeList!");
			}
		}
	}

	public static class EdgeSet implements Iterable<Edge> {
		private static final double MAX_LOAD_FACTOR = 0.9;
		private static final int INITIAL_TABLE_SIZE = 16;

		private Edge[] table = new Edge[INITIAL_TABLE_SIZE];
		private int size = 0;

		public Iterator<Edge> iterator() {
			return new EdgeSetIterator();
		}

		public void add(Edge edge) {
			if(this.get(edge.sink, edge.field) != null) {
				throw new RuntimeException("Adding duplicate edge!");
			}
			double loadFactor = (double)++size/this.table.length;
			if (loadFactor > MAX_LOAD_FACTOR) {
				this.expandCapacity();
			}
			int index = edge.sink.id%this.table.length;
			edge.nextOutgoingEdge = this.table[index];
			this.table[index] = edge;
		}

		public Edge get(Vertex sink, int field) {
			Edge e = this.table[sink.id%this.table.length];
			while(e != null) {
				// source should already be equal
				if(e.sink.id == sink.id && e.field == field) {
					return e;
				}
				e = e.nextOutgoingEdge;
			}
			return null;
		}

		private void expandCapacity() {
			Edge[] oldTable = this.table;
			int oldNumBuckets = oldTable.length;
			int newNumBuckets = oldNumBuckets<<1;
			this.table = new Edge[newNumBuckets];
			for(int i=0; i<oldNumBuckets; i++) {
				Edge e = oldTable[i];
				while(e != null) {
					int index = e.sink.id%newNumBuckets;
					Edge temp = e.nextOutgoingEdge;
					e.nextOutgoingEdge = this.table[index];
					this.table[index] = e;
					e = temp;
				}
			}
		}

		private class EdgeSetIterator implements Iterator<Edge> {
			private Edge current = null;
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
				Edge result = this.current;
				this.current = this.current.nextOutgoingEdge;
				while(this.current == null && ++this.index<table.length) {
					this.current = table[this.index];
				}
				return result;
			}

			public void remove() {
				throw new RuntimeException("Remove is not implemented for EdgeSet!");
			}
		}
	}
}