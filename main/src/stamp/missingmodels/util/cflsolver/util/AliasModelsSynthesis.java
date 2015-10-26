package stamp.missingmodels.util.cflsolver.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.grammars.PointsToGrammar;

public class AliasModelsSynthesis {
	private static final Set<String> stubSymbols = new HashSet<String>();
	static {
		stubSymbols.add("alloc");
		stubSymbols.add("assign");
		stubSymbols.add("store");
		stubSymbols.add("load");
	}
	
	public static Set<EdgeStruct> getStubEdges(String vertex) {
		Set<EdgeStruct> edges = new HashSet<EdgeStruct>();
		for(String symbol : stubSymbols) {
			edges.add(new EdgeStruct(vertex, vertex, symbol, -1, (short)1));
		}
		return edges;
	}
	
	public static List<Pair<EdgeStruct,Boolean>> synthesize(List<Pair<EdgeStruct,Boolean>> path) {
		ContextFreeGrammarOpt grammar = new PointsToGrammar().getOpt();
		// STEP 1: Build graph
		List<EdgeStruct> edges = new ArrayList<EdgeStruct>();
		boolean prevStub = false;
		for(int i=0; i<path.size(); i++) {
			// STEP 1a: Get edge data
			EdgeStruct prevEdge = path.get(i).getX();
			String symbol = prevEdge.symbol;
			short weight = (short)0;
			// STEP 1b: Add worst-case sub-graph if Bassign or assignE
			if(symbol.equals("Bassign") || symbol.equals("assignE")) {
				if(!prevStub) {
					String stub = path.get(i).getY() ? prevEdge.sinkName : prevEdge.sourceName;
					edges.addAll(getStubEdges(stub));
				}
				prevStub = !prevStub;
				symbol = symbol.equals("Bassign") ? "param" : "return";
				weight = (short)1;
			}
			// STEP 1c: Build edge
			edges.add(new EdgeStruct(prevEdge.sourceName, prevEdge.sinkName, symbol, -1, weight));
		}
		Graph graph = Graph.getGraph(grammar.getSymbols(), edges);
		
		// STEP 2: Solve shortest path
		Graph graphBar = graph.transform(new ReachabilitySolver(graph.getVertices(), grammar));
		
		// STEP 3: Get the source-sink path
		String source = path.get(0).getY() ? path.get(0).getX().sourceName : path.get(0).getX().sinkName;
		String sink = path.get(path.size()-1).getY() ? path.get(path.size()-1).getX().sinkName : path.get(path.size()-1).getX().sourceName;
		int symbolId = grammar.getSymbols().get("Flow").id;
		
		Edge flowEdge = null;
		for(Edge edge : graphBar.getVertex(source).getOutgoingEdges(symbolId)) {
			if(edge.sink.name.equals(sink)) {
				flowEdge = edge;
				break;
			}
		}
		if(flowEdge == null) { System.out.println("Source-sink edge not found!"); return new ArrayList<Pair<EdgeStruct,Boolean>>(); }
		
		// STEP 3: Get positive weight inputs
		List<Pair<EdgeStruct,Boolean>> modelEdges = new ArrayList<Pair<EdgeStruct,Boolean>>();
		for(Pair<Edge,Boolean> pair : flowEdge.getPath()) {
			modelEdges.add(new Pair<EdgeStruct,Boolean>(pair.getX().getStruct(),pair.getY()));
		}
		return modelEdges;
	}
}
