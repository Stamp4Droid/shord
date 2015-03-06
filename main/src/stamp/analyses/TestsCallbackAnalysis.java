package stamp.analyses;

import java.io.IOException;
import java.util.List;

import shord.project.analyses.JavaAnalysis;
import stamp.analyses.TestsAnalysis.CallgraphCompleter;
import stamp.analyses.TestsAnalysis.TestAbductiveInferenceHelper;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference.AbductiveInferenceHelper;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.Filter;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.grammars.CallgraphTaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
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
						return (edge.symbol.symbol.equals("param") || edge.symbol.symbol.equals("paramPrim"));
						//return (edge.symbol.symbol.equals("callgraph")) && !baseEdgeFilter.get(edge.source.toString(true)).contains(edge.sink.toString(true));
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
		try {
			MultivalueMap<String,String> result = new MultivalueMap<String,String>();
			for(Pair<String,String> pair : IOUtils.readGraphEdgesFromFile(relationName, extension).keySet()) {
				result.add(pair.getX(), pair.getY());
			}
			return result;
		} catch(IOException e) {
			e.printStackTrace();
			return new MultivalueMap<String,String>();
		}
	}

	@Override
	public void run() {
		//MultivalueMap<String,String> paramEdges = getGraphEdgesFromFile("param", "graph");
		//MultivalueMap<String,String> paramPrimEdges = getGraphEdgesFromFile("paramPrim", "graph");
		MultivalueMap<String,String> callgraphEdges = getGraphEdgesFromFile("callgraph", "graph");
		MultivalueMap<String,String> sourceSinkEdges = getGraphEdgesFromFile("Src2Sink", "graph");
		
		String[] tokens = System.getProperty("stamp.out.dir").split("_");
		
		List<String> reachedMethods = TraceReader.getReachableMethods("profiler/traceouts/", tokens[tokens.length-1]);
		int numReachableMethods = TestsAnalysis.getNumReachableMethods();
		
		List<Pair<String,String>> testReachedCallgraph = TraceReader.getOrderedCallgraph("profiler/traceouts", tokens[tokens.length-1]);
		testReachedCallgraph.addAll(TraceReader.getOrderedCallgraph("profiler/traceouts_test", tokens[tokens.length-1]));
		
		MultivalueMap<String,String> callgraph = new CallgraphCompleter().completeDynamicCallgraph(TraceReader.getCallgraph("profiler/traceouts/", tokens[tokens.length-1]));		
		List<String> testReachedMethods = TraceReader.getReachableMethods("profiler/traceouts_test/", tokens[tokens.length-1]);

		System.out.println("Method coverage: " + reachedMethods.size());
		System.out.println("Number of reachable methods: " + numReachableMethods);
		System.out.println("Percentage method coverage: " + (double)reachedMethods.size()/numReachableMethods);
		System.out.println("Test number of reached callgraph edges: " + testReachedCallgraph.size());
		System.out.println("Test coverage: " + (double)testReachedMethods.size()/numReachableMethods);
		
		for(String key : callgraphEdges.keySet()) {
			callgraph.get(key).addAll(callgraphEdges.get(key));
		}
		
		//AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(getUnion(paramEdges, paramPrimEdges), sourceSinkEdges) : new DefaultAbductiveInferenceHelper();
		//AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(callgraphEdges, sourceSinkEdges) : new DefaultAbductiveInferenceHelper();
		AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(callgraph, sourceSinkEdges) : new TestAbductiveInferenceHelper();
		
		ContextFreeGrammar taintGrammar = new CallgraphTaintGrammar();
		RelationReader relationReader = new ShordRelationReader();
		
		Graph g = relationReader.readGraph(new DynamicCallgraphRelationManager(callgraph), taintGrammar.getSymbols());
		MultivalueMap<EdgeStruct,Integer> results = new AbductiveInference(taintGrammar.getOpt(), h).process(g, relationReader.readFilter(g.getVertices(), taintGrammar.getSymbols()), 2);
		IOUtils.printCallgraphAbductionResult(results, true);
		
		Graph gbar = g.transform(new ReachabilitySolver(g.getVertices(), taintGrammar.getOpt(), relationReader.readFilter(g.getVertices(), taintGrammar.getSymbols())));
		String extension = IOUtils.graphEdgesFileExists("param", "graph") ? "graph_new" : "graph";
		
		try {
			IOUtils.printGraphEdgesToFile(gbar, "param", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "paramPrim", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "callgraph", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "Src2Sink", true, extension);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
