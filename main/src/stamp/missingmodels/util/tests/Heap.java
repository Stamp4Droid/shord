package stamp.missingmodels.util.tests;

public interface Heap<T> {
	public void add(T t, short weight);
	public void remove(Object t, short weight);
	public T pop();
	public int size();
}
