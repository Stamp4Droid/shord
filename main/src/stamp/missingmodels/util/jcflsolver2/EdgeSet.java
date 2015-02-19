package stamp.missingmodels.util.jcflsolver2;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EdgeSet implements EdgeCollection {
	private static final double MAX_LOAD_FACTOR = 0.9;
	private static final int INITIAL_TABLE_SIZE = 16;
	
	private Edge[] table = new Edge[INITIAL_TABLE_SIZE];
	private int size = 0;
	
	public Iterator<Edge> iterator() {
		return new EdgeSetIterator();
	}

	public void add(Edge edge) {
		if(this.get(edge) != null) {
			throw new RuntimeException();
		}
		double loadFactor = (double)++size/this.table.length;
		if (loadFactor > MAX_LOAD_FACTOR) {
			this.expandCapacity();
		}
		int index = edge.sink.id%this.table.length;
		edge.nextOutgoingEdge = this.table[index];
		this.table[index] = edge;
	}
	
	public Edge get(Edge edge) {
		Edge e = this.table[edge.sink.id%this.table.length];
		while(e != null) {
			// source should already be equal
			if(e.sink.id == edge.sink.id && e.symbolInt == edge.symbolInt && e.field == edge.field) {
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
