package stamp.missingmodels.util.jcflsolver2;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface EdgeCollection extends Iterable<Edge> {
	public Edge add(Edge edge);

	public static final EdgeCollection EMPTY_EDGES = new EdgeCollection() {
		private final Iterator<Edge> emptyIterator = new Iterator<Edge>() {
			public boolean hasNext() { return false; }
			public Edge next() { throw new NoSuchElementException(); }
			public void remove() { throw new RuntimeException("Remove not implemented!"); }
		};
		@Override
		public Edge add(Edge edge) { throw new RuntimeException("Cannot add edge to empty set"); }
		public Iterator<Edge> iterator() {
			return emptyIterator;
		}
	};
	
	public static final class EdgeSet implements EdgeCollection {
		private static final double MAX_LOAD_FACTOR = 0.9;
		private static final int INITIAL_TABLE_SIZE = 16;

		private Edge[] table = new Edge[INITIAL_TABLE_SIZE];
		private int size = 0;

		@Override
		public Iterator<Edge> iterator() {
			return new EdgeSetIterator();
		}

		@Override
		public Edge add(Edge edge) {
			Edge edgeWithData = this.get(edge);
			if(edgeWithData != null) {
				return edgeWithData;
			}
			// need to add the edge
			int numBuckets = this.table.length;
			double loadFactor = (double)(++this.size)/numBuckets;
			if (loadFactor > MAX_LOAD_FACTOR) {
				this.expandCapacity();
			}
			int index = edge.sink.id%numBuckets;
			edge.nextOutgoingEdge = this.table[index];
			this.table[index] = edge;
			return edge;
		}
		
		private Edge get(Edge edge) {
			Edge e = this.table[edge.sink.id%this.table.length];
			while(e != null) {
				// source should already be equal
				if(e.sink == edge.sink && e.symbolInt == edge.symbolInt) {
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
	
	public static final class EdgeList implements EdgeCollection {
		private Edge head = null;

		public Iterator<Edge> iterator() {
			return new EdgeListIterator();
		}

		@Override
		public Edge add(Edge edge) {
			Edge edgeWithData = edge;
			edgeWithData.nextIncomingEdge = this.head;
			this.head = edgeWithData;
			return null;
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
}