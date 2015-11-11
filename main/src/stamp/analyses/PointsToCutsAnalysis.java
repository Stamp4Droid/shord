package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.Monitor;
import stamp.analyses.inferaliasmodel.InstrumentationDataWriter.MonitorWriter;
import stamp.analyses.inferaliasmodel.MonitorMapUtils;
import stamp.missingmodels.util.cflsolver.core.AbductiveInferenceProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphEdgeFilter;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.NotFilter;
import stamp.missingmodels.util.cflsolver.grammars.PointsToGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.FilterRelationManager.PointsToFilterRelationManager;
import stamp.missingmodels.util.cflsolver.relation.PointsToRelationManager;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPrecomputedPointsToRelationManager;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "pt-cut")
public class PointsToCutsAnalysis extends JavaAnalysis {
	public static <T> Filter<T> getSetFilter(final Set<T> set) {
		return new Filter<T>() { public boolean filter(T t) { return set.contains(t); }};
	}
	
	public static Filter<Edge> getEdgeFilter(final Filter<EdgeStruct> filter) {
		return new Filter<Edge>() { public boolean filter(Edge edge) { return filter.filter(edge.getStruct()); }};
	}
	
	public static <T> Filter<T> getTrueFilter() {
		return new Filter<T>() { public boolean filter(T t) { return true; }};
	}
	
	public static Set<EdgeStruct> getUncuttableInitialEdges(Graph graph, ContextFreeGrammarOpt grammar, final Filter<Edge> filter, final Filter<EdgeStruct> baseEdgeFilter, Filter<EdgeStruct> initialEdgeFilter) {
		// STEP 1: Compute transitive closure
		Graph graphBar = new ReachabilitySolver(graph.getVertices(), grammar, new AndFilter<Edge>(filter, getEdgeFilter(new NotFilter<EdgeStruct>(baseEdgeFilter)))).transform(graph.getEdgeStructs());
		
		// STEP 2: Get edges
		final Set<EdgeStruct> uncuttableInitialEdges = new HashSet<EdgeStruct>();
		for(EdgeStruct edge : graphBar.getEdgeStructs(initialEdgeFilter)) {
			//System.out.println("UNCUTTABLE EDGE: " + edge.toString(true));
			uncuttableInitialEdges.add(edge);
		}
		
		return uncuttableInitialEdges;
	}
	
	public static Filter<EdgeStruct> getNewInitialEdgeFilter(Graph graph, ContextFreeGrammarOpt grammar, Filter<Edge> filter, Filter<EdgeStruct> baseEdgeFilter, Filter<EdgeStruct> initialEdgeFilter) {
		return new AndFilter<EdgeStruct>(initialEdgeFilter, new NotFilter<EdgeStruct>(getSetFilter(getUncuttableInitialEdges(graph, grammar, filter, baseEdgeFilter, initialEdgeFilter))));
	}
	
	public static Set<EdgeStruct> getUncutInitialEdges(Graph graph, ContextFreeGrammarOpt grammar, Filter<Edge> filter, Filter<EdgeStruct> baseEdgeFilter, Filter<EdgeStruct> initialEdgeFilter, Set<EdgeStruct> cut) {
		// STEP 1: Compute transitive closure
		Filter<Edge> newFilter = new AndFilter<Edge>(filter, new NotFilter<Edge>(getEdgeFilter(getSetFilter(cut))));
		Graph graphBar = new ReachabilitySolver(graph.getVertices(), grammar, newFilter).transform(graph.getEdgeStructs());
		
		// STEP 2: Make sure all edges are cut
		Set<EdgeStruct> uncutInitialEdges = new HashSet<EdgeStruct>();
		for(EdgeStruct edge : graphBar.getEdgeStructs(initialEdgeFilter)) {
			uncutInitialEdges.add(edge);
		}
		
		return uncutInitialEdges;
	}
	
	public static void checkCut(Graph graph, ContextFreeGrammarOpt grammar, Filter<Edge> filter, Filter<EdgeStruct> baseEdgeFilter, Filter<EdgeStruct> initialEdgeFilter, Set<EdgeStruct> cut) {
		Set<EdgeStruct> uncutInitialEdges = getUncutInitialEdges(graph, grammar, filter, baseEdgeFilter, initialEdgeFilter, cut);
		if(!uncutInitialEdges.isEmpty()) {
			throw new RuntimeException("UNCUT INITIAL EDGE: " + uncutInitialEdges.iterator().next().toString(true));
		}
	}
	
