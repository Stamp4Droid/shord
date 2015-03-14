package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.analyses.TestsAnalysis.TestAbductiveInferenceHelper;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference.AbductiveInferenceHelper;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.Filter;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicParamRelationManager;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "tests-callback")
public class TestsCallbackAnalysis extends JavaAnalysis {
	private AbductiveInferenceHelper getAbductiveInferenceHelper(final MultivalueMap<String,String> baseEdgeFilter, final MultivalueMap<String,String> cutEdgeFilter) {
		return new AbductiveInferenceHelper() {
			@Override
			public Iterable<Edge> getBaseEdges(Graph g) {
				return g.getEdges(new Filter<Edge>() {
					@Override
					public boolean filter(Edge edge) {
						return edge.symbol.symbol.equals("param") || edge.symbol.symbol.equals("paramPrim");
						//return ((edge.symbol.symbol.equals("param") || edge.symbol.symbol.equals("paramPrim")) && !baseEdgeFilter.get(edge.source.toString(true)).contains(edge.sink.toString(true)));
					}
				});
			}

			@Override
			public Iterable<Edge> getInitialEdges(Graph g) {
				return g.getEdges(new Filter<Edge>() {
					@Override
					public boolean filter(Edge edge) {
						return edge.symbol.symbol.equals("Src2Sink") && !cutEdgeFilter.get(edge.source.toString(true)).contains(edge.sink.toString(true));
					}
				});
			}
		};
	}

	private static MultivalueMap<String,String> getGraphEdgesFromFile(String relationName, String extension) {
		MultivalueMap<String,String> result = new MultivalueMap<String,String>();
		try {
			for(Pair<String,String> pair : IOUtils.readGraphEdgesFromFile(relationName, extension).keySet()) {
				result.add(pair.getX(), pair.getY());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public void run() {
		ContextFreeGrammarOpt grammar = new TaintPointsToGrammar().getOpt();
		RelationReader reader = new ShordRelationReader();
		RelationManager relations = new DynamicParamRelationManager(new MultivalueMap<String,String>());
		
		MultivalueMap<String,String> paramEdges = getGraphEdgesFromFile("param", "graph");
		MultivalueMap<String,String> paramPrimEdges = getGraphEdgesFromFile("paramPrim", "graph");
		MultivalueMap<String,String> sourceSinkEdges = getGraphEdgesFromFile("Src2Sink", "graph");	
		//AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(getUnion(paramEdges, paramPrimEdges), sourceSinkEdges) : new TestAbductiveInferenceHelper();
		AbductiveInferenceHelper h = new TestAbductiveInferenceHelper();
		
		Graph g = reader.readGraph(relations, grammar.getSymbols());
		Graph gbar = g.transform(new ReachabilitySolver(g.getVertices(), grammar, reader.readFilter(g.getVertices(), grammar.getSymbols())));
		
		String extension = IOUtils.graphEdgesFileExists("param", "graph") ? "graph_new" : "graph";
		IOUtils.printGraphEdgesToFile(gbar, "param", true, extension);
		IOUtils.printGraphEdgesToFile(gbar, "paramPrim", true, extension);
		IOUtils.printGraphEdgesToFile(gbar, "Src2Sink", true, extension);
		
		MultivalueMap<EdgeStruct,Integer> results = new AbductiveInference(grammar, h).process(g, reader.readFilter(g.getVertices(), grammar.getSymbols()), 2);
		IOUtils.printCallgraphAbductionResult(results, true);
	}
}
