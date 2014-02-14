package stamp.missingmodels.util.cflsolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BucketHeap<T> implements Heap<T> {
	private List<Set<T>> buckets = new ArrayList<Set<T>>();
	private int minBucket = 0;
	private int size = 0;
	
	public void ensure(int index) {
		for(int i=this.buckets.size(); i<=index; i++) {
			this.buckets.add(null);
		}
	}

	public void add(T t, short weight) {
		this.ensure(weight);
		Set<T> cur = this.buckets.get(weight);
		if(cur == null) {
			cur = new HashSet<T>();
			this.buckets.set(weight, cur);
		}
		if(cur.add(t)) {
			this.size++;
		}
	}

	public T pop() {
		Set<T> cur = this.buckets.get(this.minBucket);
		while(cur == null || cur.isEmpty()) {
			this.minBucket++;
			if(this.minBucket >= this.buckets.size()) {
				return null;
			}
			cur = this.buckets.get(this.minBucket);
		}
		
		T t = null;
		for(T tt : cur) {
			t = tt;
			break;
		}
		cur.remove(t);
		this.size--;
		return t;
	}

	public int size() {
		return this.size;
	}

	@Override
	public void remove(Object t, short weight) {
		this.buckets.get(weight).remove(t);		
	}
}

