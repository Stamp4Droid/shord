package stamp.analyses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;
import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.Util.Counter;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeFilter;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.graph.GraphTransformer;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager.Relation;
import stamp.missingmodels.util.cflsolver.relation.RelationManager.ShordRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar taintGrammar = new ContextFreeGrammar();
	static {
		// pt rules
		taintGrammar.addUnaryProduction("Flow", "alloc");
		
		taintGrammar.addBinaryProduction("Flow", "Flow", "assign");
		
		taintGrammar.addBinaryProduction("Flow", "Flow", "param");
		taintGrammar.addBinaryProduction("Flow", "Flow", "return");
		
		taintGrammar.addBinaryProduction("Flow", "Flow", "dynparam");
		taintGrammar.addBinaryProduction("Flow", "Flow", "dynreturn");
		
		taintGrammar.addProduction("FlowField", new String[]{"Flow", "store", "Flow"}, new boolean[]{false, false, true});
		taintGrammar.addProduction("Flow", new String[]{"FlowField", "Flow", "load"});
		
		taintGrammar.addBinaryProduction("FlowStatField", "Flow", "storeStat");
		taintGrammar.addBinaryProduction("Flow", "FlowStatField", "loadStat");
		
		taintGrammar.addProduction("FlowFieldArr", new String[]{"Flow", "storeArr", "Flow"}, new boolean[]{false, false, true});

		// object annotations
		taintGrammar.addBinaryProduction("Obj2RefT", "Flow", "ref2RefT");
		taintGrammar.addBinaryProduction("Obj2PrimT", "Flow", "ref2PrimT");
		taintGrammar.addBinaryProduction("Obj2RefT", "FlowFieldArr", "Obj2RefT");
		taintGrammar.addBinaryProduction("Obj2PrimT", "FlowFieldArr", "Obj2PrimT");
		
		taintGrammar.addBinaryProduction("Label2ObjT", "label2RefT", "Flow", false, true);
		taintGrammar.addBinaryProduction("Label2ObjT", "Label2ObjT", "FlowField", false, true, true);
		
		// sinkf
		taintGrammar.addBinaryProduction("SinkF2Obj", "sinkF2RefF", "Flow", false, true);
		taintGrammar.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Obj", "Flow", "ref2RefF", "Flow"}, new boolean[]{false, false, false, true, true});
		taintGrammar.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Prim", "ref2PrimF", "Flow"}, new boolean[]{false, false, true, true});
		taintGrammar.addBinaryProduction("SinkF2Obj", "SinkF2Obj", "FieldFlow", false, true, true);
		
		taintGrammar.addUnaryProduction("SinkF2Prim", "sinkF2PrimF");
		taintGrammar.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Obj", "Flow", "prim2RefF"}, new boolean[]{false, false, false, true});
		taintGrammar.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Prim", "prim2PrimF"}, new boolean[]{false, false, true});
		
		// source-sink flow
		taintGrammar.addProduction("Src2Sink", new String[]{"src2Label", "Label2Obj", "SinkF2Obj"}, new boolean[]{false, false, true});
		taintGrammar.addProduction("Src2Sink", new String[]{"src2Label", "Label2Prim", "SinkF2Prim"}, new boolean[]{false, false, true});
		taintGrammar.addProduction("Src2Sink", new String[]{"src2Label", "Label2PrimFld", "SinkF2Obj"}, new boolean[]{false, false, true}, true);
		
		// label-obj flow
		taintGrammar.addUnaryProduction("Label2Obj", "Label2ObjT");
		taintGrammar.addUnaryProduction("Label2Obj", "Label2ObjX");

		taintGrammar.addProduction("Label2ObjX", new String[]{"Label2Obj", "Obj2RefT", "Flow"}, new boolean[]{false, false, true});
		taintGrammar.addProduction("Label2ObjX", new String[]{"Label2Prim", "prim2RefT", "Flow"}, new boolean[]{false, false, true});
		taintGrammar.addProduction("Label2ObjX", new String[]{"Label2PrimFldArr", "Obj2RefT", "Flow"}, new boolean[]{false, false, true});
		taintGrammar.addBinaryProduction("Label2ObjX", "Label2ObjX", "FlowFieldArr", false, true);
		
		// label-prim flow
		taintGrammar.addUnaryProduction("Label2Prim", "label2PrimT");
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "assignPrim");
		
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "paramPrim");
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "returnPrim");

		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "dynparamPrim");
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "dynreturnPrim");

		taintGrammar.addBinaryProduction("Label2Prim", "Label2Obj", "Obj2PrimT");
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "Prim2PrimT");

		taintGrammar.addProduction("Label2Prim", new String[]{"Label2ObjT", "Flow", "loadPrim"}, true);
		taintGrammar.addProduction("Label2Prim", new String[]{"Label2ObjX", "Flow", "loadPrim"});
		taintGrammar.addBinaryProduction("Label2Prim", "Label2PrimFldArr", "Obj2PrimT");

		taintGrammar.addProduction("Label2Prim", new String[]{"Label2PrimFld", "Flow", "loadPrim"});
		taintGrammar.addBinaryProduction("Label2Prim", "Label2PrimFldStat", "loadStatPrim");
		
		taintGrammar.addProduction("Label2PrimFld", new String[]{"Label2Prim", "storePrim", "Flow"}, new boolean[]{false, false, true});
		taintGrammar.addProduction("Label2PrimFldArr", new String[]{"Label2Prim", "storePrimArr", "Flow"}, new boolean[]{false, false, true});
		taintGrammar.addBinaryProduction("Label2PrimFldStat", "Label2Prim", "storeStatPrim");
	}
	
	private static String getMethodSig(String name) {
		VarNode v;
		if(name.startsWith("V")) {
			DomV dom = (DomV)ClassicProject.g().getTrgt(name.substring(0,1).toUpperCase());
			v = dom.get(Integer.parseInt(name.substring(1)));
		} else if(name.startsWith("U")) {
			DomU dom = (DomU)ClassicProject.g().getTrgt(name.substring(0,1).toUpperCase());
			v = dom.get(Integer.parseInt(name.substring(1)));
		} else {
			throw new RuntimeException("Unrecognized vertex: " + name);
		}
		return RelationManager.getMethodForVar(v).toString();
	}

	public static void printResult(Map<EdgeStruct,Integer> result, boolean shord) {
		Counter<Integer> totalCut = new Counter<Integer>();
		for(EdgeStruct edgeStruct : result.keySet()) {
			if(result.get(edgeStruct) != -1) {
				System.out.println("in cut " + result.get(edgeStruct) + ": " + edgeStruct);
				if(shord) {
					System.out.println("caller: " + getMethodSig(edgeStruct.sourceName));
					System.out.println("callee: " + getMethodSig(edgeStruct.sinkName));
				}
				totalCut.increment(result.get(edgeStruct));
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
	
	@Override
	public void run() {
		try {
			printResult(AbductiveInferenceRunner.runInference(getGraphFromShord(), getTypeFilterFromShord(), true, 1), true);
		} catch(LpSolveException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws LpSolveException {
		String directoryName = "/home/obastani/Documents/projects/research/stamp/shord/stamp_output/_home_obastani_Documents_projects_research_stamp_stamptest_SymcApks_54975175131d44e08fc08811a318c440.apk/cfl";
		printResult(AbductiveInferenceRunner.runInference(getGraphFromFiles(directoryName), getTypeFilterFromFiles(directoryName), false, 3), false);
		
		//long time = System.currentTimeMillis();
		//Graph g = new ReachabilitySolver(getGraphFromFiles(directoryName), getTypeFilterFromFiles(directoryName)).getResult();
		//printGraphStatics(g);
		//printGraphEdges(g, "Flow");		
		//System.out.println("Done in " + (System.currentTimeMillis() - time) + "ms");
	}
	
	private static void readRelation(GraphBuilder gb, Relation relation) {
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getName());
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			relation.addEdge(gb, tuple);
		}
		
		rel.close();
	}
	
	public static TypeFilter getTypeFilterFromShord() {
		TypeFilter t = new TypeFilter(taintGrammar);
		
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("ptd");
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			t.add("H" + Integer.toString(tuple[1]), "V" + Integer.toString(tuple[0]));
		}
		
		rel.close();		
		
		return t;
	}
	
	public static Graph getGraphFromShord() {
		GraphBuilder gb = new GraphBuilder(taintGrammar);
		int numSymbols = gb.toGraph().getContextFreeGrammar().getNumLabels();
		
		String[] tokens = System.getProperty("stamp.out.dir").split("_");
		RelationManager relations = new ShordRelationManager(TraceReader.getCallgraphList("../../profiler/traceouts/", tokens[tokens.length-1]));
		
		for(int i=0; i<numSymbols; i++) {
			String symbol = gb.toGraph().getContextFreeGrammar().getSymbol(i);
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				readRelation(gb, relation);
			}
		}
		
		final Set<String> labels = new HashSet<String>();
		labels.add("$LOCATION");
		labels.add("$getLatitude");
		labels.add("$getLongitude");
		labels.add("$FINE_LOCATION");
		labels.add("$ACCOUNTS");
		labels.add("$getDeviceId");
		labels.add("$SMS");
		labels.add("$CONTACTS");
		labels.add("$CALENDAR");
		labels.add("!SOCKET");
		labels.add("!INTERNET");
		labels.add("!sendTextMessage");
		labels.add("!destinationAddress");
		labels.add("!sendDataMessage");
		labels.add("!sendMultipartTextMessage");
		
		labels.add("$CONTENT_PROVIDER");
		
		GraphTransformer gt = new GraphTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct, int weight) {
				DomL dom = (DomL)ClassicProject.g().getTrgt("L");
				String name = null;
				if(edgeStruct.sourceName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				} else if(edgeStruct.sinkName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				}
				if(name != null && !labels.contains(name)) {
					//if(!name.substring(1).startsWith("http")) {
						return;
					//}
				}
				gb.addEdge(edgeStruct, weight);
			}
		};
		
		return gt.transform(gb.toGraph());
	}
	
	private static void readRelation(BufferedReader br, GraphBuilder gb, String relationName) throws IOException {
		br.readLine();
		String line;
		while((line = br.readLine()) != null) {
			String[] tupleStr = line.split("\\s+");
			int[] tuple = new int[tupleStr.length];
			for(int i=0; i<tuple.length; i++) {
				tuple[i] = Integer.parseInt(tupleStr[i]);
			}
			for(Relation relation : new ShordRelationManager().getRelationsByName(relationName)) {
				relation.addEdge(gb, tuple);
			}
		}
	}
	
	public static TypeFilter getTypeFilterFromFiles(String directoryName) {
		TypeFilter t = new TypeFilter(taintGrammar);
		try {
			BufferedReader br = new BufferedReader(new FileReader(directoryName + File.separator + "ptd.txt"));
			br.readLine();
			String line;
			while((line = br.readLine()) != null) {
				String[] tupleStr = line.split("\\s+");
				t.add("H" + tupleStr[1], "V" + tupleStr[0]);
			}
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	public static Graph getGraphFromFiles(String directoryName) {
		GraphBuilder gb = new GraphBuilder(taintGrammar);
		File directory = new File(directoryName);
		for(File relationFile : directory.listFiles()) {
			try {
				String relationName = relationFile.getName().split("\\.")[0];
				BufferedReader br = new BufferedReader(new FileReader(relationFile));
				readRelation(br, gb, relationName);
			} catch(Exception e) {}
		}
		return gb.toGraph();
	}
	
	public static Graph getTestGraph() {
		// STEP 1: Build the graph
		GraphBuilder gb = new GraphBuilder(taintGrammar);
		gb.addEdge("o1", "x", "alloc");
		gb.addEdge("o2", "z", "alloc");
		gb.addEdge("x", "y", "param", new EdgeInfo(1));
		gb.addEdge("z", "y", "store");
		gb.addEdge("x", "w", "param", new EdgeInfo(1));
		gb.addEdge("w", "v", "load");
		return gb.toGraph();
	}
}