	public static Set<EdgeStruct> getCompositionalCut(Graph graph1, Graph graph2, Filter<Edge> filter1, Filter<Edge> filter2, ContextFreeGrammarOpt grammar1, ContextFreeGrammarOpt grammar2, Filter<EdgeStruct> baseEdgeFilter1, Filter<EdgeStruct> baseEdgeFilter2, Filter<EdgeStruct> initialEdgeFilter) {
		// STEP 1: Get new base edge filter
		Filter<EdgeStruct> newBaseEdgeFilter2 = getNewInitialEdgeFilter(graph1, grammar1, filter1, baseEdgeFilter1, baseEdgeFilter2);
		Filter<EdgeStruct> newInitialEdgeFilter = getNewInitialEdgeFilter(graph2, grammar2, filter2, newBaseEdgeFilter2, initialEdgeFilter);
		
		// STEP 2: First cut
		MultivalueMap<EdgeStruct,Integer> first = new AbductiveInferenceProduction(grammar2).process(newBaseEdgeFilter2, newInitialEdgeFilter, graph2, filter2, 1);
		IOUtils.printAbductionResult(first, true, false);
		checkCut(graph2, grammar2, filter2, newBaseEdgeFilter2, newInitialEdgeFilter, first.keySet());
		
		// STEP 3: Second cut
		MultivalueMap<EdgeStruct,Integer> second = new AbductiveInferenceProduction(grammar1).process(baseEdgeFilter1, getSetFilter(first.keySet()), graph1, filter1, 1);
		IOUtils.printAbductionResult(second, true, false);
		checkCut(graph1, grammar1, filter1, baseEdgeFilter1, getSetFilter(first.keySet()), second.keySet());
		
		return second.keySet();
	}
	
	public static Set<EdgeStruct> getIterativeCut(Graph graph1, Graph graph2, Filter<Edge> filter1, Filter<Edge> filter2, ContextFreeGrammarOpt grammar1, ContextFreeGrammarOpt grammar2, Filter<EdgeStruct> baseEdgeFilter1, Filter<EdgeStruct> baseEdgeFilter2, Filter<EdgeStruct> initialEdgeFilter, Set<EdgeStruct> initUncuttableBaseEdges, Set<EdgeStruct> allUncuttableBaseEdges, int maxIters) {
		// Get uncuttable initial edges
		Filter<EdgeStruct> newBaseEdgeFilter2 = getNewInitialEdgeFilter(graph1, grammar1, filter1, new AndFilter<EdgeStruct>(baseEdgeFilter1, new NotFilter<EdgeStruct>(getSetFilter(allUncuttableBaseEdges))), baseEdgeFilter2);
		Filter<EdgeStruct> newInitialEdgeFilter = getNewInitialEdgeFilter(graph2, grammar2, filter2, newBaseEdgeFilter2, initialEdgeFilter);
		
		// Iteratively compute cuts
		System.out.println("NUM INIT UNCUTTABLE BASE EDGES: " + initUncuttableBaseEdges.size());
		System.out.println("NUM FINAL UNCUTTABLE BASE EDGES: " + allUncuttableBaseEdges.size());
		Set<EdgeStruct> curUncuttableBaseEdges = new HashSet<EdgeStruct>(initUncuttableBaseEdges);
		for(int i=0; i<maxIters; i++) {
			System.out.println("ITERATION: " + i);
			
			// STEP 1: Run abduction
			Set<EdgeStruct> cut = getCompositionalCut(graph1, graph2, filter1, filter2, grammar1, grammar2, new AndFilter<EdgeStruct>(baseEdgeFilter1, new NotFilter<EdgeStruct>(getSetFilter(curUncuttableBaseEdges))), baseEdgeFilter2, newInitialEdgeFilter);
			
			// STEP 2: Return if no cut
			if(cut.isEmpty()) {
				System.out.println("No valid cuts (or no source-sink edges)!");
				return cut;
			}
			
			// STEP 3: Update uncuttable base edges
			boolean changed = false;
			for(EdgeStruct edge : cut) {
				if(allUncuttableBaseEdges.contains(edge)) {
					System.out.println("ADDING EDGE: " + edge.toString());
					curUncuttableBaseEdges.add(edge);
					changed = true;
				}
			}
			
			// STEP 4: Return if no change
			if(!changed) {
				System.out.println("No false positive edges!");
				return cut;
			}
		}
		
		System.out.println("Insufficient iterations!");
		return new HashSet<EdgeStruct>();
	}
	
