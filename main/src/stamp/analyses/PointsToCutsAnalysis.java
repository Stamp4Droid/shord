package stamp.analyses;

import java.util.HashSet;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.analyses.inferaliasmodel.PointsToCutMonitors;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.NotFilter;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.LimLabelShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPrecomputedPointsToRelationManager;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "pt-cut")
public class PointsToCutsAnalysis extends JavaAnalysis {
	public static Filter<EdgeStruct> getSetEdgeStructFilter(final Set<EdgeStruct> filter) {
		return new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return filter.contains(edge);
			}};
	}
	
	public static Filter<Edge> getEdgeFilter(final Filter<EdgeStruct> filter) {
		return new Filter<Edge>() {
			@Override
			public boolean filter(Edge edge) {
				return filter.filter(edge.getStruct());
			}};
	}
	
	private static Filter<EdgeStruct> getBaseEdgeFilter() {
		return new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return edge.symbol.equals("Flow") || edge.symbol.equals("FlowField") || edge.symbol.equals("FlowFieldArr");
			}};
	}
	
	private static Filter<EdgeStruct> getInitialEdgeFilter() {
		return new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return edge.symbol.equals("Src2Sink");
			}};
	}
	
	private static MultivalueMap<EdgeStruct,Integer> runCutIteration(final Filter<EdgeStruct> baseEdgeFilter, Filter<EdgeStruct> initialEdgeFilter) {
		// STEP 1: Setup
		RelationReader reader = new LimLabelShordRelationReader();
		RelationManager relations = new TaintPrecomputedPointsToRelationManager();
		ContextFreeGrammarOpt grammar = new TaintGrammar().getOpt();
		
		// STEP 2: Get graph
		Graph g = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		//Filter<Edge> f = new GraphEdgeFilter(g.getVertices(), grammar.getSymbols(), reader.readGraph(new PointsToFilterRelationManager(), grammar.getSymbols()));
		Filter<Edge> f = new Filter<Edge>() { public boolean filter(Edge edge) { return true; }};
		
		// statistics
		Graph gbar = new ReachabilitySolver(g.getVertices(), grammar, f).transform(g.getEdgeStructs());
		IOUtils.printGraphStatistics(gbar);
		//IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		
		// STEP 3: Abductive inference
		return new AbductiveInference(grammar).process(baseEdgeFilter, initialEdgeFilter, g, f, 2);
	}
	
	private static void runCut() {
		// STEP 0: Parameters
		int maxIters = 2;
		
		// STEP 1: Get ground truth
		Set<EdgeStruct> ptEdges = new HashSet<EdgeStruct>();
		for(EdgeStruct ptEdge : IOUtils.readGraphEdgesFromFile("Flow", "graph")) {
			ptEdges.add(ptEdge);
		}
		for(EdgeStruct fptEdge : IOUtils.readGraphEdgesFromFile("FlowField", "graph")) {
			ptEdges.add(fptEdge);
		}
		for(EdgeStruct fptArrEdge : IOUtils.readGraphEdgesFromFile("FlowFieldArr", "graph")) {
			ptEdges.add(fptArrEdge);
		}
		
		// STEP 2: Get the base edge filter data
		Filter<EdgeStruct> seedBaseEdgeFilter = getBaseEdgeFilter();
		Set<EdgeStruct> baseEdges = new HashSet<EdgeStruct>();
		
		// STEP 3: Get initial edge filter
		Filter<EdgeStruct> seedInitialEdgeFilter = getInitialEdgeFilter();
		Set<EdgeStruct> srcSinkEdges = new HashSet<EdgeStruct>();
		for(EdgeStruct srcSinkEdge : IOUtils.readGraphEdgesFromFile("Src2Sink", "graph")) {
			//System.out.println("Ignoring source-sink edge: " + srcSinkEdge.toString());
			srcSinkEdges.add(srcSinkEdge);
		}
		Filter<EdgeStruct> initialEdgeFilter = new AndFilter<EdgeStruct>(seedInitialEdgeFilter, new NotFilter<EdgeStruct>(getSetEdgeStructFilter(srcSinkEdges)));
		
		// STEP 4: Iteratively compute cuts
		for(int i=-1; i<maxIters; i++) {
			System.out.println("Iteration: " + i);
			
			// STEP 4a: Construct base edge filter
			Filter<EdgeStruct> baseEdgeFilter = new AndFilter<EdgeStruct>(seedBaseEdgeFilter, new NotFilter<EdgeStruct>(PointsToCutMonitors.getUncuttablePointsToEdgeFilter()), new NotFilter<EdgeStruct>(getSetEdgeStructFilter(i == -1 ? ptEdges : baseEdges)));
			
			// STEP 4b: Run abduction
			MultivalueMap<EdgeStruct,Integer> results = runCutIteration(baseEdgeFilter, initialEdgeFilter);
			IOUtils.printAbductionResult(results, true, false);
			
			// STEP 4c: Update the base edge filter (and terminate if no change)
			if(results.isEmpty()) {
				System.out.println("No valid cuts (or no source-sink edges)!");
				break;
			}
			boolean changed = false;
			for(EdgeStruct ptEdge : ptEdges) {
				if(results.containsKey(ptEdge)) {
					System.out.println("Adding edge: " + ptEdge.toString());
					baseEdges.add(ptEdge);
					changed = true;
				}
			}
			if(changed && i == -1) {
				throw new RuntimeException("This shouldn't happen -- we excluded all valid pt edges from cut!");
			}
			if(!changed && i != -1) {
				System.out.println("No false positive edges!");
				break;
			}
			
			// STEP 4d: Get monitors
			PointsToCutMonitors.printMonitors(results.keySet());
		}
	}
	
	private static void runDumpEdges() {
		// STEP 1: Setup
		RelationReader reader = new LimLabelShordRelationReader();
		RelationManager relations = new TaintPrecomputedPointsToRelationManager();
		ContextFreeGrammarOpt grammar = new TaintGrammar().getOpt();
		
		// STEP 2: Get graph and closure
		Graph g = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> f = new Filter<Edge>() { public boolean filter(Edge edge) { return true; }};
		Graph gbar = new ReachabilitySolver(g.getVertices(), grammar, f).transform(g.getEdgeStructs());
		
		// STEP 3: Print points-to and source-sink edges
		IOUtils.printGraphEdgesToFile(gbar, "Flow", false, "graph");
		IOUtils.printGraphEdgesToFile(gbar, "FlowField", false, "graph");
		IOUtils.printGraphEdgesToFile(gbar, "FlowFieldArr", false, "graph");
		IOUtils.printGraphEdgesToFile(gbar, "Src2Sink", false, "graph");
	}
	
	@Override
	public void run() {
		if(!IOUtils.relationFileExists("Flow", "graph")) {
			runDumpEdges();
		} else {
			runCut();
		}
	}
}
