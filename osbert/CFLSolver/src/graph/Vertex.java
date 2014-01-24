package graph;

import java.util.LinkedList;
import java.util.List;

public final class Vertex {
	public final List[] incomingEdgesByLabel; // list of type Edge
	public final List[] outgoingEdgesByLabel; // list of type Edge
	
	public Vertex(int numLabels) {
		this.incomingEdgesByLabel = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.incomingEdgesByLabel[i] = new LinkedList();
		}
		this.outgoingEdgesByLabel = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.outgoingEdgesByLabel[i] = new LinkedList();
		}
	}
}
