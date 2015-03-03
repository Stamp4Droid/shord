package stamp.missingmodels.util.cflsolver.solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BucketHeap<T> implements Heap<T> {
	private List<Set<T>> buckets = new LinkedList<Set<T>>();
	private Map<T,Integer> tToBucket = new HashMap<T,Integer>();
	private int minBucket = 0;
	
	private void ensure(int weight) {
		for(int i=this.buckets.size(); i<=weight; i++) {
			this.buckets.add(null);
		}
	}

	public void add(T t, int weight) {
		this.ensure(weight);
		
		// STEP 1: Remove t if it is already in the heap
		Integer prevBucket = this.tToBucket.get(t);
		if(prevBucket != null) {
			this.buckets.get(prevBucket).remove(t);
			this.tToBucket.remove(t);
		}
		
		// STEP 2: Get the new bucket to put t in
		Set<T> bucket = this.buckets.get(weight);
		if(bucket == null) {
			bucket = new HashSet<T>();
			this.buckets.set(weight, bucket);
		}
		
		// STEP 3: Put t in the bucket (and update the map)
		bucket.add(t);
		this.tToBucket.put(t, weight);
		
		// STEP 4: Update minBucket if needed
		if(weight < this.minBucket) {
			this.minBucket = weight;
		}
	}
	
	@Override
	public T peek() {
		// STEP 1: Find the minimum bucket
		Set<T> bucket = this.buckets.get(this.minBucket);
		while(bucket == null || bucket.isEmpty()) {
			this.minBucket++;
			if(this.minBucket >= this.buckets.size()) {
				return null;
			}
			bucket = this.buckets.get(this.minBucket);
		}
		
		// STEP 2: Return an element
		for(T t : bucket) {
			return t;
		}
		throw new RuntimeException("No element found!");
	}
	
	public T removeFirst() {
		// STEP 1: Get the minimum item
		T t = this.peek();
		
		// STEP 2: Remove item from bucket and map
		this.buckets.get(this.tToBucket.get(t)).remove(t);
		this.tToBucket.remove(t);
		
		return t;
	}
	
	@Override
	public int size() {
		return this.tToBucket.size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.tToBucket.isEmpty();
	}
	
	/*
	public static void main(String[] args) {
		Heap<Integer> heap = new BucketHeap<Integer>();
		heap.add(2, 2);
		heap.add(1, 1);
		System.out.println(heap.removeFirst());
		System.out.println(heap.removeFirst());
		System.out.println(heap.size());
		System.out.println(heap.isEmpty());
		heap.add(1, 4);
		heap.add(2, 3);
		System.out.println(heap.removeFirst());
		System.out.println(heap.removeFirst());
	}
	*/
}

