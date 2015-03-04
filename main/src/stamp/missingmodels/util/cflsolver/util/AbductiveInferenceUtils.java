package stamp.missingmodels.util.cflsolver.util;

import java.util.Map;

import shord.project.ClassicProject;
import stamp.analyses.DomL;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.EdgeTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.Filter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;

public class AbductiveInferenceUtils {
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
	
	private static Map<EdgeStruct,Boolean> runAbductiveInference(AbductiveInferenceHelper h, ContextFreeGrammarOpt c, Graph gbart, Graph gcur, boolean shord) {
		Iterable<Edge> baseEdges = h.getBaseEdges(gbart, gcur);
		Iterable<Edge> initialCutEdges = h.getInitialCutEdges(gbart);
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
		
		return new AbductiveInference(c, gbart, baseEdges, initialCutEdges).solve();
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
	
	public static MultivalueMap<EdgeStruct,Integer> runInference(AbductiveInferenceHelper h, ContextFreeGrammarOpt c, Graph g, boolean shord, int numCuts) {
		Graph gcur = g;
		MultivalueMap<EdgeStruct,Integer> allResults = new MultivalueMap<EdgeStruct,Integer>();
		for(int i=0; i<numCuts; i++) {
			// STEP 1: Run reachability solver
			Graph gbar = gcur.transform(new ReachabilitySolver(gcur.getVertices(), c));
			
			// STEP 2: Run the abductive inference algorithm
			final Map<EdgeStruct,Boolean> result = runAbductiveInference(h, c, gbar, gcur, shord);
			for(EdgeStruct edge : result.keySet()) {
				if(result.get(edge)) {
					allResults.add(edge, i);
				}
			}
			
			// STEP 3: Transform graph to remove cut edges
			gcur = gcur.transform(new EdgeTransformer(gcur.getVertices(), gcur.getSymbols()) {
				public void process(GraphBuilder gb, EdgeStruct edge) {
					gb.addOrUpdateEdge(edge.sourceName, edge.sinkName, edge.symbol, edge.field, result.containsKey(edge) && result.get(edge) ? (short)0 : edge.weight);
				}});
		}		
		return allResults;
	}
	
	public static MultivalueMap<EdgeStruct,Integer> runInference(ContextFreeGrammarOpt c, Graph g, boolean shord, int numCuts) {
		return runInference(new DefaultAbductiveInferenceHelper(), c, g, shord, numCuts);
	}
}
