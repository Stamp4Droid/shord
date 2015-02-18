package stamp.missingmodels.util.jcflsolver2;

abstract class EdgesCustom implements EdgeCollection
{
	private final Next next;

	EdgesCustom(boolean useNextA) {
		if(useNextA)
			this.next = new NextA();
		else
			this.next = new NextB();
	}
	
	protected final Edge getNext(Edge e) {
		return next.getNext(e);
	}

	protected final void setNext(Edge edge, Edge nextEdge) {
		next.setNext(edge, nextEdge);
	}

	interface Next {
		public Edge getNext(Edge e);
		public void setNext(Edge e, Edge next);
	}

	private class NextA implements Next {
		public Edge getNext(Edge e){ return e.nextOutgoingEdge; }
		public void setNext(Edge e, Edge next){ e.nextOutgoingEdge = next; }
	}

	private class NextB implements Next {
		public Edge getNext(Edge e){ return e.nextIncomingEdge; }
		public void setNext(Edge e, Edge next){ e.nextIncomingEdge = next; }
	}

}
