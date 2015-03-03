package stamp.missingmodels.util.cflsolver.solver;

public interface Heap<T> {
	public void add(T t, int priority);
	public T peek();
	public T removeFirst();
	public int size();
	public boolean isEmpty();
}
