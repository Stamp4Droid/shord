package stamp.missingmodels.util.jcflsolver2;

import java.util.*;

public class EdgesLinkedList extends LinkedList<Edge> implements Edges
{
	public Edge addEdge(Edge edge)
	{
		super.add(edge);
		return null;
	}
}