	private static Set<String> libraryVertices = null;
	private static Set<String> libraryVisibleVertices = null;
	private static Filter<EdgeStruct> getLibraryVertexFilter() {
		if(libraryVertices == null) {
			// STEP 1: Library vertices
			libraryVertices = new HashSet<String>();
			ProgramRel relFrameworkVar = (ProgramRel)ClassicProject.g().getTrgt("FrameworkVar");
			relFrameworkVar.load();
			for(int[] var : relFrameworkVar.getAryNIntTuples()) {
				libraryVertices.add("V" + var[0]);
			}
			relFrameworkVar.close();
			ProgramRel relFrameworkAlloc = (ProgramRel)ClassicProject.g().getTrgt("FrameworkObj");
			relFrameworkAlloc.load();
			for(int[] alloc : relFrameworkAlloc.getAryNIntTuples()) {
				libraryVertices.add("H" + alloc[0]);
			}
			
			// STEP 2: Visible variables
			libraryVisibleVertices = new HashSet<String>();
			ProgramRel relFrameworkParamVar = (ProgramRel)ClassicProject.g().getTrgt("FrameworkParamVar");
			relFrameworkParamVar.load();
			for(int[] var : relFrameworkParamVar.getAryNIntTuples()) {
				libraryVisibleVertices.add("V" + var[0]);
			}
			relFrameworkParamVar.close();
			ProgramRel relFrameworkRetVar = (ProgramRel)ClassicProject.g().getTrgt("FrameworkRetVar");
			relFrameworkRetVar.load();
			for(int[] var : relFrameworkRetVar.getAryNIntTuples()) {
				libraryVisibleVertices.add("V" + var[0]);
			}
			relFrameworkRetVar.close();
			ProgramRel relFrameworkRetObj = (ProgramRel)ClassicProject.g().getTrgt("FrameworkRetObj");
			relFrameworkRetObj.load();
			for(int[] alloc : relFrameworkRetObj.getAryNIntTuples()) {
				libraryVisibleVertices.add("H" + alloc[0]);
			}
			relFrameworkRetObj.close();
		}
		final Set<String> curLibraryVertices = libraryVertices;
		final Set<String> curLibraryVisibleVertices = libraryVisibleVertices;
		return new Filter<EdgeStruct>() {
			public boolean filter(EdgeStruct edge) {
				return (!curLibraryVertices.contains(edge.sourceName) || curLibraryVisibleVertices.contains(edge.sourceName))
						&& (!curLibraryVertices.contains(edge.sinkName) || curLibraryVisibleVertices.contains(edge.sinkName));
			}
		};
	}
	
	private static Set<EdgeStruct> uncuttablePointsToEdges = null;
	private static Set<EdgeStruct> getUncuttablePointsToEdges() {
		if(uncuttablePointsToEdges == null) {
			uncuttablePointsToEdges = new HashSet<EdgeStruct>();
			for(EdgeStruct ptEdge : IOUtils.readGraphEdgesFromFile("Flow", "graph")) {
				uncuttablePointsToEdges.add(ptEdge);
			}
			for(EdgeStruct fptEdge : IOUtils.readGraphEdgesFromFile("FlowField", "graph")) {
				uncuttablePointsToEdges.add(fptEdge);
			}
			for(EdgeStruct fptArrEdge : IOUtils.readGraphEdgesFromFile("FlowFieldArr", "graph")) {
				uncuttablePointsToEdges.add(fptArrEdge);
			}
			System.out.println("NUM UNCUTTABLE PT EDGES: " + uncuttablePointsToEdges.size());
		}
		return uncuttablePointsToEdges;
	}

