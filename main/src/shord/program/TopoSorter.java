package shord.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopoSorter<T> {
	private static class CycleException extends Exception {}

	private Collection<T> nodes;
	private Map<T,Set<T>> edges;
	private List<T> res = new ArrayList<T>();
	private Set<T> visited = new HashSet<T>();
	private Set<T> processing = new HashSet<T>();

	public TopoSorter(Collection<T> nodes, Map<T,Set<T>> edges) {
		this.nodes = nodes;
		this.edges = edges;
	}

	private void visit(T src) throws CycleException {
		if (processing.contains(src)) {
			throw new CycleException();
		}
		if (visited.contains(src)) {
			return;
		}
		processing.add(src);
		for (T tgt : edges.get(src)) {
			visit(tgt);
		}
		visited.add(src);
		processing.remove(src);
		res.add(src);
	}

	public boolean sort() {
		try {
			for (T n : nodes) {
				visit(n);
			}
		} catch (CycleException exc) {
			return false;
		}
		Collections.reverse(res);
		return true;
	}

	public List<T> result() {
		return res;
	}
}
