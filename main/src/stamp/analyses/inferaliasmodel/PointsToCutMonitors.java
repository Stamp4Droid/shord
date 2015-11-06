package stamp.analyses.inferaliasmodel;

import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.AbductiveInferenceProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.grammars.PointsToGrammar;
import stamp.missingmodels.util.cflsolver.reader.LimLabelShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.PointsToRelationManager;

public class PointsToCutMonitors {
	private static Set<String> libraryVertices = null;
	private static Set<String> getLibraryVertices() {
		if(libraryVertices == null) {
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
			relFrameworkAlloc.close();
		}
		return libraryVertices;
	}
	
	private static Filter<EdgeStruct> getBaseEdgeFilter() {
		final Set<String> libraryVertices = getLibraryVertices();
		return new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("Flow") && !libraryVertices.contains(edge.sourceName) && !libraryVertices.contains(edge.sinkName); }};
	}
	
	private static MultivalueMap<EdgeStruct,Integer> getFrameworkPointsToCuts(final Set<EdgeStruct> ptEdges) {
		// STEP 1: Configuration
		RelationReader reader = new LimLabelShordRelationReader();
		RelationManager relations = new PointsToRelationManager();
		ContextFreeGrammarOpt grammar = new PointsToGrammar().getOpt();
		
		// STEP 2: Set up abduction inference
		Filter<EdgeStruct> baseEdgeFilter = getBaseEdgeFilter();
		Filter<EdgeStruct> initialEdgeFilter = new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return ptEdges.contains(edge); }};
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> filter = new Filter<Edge>() { public boolean filter(Edge edge) { return true; }};
		
		// STEP 3: Perform abduction
		return new AbductiveInferenceProduction(grammar).process(baseEdgeFilter, initialEdgeFilter, graph, filter, 1);
	}
	
	private static Set<EdgeStruct> getUncuttablePointsToEdges() {
		// STEP 1: Configuration
		RelationReader reader = new LimLabelShordRelationReader();
		RelationManager relations = new PointsToRelationManager();
		ContextFreeGrammarOpt grammar = new PointsToGrammar().getOpt();
		
		// STEP 2: Set up graph
		final Filter<EdgeStruct> baseEdgeFilter = getBaseEdgeFilter();
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> filter = new Filter<Edge>() { public boolean filter(Edge edge) { return !baseEdgeFilter.filter(edge.getStruct()); }};
		
		// STEP 3: Compute transitive closure
		Graph graphBar = new ReachabilitySolver(graph.getVertices(), grammar, filter).transform(graph.getEdgeStructs());
		Set<EdgeStruct> ptEdges = new HashSet<EdgeStruct>();
		for(EdgeStruct edge : graphBar.getEdgeStructs(new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("Flow"); }})) {
			ptEdges.add(edge);
		}
		return ptEdges;
	}
	
	public static Filter<EdgeStruct> getUncuttablePointsToEdgeFilter() {
		final Set<EdgeStruct> edges = getUncuttablePointsToEdges();
		return new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return !edge.symbol.equals("Flow") || edges.contains(edge); }};
	}
	
	public static void printMonitors(Iterable<EdgeStruct> ptEdges) {
		// STEP 1: Get cut
		final Set<EdgeStruct> ptEdgeSet = new HashSet<EdgeStruct>();
		for(EdgeStruct ptEdge : ptEdges) {
			System.out.println("INITIAL EDGE: " + ptEdge.toString(true));
			ptEdgeSet.add(ptEdge);
		}
		final MultivalueMap<EdgeStruct,Integer> cutPtEdges = getFrameworkPointsToCuts(ptEdgeSet);
		System.out.println("CUT SIZE: " + cutPtEdges.keySet().size());
		for(EdgeStruct cutPtEdge : cutPtEdges.keySet()) {
			System.out.println("CUT EDGE: " + cutPtEdge.toString(true));
		}
		
		// STEP 2: Configuration
		RelationReader reader = new LimLabelShordRelationReader();
		RelationManager relations = new PointsToRelationManager();
		ContextFreeGrammarOpt grammar = new PointsToGrammar().getOpt();
		
		// STEP 3: Compute transitive closure
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> filter = new Filter<Edge>() { public boolean filter(Edge edge) { return !cutPtEdges.keySet().contains(edge.getStruct()); }};
		Graph graphBar = new ReachabilitySolver(graph.getVertices(), grammar, filter).transform(graph.getEdgeStructs());
		
		// STEP 4: Get un-cut edges
		for(EdgeStruct edge : graphBar.getEdgeStructs(new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return ptEdgeSet.contains(edge); }})) {
			System.out.println("UNCUT EDGE: " + edge.toString(true));
		}
	}
}
