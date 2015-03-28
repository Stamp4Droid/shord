package stamp.analyses;

import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph.EdgeTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPointsToRelationManager;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.bddbddb.Dom;
import chord.project.Chord;

@Chord(name = "tests")
public class TestsAnalysis extends JavaAnalysis {
	private static MultivalueMap<String,String> getStampEdges(String relationName, String domName, Filter<Pair<String,String>> edgeFilter) {
		ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relationName);
		Dom<?> dom = (Dom<?>)ClassicProject.g().getTrgt(domName);
		rel.load();
		MultivalueMap<String,String> result = new MultivalueMap<String,String>();
		for(int[] tuple : rel.getAryNIntTuples()) {
			String caller = ConversionUtils.getMethodForVar((VarNode)dom.get(tuple[1])).toString();
			String callee = ConversionUtils.getMethodForVar((VarNode)dom.get(tuple[0])).toString();
			if(edgeFilter.filter(new Pair<String,String>(caller, callee))) {
				result.add(domName + Integer.toString(tuple[1]), domName + Integer.toString(tuple[0]));
			}
		}
		rel.close();
		return result;
	}
	
	public static MultivalueMap<String,String> union(MultivalueMap<String,String> ... edgeLists) {
		MultivalueMap<String,String> result = new MultivalueMap<String,String>();
		for(MultivalueMap<String,String> edges : edgeLists) {
			for(String source : edges.keySet()) {
				for(String sink : edges.get(source)) {
					result.add(source, sink);
				}
			}
		}
		return result;
	}
	
	// prefixes
	private static final String[] stampMethods =  new String[]{"<java.", "<android.", "<edu.stanford.stamp"};
	
	// filter methods by prefix
	private static boolean isStampMethod(String method) {
		for(String name : stampMethods) {
			if(method.startsWith(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static Filter<EdgeStruct> getFilter(final MultivalueMap<String,String> edges) {
		return new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return edges.get(edge.sourceName).contains(edge.sinkName);
			}};
	}
	
	private static Filter<EdgeStruct> getStampEdgeFilter() {		
		// filter callgraph edges by caller
		final Filter<Pair<String,String>> callerFilter = new Filter<Pair<String,String>>() {
			@Override
			public boolean filter(Pair<String,String> edge) {
				return !isStampMethod(edge.getX());
			}};
		
		// filter callgraph edges by callee
		final Filter<Pair<String,String>> calleeFilter = new Filter<Pair<String,String>>() {
			@Override
			public boolean filter(Pair<String,String> edge) {
				return !isStampMethod(edge.getY());
			}};
		
		// edges
		final MultivalueMap<String,String> edges = union(getStampEdges("param", "V", calleeFilter), getStampEdges("paramPrim", "U", calleeFilter), getStampEdges("return", "V", callerFilter), getStampEdges("returnPrim", "U", callerFilter));
		
		// filter
		return getFilter(edges);
	}
	
	private static Filter<EdgeStruct> getBaseEdgeSymbolFilter() {
		return new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return edge.symbol.equals("param") || edge.symbol.equals("return")
						|| edge.symbol.equals("paramPrim") || edge.symbol.equals("returnPrim");
			}};
	}
	
	private static Filter<EdgeStruct> getBaseEdgeFilter() {
		return new AndFilter<EdgeStruct>(getStampEdgeFilter(), getBaseEdgeSymbolFilter());
	}
	
	private static Filter<EdgeStruct> getInitialEdgeFilter() {
		return new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				return edge.symbol.equals("Src2Sink");
			}};
	}
	
	// for callbacks
	private static MultivalueMap<String,String> getGraphEdgesFromFile(String relationName, String extension) {
		MultivalueMap<String,String> result = new MultivalueMap<String,String>();
		try {
			for(Pair<String,String> pair : IOUtils.readGraphEdgesFromFile(relationName, extension).keySet()) {
				result.add(pair.getX(), pair.getY());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// for filtering non callback (param, paramPrim, Src2Sink, etc.) edges
	private static Filter<EdgeStruct> getGraphEdgeFilter(String relationName, String extension) {
		final MultivalueMap<String,String> edges = getGraphEdgesFromFile(relationName, extension);
		return new Filter<EdgeStruct>() {
			@Override
			public boolean filter(EdgeStruct edge) {
				String source = ConversionUtils.toStringShord(edge.sourceName);
				String sink = ConversionUtils.toStringShord(edge.sinkName);
				return !edges.get(source).contains(sink);
			}
		};
	}
	
	private static void printGraphEdgesToFile(Graph gbar, boolean useCallbacks) {
		if(useCallbacks) {
			String extension = IOUtils.graphEdgesFileExists("param", "graph") ? "graph_new" : "graph";
			IOUtils.printGraphEdgesToFile(gbar, "param", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "paramPrim", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "return", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "returnPrim", true, extension);
			IOUtils.printGraphEdgesToFile(gbar, "Src2Sink", true, extension);
		}
	}
	
	private static Filter<EdgeStruct> getBaseEdgeFilter(boolean useCallbacks) {
		return useCallbacks ? new AndFilter(getBaseEdgeFilter(), getGraphEdgeFilter("param", "graph"), getGraphEdgeFilter("paramPrim", "graph"), getGraphEdgeFilter("return", "graph"), getGraphEdgeFilter("returnPrim", "graph")) : getBaseEdgeFilter();
	}
	
	private static Filter<EdgeStruct> getInitialEdgeFilter(boolean useCallbacks) {
		return useCallbacks ? new AndFilter(getInitialEdgeFilter(), getGraphEdgeFilter("Src2Sink", "graph")) : getInitialEdgeFilter();
	}

	@Override
	public void run() {
		// STEP 0: Inputs
		boolean useCallbacks = false;
		
		// STEP 1: Setup
		RelationReader reader = new ShordRelationReader();
		RelationManager relations = new TaintPointsToRelationManager();
		ContextFreeGrammarOpt grammar = new TaintPointsToGrammar().getOpt();
		
		// STEP 2: Get edgestruct filters
		final Filter<EdgeStruct> baseEdgeFilter = getBaseEdgeFilter(useCallbacks);
		Filter<EdgeStruct> initialEdgeFilter = getInitialEdgeFilter(useCallbacks);
		
		// STEP 3: Get graph
		Graph g = reader.readGraph(relations, grammar.getSymbols());
		
		// STEP 4: Print statistics and relations
		Graph gw = g.transform(new EdgeTransformer(g.getVertices(), g.getSymbols()) {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edge) {
				gb.addOrUpdateEdge(new EdgeStruct(edge.sourceName, edge.sinkName, edge.symbol, edge.field, baseEdgeFilter.filter(edge) ? (short)1 : (short)0));
			}});
		Graph gwbar = gw.transform(new ReachabilitySolver(g.getVertices(), grammar, reader.readFilter(g.getVertices(), grammar.getSymbols())));
		
		System.out.println("Printing graph edges:");
		IOUtils.printGraphStatistics(gwbar);
		IOUtils.printGraphEdges(gwbar, "Src2Sink", true);
		printGraphEdgesToFile(gwbar, useCallbacks);
		
		// STEP 5: Abductive inference
		MultivalueMap<EdgeStruct,Integer> results = new AbductiveInference(grammar).process(baseEdgeFilter, initialEdgeFilter, g, reader.readFilter(g.getVertices(), grammar.getSymbols()), 2);
		IOUtils.printAbductionResult(results, true, useCallbacks);
		
		// TEMP CODE
		Graph gbar = g.transform(new ReachabilitySolver(g.getVertices(), grammar));
		IOUtils.printGrammar(grammar);
		IOUtils.printGraphToFile(g, false);
		IOUtils.printGraphToFile(gbar, true);
	}
}
