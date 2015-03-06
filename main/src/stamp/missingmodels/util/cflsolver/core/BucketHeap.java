package stamp.missingmodels.util.cflsolver.core;

import java.util.ArrayList;
import java.util.List;

public class BucketHeap {
	private List<Edge> buckets = new ArrayList<Edge>();
	private int minBucket = 0;
	private int size = 0;
	
	public void ensure(int index) {
		for(int i=this.buckets.size(); i<=index; i++) {
			this.buckets.add(null);
		}
	}

	public void push(Edge t) {
		if(t.weight<this.minBucket) {
			throw new RuntimeException("Edge weight lower than current minimum bucket!");
		}
		this.ensure(t.weight);
		Edge head = this.buckets.get(t.weight);
		if(head != null) {
			t.nextWorklist = head;
			head.prevWorklist = t;
		} else {
			t.nextWorklist = null;
		}
		t.prevWorklist = null;
		this.buckets.set(t.weight, t);
		this.size++;
	}

	public Edge pop() {
		Edge head = this.buckets.get(this.minBucket);
		while(head == null) {
			this.minBucket++;
			if(this.minBucket >= this.buckets.size()) {
				return null;
			}
			head = this.buckets.get(this.minBucket);
		}
		this.buckets.set(this.minBucket, head.nextWorklist);
		this.size--;
		return head;
	}

	public void update(Edge edge, short oldWeight) {
		if(edge.prevWorklist != null) {
			edge.prevWorklist.nextWorklist = edge.nextWorklist;
		} else {
			this.buckets.set(oldWeight, edge.nextWorklist);
		}
		if(edge.nextWorklist != null) {
			edge.nextWorklist.prevWorklist = edge.prevWorklist;
		}
		this.size--;
		this.push(edge);
	}

	public int size() {
		return this.size;
	}
}

