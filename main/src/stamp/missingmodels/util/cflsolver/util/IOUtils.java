package stamp.missingmodels.util.cflsolver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeFilter;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;

public class IOUtils {
	public static File getAppOutputDirectory() {
		// stamp output directory
		File stampDirectory = new File(System.getProperty("stamp.out.dir"));
		
		// output directory
		File outputDirectory = new File(stampDirectory.getParentFile().getParentFile(), "tests_output");
		
		// app output directory
		File appOutputDirectory = new File(outputDirectory, getAppName());
		appOutputDirectory.mkdirs();
		
		return appOutputDirectory;
	}
	
	public static String getAppName() {
		String[] tokens = new File(System.getProperty("stamp.out.dir")).getName().split("_");
		return tokens[tokens.length-1];
	}
	
	public static void printRelation(String relationName) {
		System.out.println("Printing " + relationName);
		printRelation(relationName, new PrintWriter(System.out), true);
	}
	
	public static void printRelationToFile(String relationName, String extension) throws IOException {
		PrintWriter pw = new PrintWriter(new File(getAppOutputDirectory(), relationName + "." + extension));
		printRelation(relationName, pw, false);
		pw.close();
	}
	
	public static boolean relationFileExists(String relationName, String extension) {
		return new File(getAppOutputDirectory(), relationName + "." + extension).exists();
	}
	
	public static List<String[]> readRelationFromFile(String relationName, String extension) throws IOException {
		List<String[]> relationTuples = new ArrayList<String[]>();
		BufferedReader bw = new BufferedReader(new FileReader(new File(getAppOutputDirectory(), relationName + "." + extension)));
		String line;
		while((line = bw.readLine()) != null) {
			relationTuples.add(line.split("#"));
		}
		bw.close();
		return relationTuples;
	}
	
	public static void printGraphEdges(Graph g, String symbol, boolean shord) {
		System.out.println("Printing edges: " + symbol);
		printGraphEdges(g, symbol, shord, new PrintWriter(System.out), true);
	}
	
	public static void printGraphEdgesToFile(Graph g, String symbol, boolean shord, String extension) throws IOException {
		PrintWriter pw = new PrintWriter(new File(getAppOutputDirectory(), symbol + "." + extension));
		printGraphEdges(g, symbol, shord, pw, false);
		pw.close();
	}
	
	public static boolean graphEdgesFileExists(String symbol, String extension) {
		return new File(getAppOutputDirectory(), symbol + "." + extension).exists();
	}
	
	public static Map<Pair<String,String>,Integer> readGraphEdgesFromFile(String symbol, String extension) throws IOException {
		Map<Pair<String,String>,Integer> edges = new HashMap<Pair<String,String>,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(new File(getAppOutputDirectory(), symbol + "." + extension)));
		String line;
		while((line = br.readLine()) != null) {
			String[] tokens = line.split("#");
			edges.put(new Pair<String,String>(tokens[0], tokens[1]), Integer.parseInt(tokens[2]));
		}
		br.close();
		return edges;
	}
	
	// prints "tuple0, tuple1, ..., tuplek"
	private static void printRelation(String relationName, PrintWriter pw, boolean prependRelationName) {
		ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relationName);
		rel.load();
		for(Object[] tuple : rel.getAryNValTuples()) {
			StringBuilder sb = new StringBuilder();
			if(prependRelationName) {
				sb.append(relationName).append("#");
			}
			for(int i=0; i<tuple.length-1; i++) {
				sb.append(tuple[i]).append("#");
			}
			sb.append(tuple[tuple.length-1]);
			pw.println(sb.toString());
		}
		rel.close();
	}
	
	// if shord = true, prints "edgeSymbol#sourceToString#sinkToString"
	// otherwise, prints "edgeSymbol#source#sink"
	private static void printGraphEdges(Graph g, final String symbol, boolean shord, PrintWriter pw, boolean prependRelationName) {
		Set<String> edgeStrings = new HashSet<String>();
		for(Edge edge : g.getEdges(new EdgeFilter() {
			@Override
			public boolean filter(Edge edge) {
				return edge.getSymbol().equals(symbol);
			}})) {
			StringBuilder sb = new StringBuilder();
			if(prependRelationName) {
				sb.append(edge.getSymbol()).append("#");
			}
			sb.append(shord ? ConversionUtils.toStringShord(edge.source.name) : edge.source.name.substring(1)).append("#");
			sb.append(shord ? ConversionUtils.toStringShord(edge.sink.name) : edge.sink.name.substring(1)).append("#");
			sb.append(edge.getInfo().weight);
			edgeStrings.add(sb.toString());
		}
		List<String> edgeList = new ArrayList<String>(edgeStrings);
		Collections.sort(edgeList);
		for(String edge : edgeList) {
			pw.println(edge);
		}
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
