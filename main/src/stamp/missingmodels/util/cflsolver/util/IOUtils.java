package stamp.missingmodels.util.cflsolver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.analyses.DomL;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Util.Counter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;

public class IOUtils {
	private static final String SEPARATOR = "##";
	
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
		PrintWriter pw = new PrintWriter(System.out);
		printRelation(relationName, pw, true);
		pw.flush();
	}
	
	public static void printRelationToFile(String relationName, String extension) {
		try {
			PrintWriter pw = new PrintWriter(new File(getAppOutputDirectory(), relationName + "." + extension));
			printRelation(relationName, pw, false);
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading file!");
		}
	}
	
	public static boolean relationFileExists(String relationName, String extension) {
		return new File(getAppOutputDirectory(), relationName + "." + extension).exists();
	}
	
	public static List<String[]> readRelationFromFile(String relationName, String extension) {
		try {
			List<String[]> relationTuples = new ArrayList<String[]>();
			BufferedReader bw = new BufferedReader(new FileReader(new File(getAppOutputDirectory(), relationName + "." + extension)));
			String line;
			while((line = bw.readLine()) != null) {
				relationTuples.add(line.split(SEPARATOR));
			}
			bw.close();
			return relationTuples;
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading file!");
		}
	}
	
	public static void printGraphToFile(Graph g, boolean isBar) {
		try {
			File dir = new File("graph_output");
			dir.mkdirs();
			File file = new File(dir, getAppName() + ".graph" + (isBar ? "_bar" : ""));
			PrintWriter pw = new PrintWriter(file);
			for(EdgeStruct edge : g.getEdgeStructs()) {
				StringBuilder str = new StringBuilder();
				str.append(edge.sourceName).append(",");
				str.append(edge.sinkName).append(",");
				str.append(edge.symbol).append(",");
				str.append(edge.field);
				pw.println(str);
			}
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error writing file!");
		}
	}
	
	public static void printGrammar(ContextFreeGrammarOpt cfg) {
		try {
			File dir = new File("graph_output");
			dir.mkdirs();
			File file = new File(dir, "flow.grammar");
			PrintWriter pw = new PrintWriter(file);
			for(int i=0; i<cfg.binaryProductionsByTarget.length; i++) {
				for(UnaryProduction up : cfg.unaryProductionsByTarget[i]) {
					StringBuilder sb = new StringBuilder();
					sb.append("unary").append(",");
					sb.append(up.target.symbol).append(",");
					sb.append(up.input.symbol).append(",");
					sb.append(up.isInputBackwards).append(",");
					sb.append(up.ignoreFields);
					pw.println(sb);
				}
				for(BinaryProduction bp : cfg.binaryProductionsByTarget[i]) {
					StringBuilder sb = new StringBuilder();
					sb.append("binary").append(",");
					sb.append(bp.target.symbol).append(",");
					sb.append(bp.firstInput.symbol).append(",");
					sb.append(bp.isFirstInputBackwards).append(",");
					sb.append(bp.secondInput.symbol).append(",");
					sb.append(bp.isSecondInputBackwards).append(",");
					sb.append(bp.ignoreFields);
					pw.println(sb);
				}
				for(AuxProduction ap : cfg.auxProductionsByTarget[i]) {
					StringBuilder sb = new StringBuilder();
					sb.append("aux").append(",");
					sb.append(ap.target.symbol).append(",");
					sb.append(ap.input.symbol).append(",");
					sb.append(ap.isInputBackwards).append(",");
					sb.append(ap.auxInput.symbol).append(",");
					sb.append(ap.isAuxInputBackwards).append(",");
					sb.append(ap.ignoreFields);
					pw.println(sb);
				}
			}
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error writing file!");
		}
	}
	
	public static void printGraphEdges(Graph g, String symbol, boolean shord) {
		System.out.println("Printing edges: " + symbol);
		PrintWriter pw = new PrintWriter(System.out);
		printGraphEdges(g, symbol, shord, pw);
		pw.flush();
	}
	
	public static void printGraphEdgesFormatted(Graph g, final String symbol, boolean shord, PrintWriter pw) {
		Set<String> edgeStrings = new HashSet<String>();
		for(Edge edge : g.getEdges(new Filter<Edge>() { public boolean filter(Edge edge) { return edge.symbol.symbol.equals(symbol); }})) {
			StringBuilder sb = new StringBuilder();
			sb.append(shord ? ConversionUtils.toStringShord(edge.source.name) : edge.source.name).append(SEPARATOR);
			sb.append(shord ? ConversionUtils.toStringShord(edge.sink.name) : edge.sink.name).append(SEPARATOR);
			sb.append(edge.field.field).append(SEPARATOR);
			sb.append(edge.weight);
			edgeStrings.add(sb.toString());
		}
		List<String> edgeList = new ArrayList<String>(edgeStrings);
		Collections.sort(edgeList);
		for(String edge : edgeList) {
			pw.println(edge);
		}
	}
	
	public static void printGraphEdgesToFile(Graph g, String symbol, boolean shord, String extension) {
		try {
			PrintWriter pw = new PrintWriter(new File(getAppOutputDirectory(), symbol + "." + extension));
			printGraphEdgesFormatted(g, symbol, shord, pw);
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading file!");
		}
	}
	
	public static boolean graphEdgesFileExists(String symbol, String extension) {
		return new File(getAppOutputDirectory(), symbol + "." + extension).exists();
	}
	
	public static Iterable<EdgeStruct> readGraphEdgesFromFile(String symbol, String extension) {
		try {
			List<EdgeStruct> edges = new ArrayList<EdgeStruct>();
			BufferedReader br = new BufferedReader(new FileReader(new File(getAppOutputDirectory(), symbol + "." + extension)));
			String line;
			while((line = br.readLine()) != null) {
				String[] tokens = line.split(SEPARATOR);
				edges.add(new EdgeStruct(tokens[0], tokens[1], symbol, Integer.parseInt(tokens[2]), Short.parseShort(tokens[3])));
			}
			br.close();
			return edges;
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading file!");
		}
	}
	
	// prints "tuple0, tuple1, ..., tuplek"
	private static void printRelation(String relationName, PrintWriter pw, boolean prependRelationName) {
		ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relationName);
		rel.load();
		try {
			for(Object[] tuple : rel.getAryNValTuples()) {
				StringBuilder sb = new StringBuilder();
				if(prependRelationName) {
					sb.append(relationName).append(SEPARATOR);
				}
				for(int i=0; i<tuple.length-1; i++) {
					sb.append(tuple[i]).append(SEPARATOR);
				}
				sb.append(tuple[tuple.length-1]);
				pw.println(sb.toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		rel.close();
	}
	
	private static void printGraphEdges(Graph g, final String symbol, boolean shord, PrintWriter pw) {
		Set<String> edgeStrings = new HashSet<String>();
		for(Edge edge : g.getEdges(new Filter<Edge>() { public boolean filter(Edge edge) { return edge.symbol.symbol.equals(symbol); }})) {
			edgeStrings.add(edge.toString(shord));
		}
		List<String> edgeList = new ArrayList<String>(edgeStrings);
		Collections.sort(edgeList);
		for(String edge : edgeList) {
			pw.println(edge);
		}
	}
	
	public static void printAbductionResult(MultivalueMap<EdgeStruct,Integer> result, boolean shord, boolean useCallbacks) {
		ProgramRel relPotentialCallbackDependent = null;
		if(useCallbacks) {
			relPotentialCallbackDependent = (ProgramRel)ClassicProject.g().getTrgt("potentialCallbackDependent");
			relPotentialCallbackDependent.load();
		}
		
		Counter<Integer> totalCut = new Counter<Integer>();
		for(EdgeStruct edgeStruct : result.keySet()) {
			if(result.get(edgeStruct).size() > 1) {
				System.out.println("ERROR: Multiple cuts for edge " + edgeStruct);
			}
			for(int cut : result.get(edgeStruct)) {
				System.out.println("in cut " + cut + ": " + edgeStruct.toString());
				System.out.println("in cut " + cut + ": " + edgeStruct.toString(true));
				if(shord) {
					System.out.println("caller: " + ConversionUtils.getMethodSig(edgeStruct.sourceName));
					System.out.println("callee: " + ConversionUtils.getMethodSig(edgeStruct.sinkName));
					if(useCallbacks) {
						for(chord.util.tuple.object.Pair<Object, Object> pair : relPotentialCallbackDependent.getAry2ValTuples()) {
							if(ConversionUtils.getMethodSig(edgeStruct.sourceName).equals(pair.val1.toString())) {
								System.out.println("potential callback dependent: " + pair.val0);
							}
						}
					}
				}
				totalCut.increment(cut);
			}
		}
		for(int i : totalCut.keySet()) {
			System.out.println("total cut " + i + ": " + totalCut.getCount(i));
		}
		
		if(useCallbacks) {
			relPotentialCallbackDependent.close();
		}
	}
	
	public static void printGraphStatistics(Graph g) {
		int totalNumEdges = 0;
		for(int symbolInt=0; symbolInt<g.getSymbols().getNumSymbols(); symbolInt++) {
			final String symbol = g.getSymbols().get(symbolInt).symbol;
			if(!symbol.equals(symbol)) continue;
			Set<String> edges = new HashSet<String>();
			for(Edge edge : g.getEdges(new Filter<Edge>() {
				@Override
				public boolean filter(Edge edge) {
					return edge.symbol.symbol.equals(symbol); 
				}})) {
				edges.add(edge.sink.name.substring(1) + " " + edge.source.name.substring(1));
			}
			System.out.println(symbol + ": " + edges.size());
			totalNumEdges += edges.size();
		}
		System.out.println("total edges: " + totalNumEdges);
	}
	
	// CLEANUP: from AbductiveInferenceUtils
	public static void printSourceSinkEdgePaths(Graph g, Iterable<Edge> sourceSinkEdges, boolean shord) {
		DomL dom = shord ? (DomL)ClassicProject.g().getTrgt("L") : null;
		for(Edge edge : sourceSinkEdges) {
			System.out.println("Cutting Src2Sink edge: " + edge.toString());
			System.out.println("Edge weight: " + edge.weight);
			if(dom != null) {
				String source = dom.get(Integer.parseInt(edge.source.name.substring(1)));
				String sink = dom.get(Integer.parseInt(edge.sink.name.substring(1)));
				System.out.println("Edge represents source-sink flow: " + source + " -> " + sink);
			}
			System.out.println("Starting edge path...");
			for(Pair<Edge,Boolean> pathEdgePair : edge.getPath()) {
				System.out.println("weight " + pathEdgePair.getX().weight + ", " + "isForward " + pathEdgePair.getY() + ": " + pathEdgePair.getX().toString(shord));
			}
			System.out.println("Ending edge path");
		}		
	}
	
	// CLEANUP NEEDED: from TraceReader
	public static void printCallGraph(MultivalueMap<String,String> callgraph, PrintWriter pw) {
		int callgraphSize = 0;
		for(String caller : callgraph.keySet()) {
			callgraphSize += callgraph.get(caller).size();
			for(String callee : callgraph.get(caller)) {
				System.out.println(caller + " -> " + callee);
			}
		}
		System.out.println("callgraph size: " + callgraphSize);
	}
}
