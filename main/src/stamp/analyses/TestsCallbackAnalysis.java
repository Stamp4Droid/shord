package stamp.analyses;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG.EdgeFilter;
import stamp.analyses.TestsAnalysis.CallgraphCompleter;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.cflsolver.grammars.CallgraphTaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import stamp.missingmodels.util.jcflsolver2.AbductiveInferenceRunner2.AbductiveInferenceHelper;
import stamp.missingmodels.util.jcflsolver2.AbductiveInferenceRunner2.DefaultAbductiveInferenceHelper;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.Edge;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeStruct;
import stamp.missingmodels.util.jcflsolver2.Graph;
import stamp.missingmodels.util.jcflsolver2.Graph.Filter;
import stamp.missingmodels.util.jcflsolver2.ReachabilitySolver;
import stamp.missingmodels.util.jcflsolver2.RelationManager.RelationReader;
import stamp.missingmodels.util.jcflsolver2.TypeFilter;
import stamp.missingmodels.util.jcflsolver2.Util.MultivalueMap;
import stamp.missingmodels.util.jcflsolver2.Util.Pair;
import chord.project.Chord;

@Chord(name = "tests-callback")
public class TestsCallbackAnalysis extends JavaAnalysis {
	private AbductiveInferenceHelper getAbductiveInferenceHelper(final MultivalueMap<String,String> baseEdgeFilter, final MultivalueMap<String,String> cutEdgeFilter) {
		return new AbductiveInferenceHelper() {
			@Override
			public Iterable<Edge> getBaseEdges(Graph gbar, Graph gcur) {
				return gbar.getEdges(new Filter<Edge>() {
					@Override
					public boolean filter(Edge edge) {
						//return (edge.getSymbol().equals("param") || edge.getSymbol().equals("paramPrim"))
						return (edge.symbol.symbol.equals("callgraph"))
								&& !baseEdgeFilter.get(edge.source.toString(true)).contains(edge.sink.toString(true));
					}
				});
			}

			@Override
			public Iterable<Edge> getInitialCutEdges(Graph g) {
				return g.getEdges(new Filter<Edge>() {
					@Override
					public boolean filter(Edge edge) {
						return edge.symbol.symbol.equals("Src2Sink")
								&& !cutEdgeFilter.get(edge.source.toString(true)).contains(edge.sink.toString(true));
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
		/*
		try {
			MultivalueMap<String,String> paramEdges = getGraphEdgesFromFile("param", "graph");
			MultivalueMap<String,String> paramPrimEdges = getGraphEdgesFromFile("paramPrim", "graph");
			MultivalueMap<String,String> callgraphEdges = getGraphEdgesFromFile("callgraph", "graph");
			MultivalueMap<String,String> sourceSinkEdges = getGraphEdgesFromFile("Src2Sink", "graph");
			//AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(getUnion(paramEdges, paramPrimEdges), sourceSinkEdges) : new DefaultAbductiveInferenceHelper();
			//AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(callgraphEdges, sourceSinkEdges) : new DefaultAbductiveInferenceHelper();
			
			//IOUtils.printRelation("reachableM");
			//IOUtils.printRelation("callgraph");
			//IOUtils.printRelation("potentialCallbackDependent");
			
			//ContextFreeGrammar taintGrammar = new TaintPointsToGrammar();
			ContextFreeGrammar taintGrammar = new CallgraphTaintGrammar();
			RelationReader relationReader = new ShordRelationReader();
			
			/*** COPIED FROM TESTS ANALYSIS ***//*
			String[] tokens = System.getProperty("stamp.out.dir").split("_");
			
			List<String> reachedMethods = TraceReader.getReachableMethods("profiler/traceouts/", tokens[tokens.length-1]);
			int numReachableMethods = TestsAnalysis.getNumReachableMethods();

			List<Pair<String,String>> testReachedCallgraph = TraceReader.getOrderedCallgraph("profiler/traceouts", tokens[tokens.length-1]);
			testReachedCallgraph.addAll(TraceReader.getOrderedCallgraph("profiler/traceouts_test", tokens[tokens.length-1]));
			
			System.out.println("Method coverage: " + reachedMethods.size());
			System.out.println("Number of reachable methods: " + numReachableMethods);
			System.out.println("Percentage method coverage: " + (double)reachedMethods.size()/numReachableMethods);

			System.out.println("Test number of reached callgraph edges: " + testReachedCallgraph.size());
			
			MultivalueMap<String,String> callgraph = TraceReader.getCallgraph("profiler/traceouts/", tokens[tokens.length-1]);
			callgraph = new CallgraphCompleter().completeDynamicCallgraph(callgraph);
			//IOUtils.printRelation("callgraph");
			
			List<String> testReachedMethods = TraceReader.getReachableMethods("profiler/traceouts_test/", tokens[tokens.length-1]);
			System.out.println("Test coverage: " + (double)testReachedMethods.size()/numReachableMethods);

			//double fractionMethodIncrement = 0.1;
			double fractionMethodIncrement = (double)reachedMethods.size()/(0.75*numReachableMethods);
			//int numMethods = reachedMethods.size();
			int numMethods = 0;
			while(true) {
				double trueSize = numMethods >= reachedMethods.size() ? (double)reachedMethods.size() : (double)numMethods;
				System.out.println("Running method coverage: " + trueSize/numReachableMethods);

				/*** END COPIED FROM TESTS ANALYSIS ***//*
				MultivalueMap<String,String> newCallgraph = TestsAnalysis.getFilteredCallgraph(callgraph, reachedMethods, numMethods);
				for(String key : callgraphEdges.keySet()) {
					newCallgraph.get(key).addAll(callgraphEdges.get(key));
				}

				AbductiveInferenceHelper h = IOUtils.relationFileExists("param", "graph") ? getAbductiveInferenceHelper(newCallgraph, sourceSinkEdges) : new DefaultAbductiveInferenceHelper();
				
				//Graph g = relationReader.readGraph(new CallbackRelationManager(paramEdges, paramPrimEdges), taintGrammar);
				//Graph g = relationReader.readGraph(new DynamicCallgraphRelationManager(callgraphEdges), taintGrammar);
				Graph g = relationReader.readGraph(new DynamicCallgraphRelationManager(newCallgraph), taintGrammar);
				TypeFilter t = relationReader.readTypeFilter(taintGrammar);
				MultivalueMap<EdgeStruct,Integer> results = AbductiveInferenceRunner.runInference(h, g, t, true, 2); 
				IOUtils.printCallgraphAbductionResult(results, true);
			
				Graph gbar = new ReachabilitySolver(g, t).getResult();
				String extension = IOUtils.graphEdgesFileExists("param", "graph") ? "graph_new" : "graph";
				IOUtils.printGraphEdgesToFile(gbar, "param", true, extension);
				IOUtils.printGraphEdgesToFile(gbar, "paramPrim", true, extension);
				IOUtils.printGraphEdgesToFile(gbar, "callgraph", true, extension);
				IOUtils.printGraphEdgesToFile(gbar, "Src2Sink", true, extension);
				
				/*** COPIED FROM TESTS ANALYSIS ***//*
				Map<Integer,Integer> minTimeToCrash = new HashMap<Integer,Integer>();
				Map<Integer,String> callgraphEdgeToCrash = new HashMap<Integer,String>();
				for(EdgeStruct edge : results.keySet()) {
					for(int cut : results.get(edge)) {
						Integer curTimeToCrash = minTimeToCrash.get(cut);
						Pair<String,String> callgraphEdge = new Pair<String,String>(ConversionUtils.getMethodSig(edge.sourceName), ConversionUtils.getMethodSig(edge.sinkName));
						int newTimeToCrash = testReachedCallgraph.indexOf(callgraphEdge);
						if(newTimeToCrash == -1) {
							newTimeToCrash = testReachedCallgraph.size();
						}
						if(curTimeToCrash == null || newTimeToCrash < curTimeToCrash) {
							minTimeToCrash.put(cut, newTimeToCrash);
							callgraphEdgeToCrash.put(cut, callgraphEdge.getX() + " -> " + callgraphEdge.getY());
						}
					}
				}
				for(int cut : minTimeToCrash.keySet()) {
					System.out.println("MIN TIME TO CRASH FOR CUT " + cut + ": " + minTimeToCrash.get(cut));
					System.out.println("CALLGRAPH EDGE TO CRASH FOR CUT " + cut + ": " + callgraphEdgeToCrash.get(cut));
				}

				if(numMethods >= reachedMethods.size()) {
					break;
				}
				numMethods += (int)(fractionMethodIncrement*numReachableMethods);
				/*** END COPIED FROM TESTS ANALYSIS ***//*
			}
		} catch(Exception e) {
			e.printStackTrace();
		}*/
	}
}
