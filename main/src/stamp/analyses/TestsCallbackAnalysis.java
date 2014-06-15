package stamp.analyses;

import java.io.IOException;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner.AbductiveInferenceHelper;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner.DefaultAbductiveInferenceHelper;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeFilter;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPointsToRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "tests-callback")
public class TestsCallbackAnalysis extends JavaAnalysis {
	private AbductiveInferenceHelper getAbductiveInferenceHelper(final MultivalueMap<String,String> baseEdgeFilter, final MultivalueMap<String,String> cutEdgeFilter) {
		return new AbductiveInferenceHelper() {
			@Override
			public Set<Edge> getBaseEdges(Graph g) {
				return g.getEdges(new EdgeFilter() {
					@Override
					public boolean filter(Edge edge) {
						return (edge.getSymbol().equals("param") || edge.getSymbol().equals("paramPrim"))
								&& !baseEdgeFilter.get(edge.source.toString(true)).contains(edge.sink.toString(true));
					}
				});
			}

			@Override
			public Set<Edge> getInitialCutEdges(Graph g) {
				return g.getEdges(new EdgeFilter() {
					@Override
					public boolean filter(Edge edge) {
						return edge.getSymbol().equals("Src2Sink")
								&& !cutEdgeFilter.get(edge.source.toString(true)).contains(edge.sink.toString(true));
					}
				});
			}
		};
	}
	
	private static MultivalueMap<String,String> getGraphEdgesFromFile(String relationName, String extension) throws IOException {
		MultivalueMap<String,String> result = new MultivalueMap<String,String>();
		for(Pair<String,String> pair : IOUtils.readGraphEdgesFromFile(relationName, extension).keySet()) {
			result.add(pair.getX(), pair.getY());
		}
		return result;
	}
	
	@SafeVarargs
	private static MultivalueMap<String,String> getUnion(MultivalueMap<String,String> ... maps) {
		MultivalueMap<String,String> result = new MultivalueMap<String,String>();
		for(MultivalueMap<String,String> map : maps) {
			for(String source : map.keySet()) {
				for(String sink : map.get(source)) {
					result.add(source, sink);
				}
			}
		}
		return result;
	}

	@Override
	public void run() {
		try {
			AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(getUnion(getGraphEdgesFromFile("param", "graph"), getGraphEdgesFromFile("paramPrim", "graph")), getGraphEdgesFromFile("Src2Sink", "graph")) : new DefaultAbductiveInferenceHelper();
			
			ContextFreeGrammar taintGrammar = new TaintPointsToGrammar();
			RelationReader relationReader = new ShordRelationReader();
			Graph g = relationReader.readGraph(new TaintPointsToRelationManager(), taintGrammar);
			TypeFilter t = relationReader.readTypeFilter(taintGrammar);
			IOUtils.printAbductionResult(AbductiveInferenceRunner.runInference(h, g, t, true, 2), true);

			Graph gbar = new ReachabilitySolver(g, t).getResult();
			String extension = IOUtils.graphEdgesFileExists("param", "graph") ? "graph_new" : "graph";
			IOUtils.printGraphEdgesToFile(gbar, "param", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "paramPrim", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "Src2Sink", true, extension);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
