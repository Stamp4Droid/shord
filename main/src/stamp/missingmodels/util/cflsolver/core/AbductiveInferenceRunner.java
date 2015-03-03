package stamp.missingmodels.util.cflsolver.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;
import shord.project.ClassicProject;
import stamp.analyses.DomL;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph.EdgeTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.Filter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;

public class AbductiveInferenceRunner {
	
	public interface AbductiveInferenceHelper {
		public Iterable<Edge> getBaseEdges(Graph gbar, Graph gcur);
		public Iterable<Edge> getInitialCutEdges(Graph g);
	}
	
	public static class DefaultAbductiveInferenceHelper implements AbductiveInferenceHelper {
		@Override
		public Iterable<Edge> getBaseEdges(Graph gbart, final Graph gcur) {
			return gbart.getEdges(new Filter<Edge>() {
				@Override
				public boolean filter(Edge edge) {
					return edge.symbol.symbol.equals("param") || edge.symbol.symbol.equals("paramPrim");
					//return edge.symbol.symbol.equals("callgraph");
				}
			});
		}
		
		@Override
		public Iterable<Edge> getInitialCutEdges(Graph gbart) {
			return gbart.getEdges(new Filter<Edge>() {
				//private boolean added = false;
				@Override
				public boolean filter(Edge edge) {
					//boolean result = (!this.added) && edge.getSymbol().equals("Src2Sink");
					//if(result) {
					//	this.added = true;
					//}
					//return result;
					return edge.symbol.symbol.equals("Src2Sink");
				}
			});
		}
	}
	
	private static Graph computeTransitiveClosure(Graph g, boolean shord) {
		long time = System.currentTimeMillis();
		System.out.println("Computing transitive closure");
		
		Graph gbar = g.transform(new ReachabilitySolver());
		
		System.out.println("Done in " + (System.currentTimeMillis() - time) + "ms");
		//System.out.println("Num edges: " + gbar.getEdges().size());
		
		return gbar;
	}
	
	private static Map<EdgeStruct,Boolean> runAbductiveInference(AbductiveInferenceHelper h, Graph gbart, Graph gcur, boolean shord) throws LpSolveException {
		Iterable<Edge> baseEdges = h.getBaseEdges(gbart, gcur);
		//System.out.println("Num base edges: " + baseEdges.size());
		
		Iterable<Edge> initialCutEdges = h.getInitialCutEdges(gbart);
		//System.out.println("Num initial edges: " + initialCutEdges.size());

		// print initial cut edges
		DomL dom = shord ? (DomL)ClassicProject.g().getTrgt("L") : null;
		System.out.println("Printing Src2Sink edges...");
		/*
		for(Edge edge : initialCutEdges) {
			System.out.println("Cutting Src2Sink edge: " + edge.toString());
			System.out.println("Edge weight: " + edge.weight);
			if(dom != null) {
				String source = dom.get(Integer.parseInt(edge.source.name.substring(1)));
				String sink = dom.get(Integer.parseInt(edge.sink.name.substring(1)));
				System.out.println("Edge represents source-sink flow: " + source + " -> " + sink);
			}
		}
		*/
		
		return new AbductiveInference(gbart, baseEdges, initialCutEdges).solve();
	}
	