	private static Filter<EdgeStruct> getBaseEdgeNameFilter1() {
		return new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("Flow"); }};
	}
	
	private static Filter<EdgeStruct> getBaseEdgeNameFilter2() {
		return new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("Flow") || edge.symbol.equals("FlowField") || edge.symbol.equals("FlowFieldArr"); }};
	}
	
	private static Filter<EdgeStruct> getInitialEdgeNameFilter() {
		return new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("Src2Sink"); }};
	}
	
	public static Set<EdgeStruct> getIterativePointsToCut(Set<EdgeStruct> uncuttablePointsToEdges, int maxIters) {
		// STEP 1: Configuration
		RelationReader reader = new ShordRelationReader();
		RelationManager relations1 = new PointsToRelationManager();
		RelationManager relations2 = new TaintPrecomputedPointsToRelationManager();
		ContextFreeGrammarOpt grammar1 = new PointsToGrammar().getOpt();
		ContextFreeGrammarOpt grammar2 = new TaintGrammar().getOpt();
		
		// STEP 2: Get graphs and filters
		Graph graph1 = Graph.getGraph(grammar1.getSymbols(), reader.readGraph(relations1, grammar1.getSymbols()));
		Graph graph2 = Graph.getGraph(grammar2.getSymbols(), reader.readGraph(relations2, grammar2.getSymbols()));
		Filter<Edge> filter1 = new GraphEdgeFilter(graph1.getVertices(), grammar1.getSymbols(), reader.readGraph(new PointsToFilterRelationManager(), grammar1.getSymbols()));
		Filter<Edge> filter2 = getTrueFilter();
		Filter<EdgeStruct> baseEdgeFilter1 = new AndFilter<EdgeStruct>(getBaseEdgeNameFilter1(), getLibraryVertexFilter());
		Filter<EdgeStruct> baseEdgeFilter2 = new AndFilter<EdgeStruct>(getBaseEdgeNameFilter2());
		Filter<EdgeStruct> initialEdgeFilter = getInitialEdgeNameFilter();
		
		// STEP 3: Perform cuts
		int newMaxIters = maxIters == 0 ? 1 : maxIters;
		Set<EdgeStruct> initUncuttableEdges = maxIters == 0 ? uncuttablePointsToEdges : new HashSet<EdgeStruct>();
		return getIterativeCut(graph1, graph2, filter1, filter2, grammar1, grammar2, baseEdgeFilter1, baseEdgeFilter2, initialEdgeFilter, initUncuttableEdges, uncuttablePointsToEdges, newMaxIters);
	}
	
	public static MultivalueMap<String,Monitor> getMonitors(Set<EdgeStruct> cut) {
		Set<String> cutVertices = new HashSet<String>();
		for(EdgeStruct edge : cut) {
			cutVertices.add(edge.sourceName);
			cutVertices.add(edge.sinkName);
		}
		return MonitorMapUtils.getMonitorMapForVertices(cutVertices);
	}
	
	public static void printMonitors(MultivalueMap<String,Monitor> monitors, boolean printToFile) {
		for(String vertex : monitors.keySet()) {
			System.out.println("VERTEX: " + vertex);
			for(Monitor monitor : monitors.get(vertex)) {
				System.out.println(monitor.getRecord(3192));
			}
		}
		if(printToFile) {
			try {
				File outDir = new File(System.getProperty("stamp.out.dir"), "inferaliasmodel");
				outDir.mkdirs();
				MonitorWriter monitorWriter = new MonitorWriter(new PrintWriter(new File(outDir, "instrinfo.txt")));
				PrintWriter eventWriter = new PrintWriter(new File(outDir, "event.txt"));
				for(String vertex : monitors.keySet()) {
					for(Monitor monitor : monitors.get(vertex)) {
						int id = monitorWriter.write(monitor);
						eventWriter.println(vertex + " " + id);
					}
				}
				monitorWriter.close();
				eventWriter.close();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static void runCut() {
		printMonitors(getMonitors(getIterativePointsToCut(getUncuttablePointsToEdges(), 0)), true);
		printMonitors(getMonitors(getIterativePointsToCut(getUncuttablePointsToEdges(), 10)), false);
	}
	
	private static void runDumpEdges() {
		// STEP 1: Setup
		RelationReader reader = new ShordRelationReader();
		RelationManager relations = new TaintPrecomputedPointsToRelationManager();
		ContextFreeGrammarOpt grammar = new TaintGrammar().getOpt();
		
		// STEP 2: Get graph and closure
		Graph g = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> f = getTrueFilter();
		Graph gbar = new ReachabilitySolver(g.getVertices(), grammar, f).transform(g.getEdgeStructs());
		
		// STEP 3: Print points-to and source-sink edges
		IOUtils.printGraphEdgesToFile(gbar, "Flow", false, "graph");
		IOUtils.printGraphEdgesToFile(gbar, "FlowField", false, "graph");
		IOUtils.printGraphEdgesToFile(gbar, "FlowFieldArr", false, "graph");
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
