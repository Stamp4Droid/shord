package stamp.missingmodels.util.cflsolver.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.Util.Counter;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeFilter;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;

public class PrintingUtils {
	// if shord = true, prints "edgeSymbol: sourceToString -> sinkToString"
	// otherwise, prints "sourceId sinkId"
	public static void printGraphEdges(Graph g, final String symbol, boolean shord) {
		Map<String,Edge> edges = new HashMap<String,Edge>();
		for(Edge edge : g.getEdges(new EdgeFilter() {
			@Override
			public boolean filter(Edge edge) {
				return edge.getSymbol().equals(symbol); 
			}})) {
			
			if(shord) {
				String source = ConversionUtils.toStringShord(edge.source.name);
				String sink = ConversionUtils.toStringShord(edge.sink.name);
				edges.put(edge.getSymbol() + ": " + source + " -> " + sink, edge);
			} else {
				edges.put(edge.sink.name.substring(1) + " " + edge.source.name.substring(1), edge);
			}
		}
		List<String> edgeList = new ArrayList<String>(edges.keySet());
		Collections.sort(edgeList);
		System.out.println("Printing " + symbol + " edges:");
		for(String edge : edgeList) {
			System.out.println(edge);
			if(shord) {
				System.out.println(edges.get(edge) + ", weight: " + edges.get(edge).getInfo().weight);
			}
		}
	}
	
	// prints "tuple0, tuple1, ..., tuplek"
	public static void printRelation(String relationName) {
		System.out.println("Printing " + relationName);
		ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relationName);
		rel.load();
		for(Object[] tuple : rel.getAryNValTuples()) {
			StringBuilder sb = new StringBuilder();
			sb.append(relationName).append(", ");
			for(int i=0; i<tuple.length-1; i++) {
				sb.append(tuple[i]).append(", ");
			}
			sb.append(tuple[tuple.length-1]);
			System.out.println(sb.toString());
		}
		rel.close();
	}
	
	public static void printAbductionResult(MultivalueMap<EdgeStruct,Integer> result, boolean shord) {
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
	
	public static void printGraphStatistics(Graph g) {
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
	

}
