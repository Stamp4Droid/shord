package stamp.missingmodels.util.cflsolver.solver;

import java.util.LinkedList;

public class TestHeap<T> implements Heap<T> {
	private LinkedList<T> heap = new LinkedList<T>();
	
	public void add(T t, int priority) {
		this.heap.add(t);
	}
	
	public T peek() {
		return this.heap.peek();
	}
	
	public T removeFirst() {
		return this.heap.removeFirst();
	}
	
	public int size() {
		return this.heap.size();
	}
	
	public boolean isEmpty() {
		return this.heap.isEmpty();
	}
}
