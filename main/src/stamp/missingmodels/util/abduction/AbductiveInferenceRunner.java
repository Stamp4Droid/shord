package stamp.missingmodels.util.abduction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;
import shord.project.ClassicProject;
import stamp.analyses.DomL;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeFilter;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.graph.GraphTransformer;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.cflsolver.util.PrintingUtils;

public class AbductiveInferenceRunner {
	
	private static Graph computeTransitiveClosure(Graph g, TypeFilter t, boolean shord) {
		long time = System.currentTimeMillis();
		System.out.println("Computing transitive closure");
		
		Graph gbar = new ReachabilitySolver(g, t).getResult();
		
		System.out.println("Done in " + (System.currentTimeMillis() - time) + "ms");
		System.out.println("Num edges: " + gbar.getEdges().size());
		
		return gbar;
	}
	
	private static Graph stripContexts(Graph gbar) {
		GraphTransformer gtStripContext = new GraphTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct, int weight) {
				EdgeInfo curInfo = gb.toGraph().getInfo(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, Context.DEFAULT_CONTEXT);
				if(curInfo == null || weight < curInfo.weight) {   
					gb.addEdge(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, Context.DEFAULT_CONTEXT, new EdgeInfo(weight));
				}
			}
		};
		return gtStripContext.transform(gbar);
	}
	
	private static Set<Edge> getBaseEdges(Graph gbart) {
		return gbart.getEdges(new EdgeFilter() {
			@Override
			public boolean filter(Edge edge) {
				return edge.getSymbol().equals("callgraph");
			}
		});
	}
	
	private static Set<Edge> getInitialCutEdges(Graph gbart) {
		return gbart.getEdges(new EdgeFilter() {
			private boolean added = false;
			@Override
			public boolean filter(Edge edge) {
				boolean result = (!this.added) && edge.getSymbol().equals("Src2Sink");
				if(result) {
					//this.added = true;
				}
				return result;
			}
		});
	}
	
	private static Map<EdgeStruct,Boolean> runAbductiveInference(Graph gbart, boolean shord) throws LpSolveException {
		Set<Edge> baseEdges = getBaseEdges(gbart);
		System.out.println("Num base edges: " + baseEdges.size());
		
		Set<Edge> initialCutEdges = getInitialCutEdges(gbart);
		System.out.println("Num initial edges: " + initialCutEdges.size());

		// print initial cut edges
		DomL dom = shord ? (DomL)ClassicProject.g().getTrgt("L") : null;
		for(Edge edge : initialCutEdges) {	
			System.out.println("Cutting Src2Sink edge: " + edge.toString());
			System.out.println("Edge weight: " + edge.getInfo().weight);
			if(dom != null) {
				String source = dom.get(Integer.parseInt(edge.source.name.substring(1)));
				String sink = dom.get(Integer.parseInt(edge.sink.name.substring(1)));
				System.out.println("Edge represents source-sink flow: " + source + " -> " + sink);
			}
		}
		
		return new AbductiveInference(gbart, baseEdges, initialCutEdges).solve();
	}
	
	private static void recomputeTransitiveClosure(Graph gbar, TypeFilter t, Set<Edge> baseEdges, final Map<EdgeStruct,Boolean> result) {
		System.out.println("Printing remaining src-sink edges:");
		final Set<EdgeStruct> baseEdgeStructs = new HashSet<EdgeStruct>();
		for(Edge edge : baseEdges) {
			baseEdgeStructs.add(edge.getStruct());
		}
		GraphTransformer gtNew = new GraphTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct, int weight) {
				EdgeStruct tempStruct = new EdgeStruct(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, Context.DEFAULT_CONTEXT);
				if(result.get(tempStruct) != null && result.get(tempStruct)) {
					return;
				}
				if(!baseEdgeStructs.contains(tempStruct) && weight > 0) {
					return;
				}
				gb.addEdge(edgeStruct);
			}
		};
		Graph gnew = gtNew.transform(gbar);
		Graph gnewBar = new ReachabilitySolver(gnew, t).getResult();
		for(Edge edge : gnewBar.getEdges(new EdgeFilter() {
			@Override
			public boolean filter(Edge edge) {
				return edge.getSymbol().equals("Src2Sink");
			}
		})) {
			System.out.println("src2sink edge: " + edge);
			System.out.println("weight: " + edge.getInfo().weight);
		}
		System.out.println("Done!");
	}
	
	private static Graph removeResultingEdges(Graph g, final Map<EdgeStruct,Boolean> result) {
		GraphTransformer gt = new GraphTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct, int weight) {
				int newWeight = weight;
				//System.out.println(edgeStruct);
				EdgeStruct newEdgeStruct = new EdgeStruct(edgeStruct.sourceName, edgeStruct.sinkName, edgeStruct.symbol, edgeStruct.field, Context.DEFAULT_CONTEXT);
				if(result.get(newEdgeStruct) != null && result.get(newEdgeStruct)) {
					//System.out.println("Removing edge: " + newEdgeStruct);
					newWeight = 0;
				}
				gb.addEdge(edgeStruct, newWeight);
			}
		};
		return gt.transform(g);
	}
	
	private static void printPaths(Graph g, Set<Edge> edges, boolean shord) {
		System.out.println("Initial cut edges before context strip: " + edges.size());
		DomL dom = shord ? (DomL)ClassicProject.g().getTrgt("L") : null;
		for(Edge edge : edges) {	
			System.out.println("Cutting Src2Sink edge: " + edge.toString());
			System.out.println("Edge weight: " + edge.getInfo().weight);
			if(dom != null) {
				String source = dom.get(Integer.parseInt(edge.source.name.substring(1)));
				String sink = dom.get(Integer.parseInt(edge.sink.name.substring(1)));
				System.out.println("Edge represents source-sink flow: " + source + " -> " + sink);
			}
			System.out.println("STARTING EDGE PATH");
			for(Edge pathEdge : edge.getPath()) {
				System.out.println("weight " + pathEdge.getInfo().weight + ": " + pathEdge.toString(shord));
			}
			System.out.println("ENDING EDGE PATH");
		}		
	}
	
	public static MultivalueMap<EdgeStruct,Integer> runInference(Graph g, TypeFilter t, boolean shord, int numCuts) throws LpSolveException {
		Graph gcur = g;
		MultivalueMap<EdgeStruct,Integer> allResults = new MultivalueMap<EdgeStruct,Integer>();
		for(int i=0; i<numCuts; i++) {
			// STEP 1: Run reachability solver
			Graph gbar = computeTransitiveClosure(gcur, t, shord);
			printPaths(gbar, getInitialCutEdges(gbar), shord);
			
			// STEP 2: Strip contexts
			Graph gbart = stripContexts(gbar);

			// STEP 3: Run the abductive inference algorithm
			Map<EdgeStruct,Boolean> result = runAbductiveInference(gbart, shord);
			
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
}
