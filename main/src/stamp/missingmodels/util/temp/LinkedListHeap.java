package stamp.missingmodels.util.temp;

import java.util.LinkedList;

public class LinkedListHeap<T> extends LinkedList<T> implements Heap<T> {
	private static final long serialVersionUID = 2393030812340710078L;

	@Override
	public void add(T t, short weight) {
		super.add(t);
	}

	@Override
	public void remove(Object t, short weight) {
		super.remove(t);
	}
}
