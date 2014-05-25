package stamp.analyses;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lpsolve.LpSolveException;
import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.Util.Counter;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeFilter;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.FileRelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar taintGrammar = new TaintGrammar();

	public static void printResult(MultivalueMap<EdgeStruct,Integer> result, boolean shord) {
		Counter<Integer> totalCut = new Counter<Integer>();
		for(EdgeStruct edgeStruct : result.keySet()) {
			if(result.get(edgeStruct).size() > 1) {
				System.out.println("ERROR: Multiple cuts for edge " + edgeStruct);
			}
			for(int cut : result.get(edgeStruct)) {
				System.out.println("in cut " + cut + ": " + edgeStruct);
				if(shord) {
					System.out.println("caller: " + ConversionUtils.getMethodSig(edgeStruct.sourceName));
					System.out.println("callee: " + ConversionUtils.getMethodSig(edgeStruct.sinkName));
				}
				totalCut.increment(cut);
			}
		}
		for(int i : totalCut.keySet()) {
			System.out.println("total cut " + i + ": " + totalCut.getCount(i));
		}
	}
	
	public static void printGraphEdges(Graph g, final String symbol) {
		Set<String> edges = new HashSet<String>();
		for(Edge edge : g.getEdges(new EdgeFilter() {
			@Override
			public boolean filter(Edge edge) {
				return edge.getSymbol().equals(symbol); 
			}})) {
			edges.add(edge.sink.name.substring(1) + " " + edge.source.name.substring(1));
		}
		List<String> edgeList = new ArrayList<String>(edges);
		Collections.sort(edgeList);
		for(String edge : edgeList) {
			System.out.println(edge);
		}
	}
	
	public static void printGraphStatistcs(Graph g) {
		for(int symbolInt=0; symbolInt<g.getContextFreeGrammar().getNumLabels(); symbolInt++) {
			final String symbol = g.getContextFreeGrammar().getSymbol(symbolInt);
			if(!symbol.equals(symbol)) continue;
			Set<String> edges = new HashSet<String>();
			for(Edge edge : g.getEdges(new EdgeFilter() {
				@Override
				public boolean filter(Edge edge) {
					return edge.getSymbol().equals(symbol); 
				}})) {
				edges.add(edge.sink.name.substring(1) + " " + edge.source.name.substring(1));
			}
			System.out.println(symbol + ": " + edges.size());
		}
		System.out.println("total edges: " + g.getEdges().size());
	}
	
	public static Graph getTestGraph() {
		GraphBuilder gb = new GraphBuilder(taintGrammar);
		gb.addEdge("o1", "x", "alloc");
		gb.addEdge("o2", "z", "alloc");
		gb.addEdge("x", "y", "param", new EdgeInfo(1));
		gb.addEdge("z", "y", "store");
		gb.addEdge("x", "w", "param", new EdgeInfo(1));
		gb.addEdge("w", "v", "load");
		return gb.toGraph();
	}
	
	@Override
	public void run() {
		try {
			RelationReader relationReader = new ShordRelationReader();
			String[] tokens = System.getProperty("stamp.out.dir").split("_");
			RelationManager relations = new DynamicCallgraphRelationManager(TraceReader.getCallgraphList("../../profiler/traceouts/", tokens[tokens.length-1]));
			printResult(AbductiveInferenceRunner.runInference(relationReader.readGraph(relations, taintGrammar), relationReader.readTypeFilter(taintGrammar), true, 4), true);
		} catch(LpSolveException e) {
			e.printStackTrace();
		}
	}
	
 	public static void main(String[] args) throws LpSolveException {
		String directoryName = "/home/obastani/Documents/projects/research/stamp/shord_clone/stamp_output/_home_obastani_Documents_projects_research_stamp_stamptest_DarpaApps_1C_tomdroid/cfl";		
		RelationReader relationReader = new FileRelationReader(new File(directoryName));
		printResult(AbductiveInferenceRunner.runInference(relationReader.readGraph(new DynamicCallgraphRelationManager(), taintGrammar), relationReader.readTypeFilter(taintGrammar), false, 4), false);
	}
}
