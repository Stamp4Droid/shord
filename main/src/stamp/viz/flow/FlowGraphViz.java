package stamp.viz.flow;

import java.util.List;
import java.util.Set;

import stamp.jcflsolver.Util.Pair;
import stamp.viz.dot.DotObject;
import stamp.viz.dot.Viz;
import stamp.viz.flow.FlowGraphViz.FlowGraph;

public class FlowGraphViz extends Viz<FlowGraph> {
    public static class FlowGraph {
	public final List<Pair<String,String>> edges;
	public final Set<String> sources;
	public final Set<String> sinks;

	public FlowGraph(List<Pair<String,String>> edges, Set<String> sources, Set<String> sinks) {
	    this.edges = edges;
	    this.sources = sources;
	    this.sinks = sinks;
	}
    }

    public FlowGraphViz(String filename) {
	super(filename);
    }

    public static final FlowGraphViz viz = new FlowGraphViz("flowViz");
    @Override public DotObject vizDot(FlowGraph g) {
	DotObject dot = new DotObject("viz");
	for(String source : g.sources) {
	    dot.setNodeProperty(source, "color", "RED");
	}
	for(String sink : g.sinks) {
	    dot.setNodeProperty(sink, "color", "BLUE");
	}
	for(Pair<String,String> edge : g.edges) {
	    dot.addEdge(edge.getX(), edge.getY());
	}
	return dot;
    }
}