	private static void recomputeTransitiveClosure(Graph gbar, TypeFilter t, Set<Edge> baseEdges, final Map<EdgeStruct,Boolean> result) {
		System.out.println("Printing remaining src-sink edges:");
		final Set<EdgeStruct> baseEdgeStructs = new HashSet<EdgeStruct>();
		for(Edge edge : baseEdges) {
			baseEdgeStructs.add(edge.getStruct());
		}
		EdgeTransformer gtNew = new EdgeTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edge) {
				EdgeStruct tempEdge = new EdgeStruct(edge.sourceName, edge.sinkName, edge.symbol, edge.field, edge.weight);
				if(result.get(tempEdge) != null && result.get(tempEdge)) {
					return;
				}
				if(!baseEdgeStructs.contains(tempEdge) && edge.weight > 0) {
					return;
				}
				gb.addEdge(edge);
			}
		};
		Graph gnew = gbar.transform(gtNew);
		Graph gnewBar = gnew.transform(new ReachabilitySolver());
		for(EdgeStruct edge : gnewBar.getEdgeStructs(new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return edge.symbol.equals("Src2Sink");
			}})) {
			System.out.println("src2sink edge: " + edge);
			System.out.println("weight: " + edge.weight);
		}
		System.out.println("Done!");
	}
	
	private static Graph removeResultingEdges(Graph g, final Map<EdgeStruct,Boolean> result) {
		EdgeTransformer gt = new EdgeTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct) {
				short newWeight = edgeStruct.weight;
				//System.out.println(edgeStruct);
				EdgeStruct newEdgeStruct = new EdgeStruct(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, newWeight);
				if(result.get(newEdgeStruct) != null && result.get(newEdgeStruct)) {
					//System.out.println("Removing edge: " + newEdgeStruct);
					newWeight = 0;
				}
				gb.addEdge(edgeStruct);
			}
		};
		return g.transform(gt);
	}
	
	private static void printPaths(Graph g, Iterable<Edge> edges, boolean shord) {
		//System.out.println("Initial cut edges before context strip: " + edges.size());
		DomL dom = shord ? (DomL)ClassicProject.g().getTrgt("L") : null;
		for(Edge edge : edges) {	
			System.out.println("Cutting Src2Sink edge: " + edge.toString());
			System.out.println("Edge weight: " + edge.weight);
			if(dom != null) {
				String source = dom.get(Integer.parseInt(edge.source.name.substring(1)));
				String sink = dom.get(Integer.parseInt(edge.sink.name.substring(1)));
				System.out.println("Edge represents source-sink flow: " + source + " -> " + sink);
			}
			/*
			System.out.println("STARTING EDGE PATH");
			for(Pair<Edge,Boolean> pathEdgePair : edge.getPath()) {
				System.out.println("weight " + pathEdgePair.getX().getInfo().weight + ", " + "isForward " + pathEdgePair.getY() + ": " + pathEdgePair.getX().toString(shord));
			}
			System.out.println("ENDING EDGE PATH");
			*/
		}		
	}
	
	public static MultivalueMap<EdgeStruct,Integer> runInference(AbductiveInferenceHelper h, Graph g, boolean shord, int numCuts) throws LpSolveException {
		Graph gcur = g;
		MultivalueMap<EdgeStruct,Integer> allResults = new MultivalueMap<EdgeStruct,Integer>();
		for(int i=0; i<numCuts; i++) {
			// STEP 1: Run reachability solver
			Graph gbar = computeTransitiveClosure(gcur, shord);
			//IOUtils.printGraphStatistics(gbar);
			printPaths(gbar, h.getInitialCutEdges(gbar), shord);
			
			// STEP 2: Strip contexts
			//Graph2 gbart = stripContexts(gbar);

			// STEP 3: Run the abductive inference algorithm
			Map<EdgeStruct,Boolean> result = runAbductiveInference(h, gbar, gcur, shord);
			
			// STEP 4: Transform graph to remove cut edges
			gcur = removeResultingEdges(gcur, result);
			
			// STEP 5: Rerun reachability solver
			// TODO: should rerun with only edges removed from g, not gcur
			//recomputeTransitiveClosure(gbar, t, getBaseEdges(gbart), result);
			
			// STEP 6: Add cut to list
			for(EdgeStruct edge : result.keySet()) {
				if(result.get(edge)) {
					//System.out.println("Cut " + i + ": " + edge);
					allResults.add(edge, i);
				}
			}
		}		
		return allResults;
	}
	
	public static MultivalueMap<EdgeStruct,Integer> runInference(Graph g, boolean shord, int numCuts) throws LpSolveException {
		return runInference(new DefaultAbductiveInferenceHelper(), g, shord, numCuts);
	}
}
