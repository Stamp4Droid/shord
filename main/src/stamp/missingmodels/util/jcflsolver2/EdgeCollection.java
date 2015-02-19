package stamp.missingmodels.util.jcflsolver2;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface EdgeCollection extends Iterable<Edge> {
	public void add(Edge edge);

	public static final EdgeCollection EMPTY_EDGES = new EdgeCollection() {
		private final Iterator<Edge> emptyIterator = new Iterator<Edge>() {
			public boolean hasNext() { return false; }
			public Edge next() { throw new NoSuchElementException(); }
			public void remove() { throw new RuntimeException("Remove not implemented!"); }
		};
		@Override
		public void add(Edge edge) { throw new RuntimeException("Cannot add edge to empty set"); }
		public Iterator<Edge> iterator() {
			return emptyIterator;
		}
	};
	
	public static final class EdgeList implements EdgeCollection {
		private Edge head = null;

		public Iterator<Edge> iterator() {
			return new EdgeListIterator();
		}

		@Override
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
}