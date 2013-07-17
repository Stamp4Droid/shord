package stamp.missingmodels.viz.flow;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.analyses.DomC;
import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.analyses.LocalVarNode;
import shord.analyses.ParamVarNode;
import shord.analyses.RetVarNode;
import shord.analyses.ThisVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import soot.Local;
import soot.SootMethod;
import stamp.analyses.DomL;
import stamp.analyses.JCFLSolverAnalysis;
import stamp.missingmodels.util.StubLookup.StubLookupKey;
import stamp.missingmodels.util.StubLookup.StubLookupValue;
import stamp.missingmodels.util.Util.Counter;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Edge;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.viz.flow.FlowObject.AliasCompressedFlowObject;
import stamp.srcmap.Expr;
import stamp.srcmap.RegisterMap;
import stamp.srcmap.SourceInfo;

public class FlowWriter {
	/*
	 * This function tokenizes graph node names in one of two ways:
	 * a) "V1_2" -> ["V", 1, 2]
	 * b) "L1" -> ["L", 1]
	 */
	private static String[] tokenizeNodeName(String node) {
		String[] result;
		String dom = node.replaceAll("[^a-zA-Z]", "");
		if(!node.startsWith(dom)) {
			throw new RuntimeException("Invalid node name " + node + "!");
		}
		if(node.contains("_")) {
			result = new String[3];
			String[] tokens = node.split("_");
			if(tokens.length != 2) {
				System.out.println("Invalid node name " + node + "!");
			}
			result[1] = tokens[0].substring(dom.length());
			result[2] = tokens[1];
		} else {
			result = new String[2];
			result[1] = node.substring(dom.length());
		}
		result[0] = dom;
		return result;
	}

	/*
	 * A cache of register maps.
	 */
	private static Map<String,RegisterMap> registerMaps = new HashMap<String,RegisterMap>();
	
	/*
	 * Returns a register map, caching them as they are requested.
	 */
	private static RegisterMap getRegisterMap(SootMethod method) {
		RegisterMap registerMap = registerMaps.get(method.toString());
		if(registerMap == null) {
			registerMap = SourceInfo.buildRegMapFor(method);
			registerMaps.put(method.toString(), registerMap);
		}
		return registerMap;
	}

	/*
	 * Returns source information about the node.
	 * a) If the node name is in DomL (starts with L), then
	 * maps "L1" -> ["L", label].
	 * b) If the node name is in DomV (starts with V), then
	 * returns [hyper link to containing method, hyper link
	 * to variable, contextId]. 
	 * c) If the node name is in DomU, then similar to (b).
	 * d) In any other case, just returns the given string in
	 * a length one array.
	 * NOTE: if source information for the variable can't be
	 * found, then the variableId is returned in place of
	 * the second hyper link.
	 */
	private static String[] getNodeInfoHelper(String node) {
		try {
			// STEP 1: tokenize the node name
			String[] tokens = tokenizeNodeName(node);

			// STEP 2: parse labels, reference variables, and primitive variables 
			if(tokens[0].equals("L")) {
				// STEP 2a: if it is a label, then get the string
				DomL dom = (DomL)ClassicProject.g().getTrgt("L");
				tokens[1] = dom.toUniqueString(Integer.parseInt(tokens[1]));
			} else if(tokens[0].equals("V") || tokens[0].equals("U")) {
				// STEP 2b: if it is a variable, then get the variable and method information
				
				// get the register from the domain
				VarNode register;
				if(tokens[0].equals("V")) {
					DomV dom = (DomV)ClassicProject.g().getTrgt(tokens[0].toUpperCase());
					register = dom.get(Integer.parseInt(tokens[1]));
				} else {
					DomU dom = (DomU)ClassicProject.g().getTrgt(tokens[0].toUpperCase());
					register = dom.get(Integer.parseInt(tokens[1]));
				}

				// look up the method and local from the register
				SootMethod method = null;
				Local local = null;
				if(register instanceof LocalVarNode) {
					LocalVarNode localRegister = (LocalVarNode)register;
					local = localRegister.local;
					method = localRegister.meth;
				} else if(register instanceof ThisVarNode) {
					ThisVarNode thisRegister = (ThisVarNode)register;
					method = thisRegister.method;
				} else if(register instanceof ParamVarNode) {
					ParamVarNode paramRegister = (ParamVarNode)register;
					method = paramRegister.method;
				} else if(register instanceof RetVarNode) {
					RetVarNode retRegister = (RetVarNode)register;
					method = retRegister.method;
				}

				// HTML hyper link to the method
				String sourceFileName = method == null ? "" : SourceInfo.filePath(method.getDeclaringClass());
				int methodLineNum = SourceInfo.methodLineNum(method);

				String methStr = "<a onclick=\"showSource('" + sourceFileName + "','false','" + methodLineNum + "')\">" + "[" + method.getName() + "]</a> ";

				// HTML hyper link to the register
				RegisterMap regMap = getRegisterMap(method);
				Set<Expr> locations = regMap.srcLocsFor(local);
				Integer registerLineNum = null;
				String text = null;
				if(locations != null) {
					for(Expr location : locations) {
						if(location.start() >= 0 && location.length() >= 0 && location.text() != null) {
							registerLineNum = location.line();
							text = location.text();
							break;
						}
					}
				}

				// store the links in the tokens and return
				if(registerLineNum != null) {
					tokens[0] = methStr;
					tokens[1] = "<a onclick=\"showSource('" + sourceFileName + "','false','" + registerLineNum + "')\">" + text + "</a>";
				} else {
					tokens[0] = methStr+tokens[0];
				}

				// if the context exists, get the context
				/*
				if(tokens.length == 3) {
					DomC domC = (DomC)ClassicProject.g().getTrgt("C");
					tokens[2] = domC.toUniqueString(Integer.parseInt(tokens[2]));
				}
				*/
			}
			return tokens;
		} catch(Exception e) {
			String[] tokens = {node};
			return tokens;
		}
	}

	/*
	 * Gets the node info and concatenates into a String.
	 */
	public static String getNodeInfo(String node) {
		String[] tokens = getNodeInfoHelper(node);
		return tokens[0] + (tokens.length >= 2 ? tokens[1] : ""); /*+ (tokens.length == 3 ? "_" + tokens[2] : "")*/
	}

	public static void printAllStubs() throws IOException {
		PrintWriter pw = new PrintWriter(new File("cfl/Stubs.out"));
		/*
		for(StubLookupValue stub : JCFLSolverAnalysis.getAllStubs().values()) {
	    	if(!printedStubs.contains(stub.method)) {
				StubInfo info = new StubInfo(stub);
				pw.println(stub.method + " " + info.method.toString());
				printedStubs.add(stub.method);
	    	}
		}
		 */
		Set<String> printedStubs = new HashSet<String>();
		for(Map.Entry<StubLookupKey,StubLookupValue> entry : JCFLSolverAnalysis.getAllStubs().entrySet()) {
			StubLookupKey key = entry.getKey();
			StubLookupValue info = entry.getValue();
			if(key.source.startsWith("M") && !printedStubs.contains(key.source)) {
				pw.println(key.source + " " + info.method.toString());
				printedStubs.add(key.source);
			}
			if(key.sink.startsWith("M") && !printedStubs.contains(key.sink)) {
				pw.println(key.sink + " " + info.method.toString());
				printedStubs.add(key.sink);
			}
		}
		pw.close();

		pw = new PrintWriter("cfl/StubModels.out");
		for(Map.Entry<StubLookupKey,StubLookupValue> entry : JCFLSolverAnalysis.getAllStubs().entrySet()) {
			StubLookupKey key = entry.getKey();
			StubLookupValue info = entry.getValue();
			pw.println(key.symbol + "," + key.source + "," + key.sink + " " + info.toString());
		}
		pw.close();
	}

	public static void printStubInputs(Graph g, File outputDir) throws IOException {
		PrintWriter pw = new PrintWriter(new File("cfl/Src2SinkStubs.out"));
		Counter<String> keys = new Counter<String>();

		for(Edge edge : g.getEdges("Src2Sink")) {
			pw.println(edge.from.getName() + " -> " + edge.to.getName());

			String[] sourceTokens = getNodeInfoHelper(edge.from.getName());
			String source = sourceTokens[1];

			String[] sinkTokens = getNodeInfoHelper(edge.to.getName());
			String sink = sinkTokens[1];
			pw.println(source + " -> " + sink);

			List<Pair<Edge,Boolean>> path = g.getPath(edge);
			for(Pair<Edge,Boolean> pair : path) {
				EdgeData data = pair.getX().getData(g);
				boolean forward = pair.getY();

				if(data.weight > 0) {
					StubLookupValue info = JCFLSolverAnalysis.lookup(data, forward);
					String line;
					if(info != null) {
						line = data.toString(forward) + ": " + info.toString();
					} else {
						line = data.toString(forward) + ": " + "NOT_FOUND";
					}
					//keys.increment(new StubLookupKey(data.symbol, data.from, data.to));
					keys.increment(line);
					pw.println(line);
				}
			}
			pw.println();
		}
		pw.close();

		pw = new PrintWriter(new File("cfl/Src2SinkAllStubs.out"));
		for(String key: keys.sortedKeySet()) {
			pw.println(key + " " + keys.getCount(key));
		}
		pw.close();
	}

	public static void printFlowViz(Graph g) {
		List<Pair<String,String>> edges = new ArrayList<Pair<String,String>>();
		Set<String> sources = new HashSet<String>();
		Set<String> sinks = new HashSet<String>();
		for(Edge edge : g.getEdges("Src2Sink")) {
			List<Pair<Edge,Boolean>> path = g.getPath(edge);

			if(path.size() >= 2) {
				Pair<Edge,Boolean> source = path.get(0);
				EdgeData sourceData = source.getX().getData(g);
				StubLookupValue sourceInfo = JCFLSolverAnalysis.lookup(sourceData, source.getY());

				Pair<Edge,Boolean> sink = path.get(path.size()-1);
				EdgeData sinkData = sink.getX().getData(g);
				StubLookupValue sinkInfo = JCFLSolverAnalysis.lookup(sinkData, sink.getY());

				edges.add(new Pair<String,String>(sourceData.from, sinkData.to));

				if(sourceData.symbol.equals("cs_srcRefFlowNew") || sourceData.symbol.equals("cs_srcPrimFlowNew")) {
					sources.add(sourceData.from);
				}
				if(sinkData.symbol.equals("cs_refSinkFlowNew") || sinkData.symbol.equals("cs_primSinkFlowNew")) {
					sinks.add(sinkData.to);
				}
				/*
				if(sourceInfo != null && sinkInfo != null) {
		    		//edges.add(new Pair<String,String>(sourceInfo.toString(), sinkInfo.toString()));
				} else {
		    		System.out.println("ERROR: Src2Sink path length too short!");
				}
				*/
			}
		}
		FlowGraphViz.viz.viz(new FlowGraphViz.FlowGraph(edges, sources, sinks));
	}

	public static String parseEdge(EdgeData edge, boolean forward) {
		StringBuilder sb = new StringBuilder();
		sb.append(edge.toString(forward));

		StubLookupValue info = JCFLSolverAnalysis.lookup(edge, forward);
		if(info == null) {
			return sb.toString();
		}

		String sourceFileName = SourceInfo.filePath(info.method.getDeclaringClass());
		int methodLineNum = SourceInfo.methodLineNum(info.method);

		// TODO: make this print more than just the method name
		String methStr = " <a onclick=\"showSource('" + sourceFileName + "','false','" + methodLineNum + "')\">" + "[" + info.method.getName() + "]</a>";
		sb.append(methStr);

		return sb.toString();
	}

	public static void viz(Graph g, File outputDir) throws IOException {
		Set<String> terminals = new HashSet<String>();
		terminals.add("FlowsTo");

		for(Edge edge : g.getEdges("Src2Sink")) {
			String[] sourceTokens = getNodeInfoHelper(edge.from.getName());
			String source = sourceTokens[1].substring(1);

			String[] sinkTokens = getNodeInfoHelper(edge.to.getName());
			String sink = sinkTokens[1].substring(1);

			PrintWriter pw = new PrintWriter(new File("cfl/" + source + "2" + sink + ".out"));
			//pw.println(new MethodCompressedFlowObject(g.getPath(edge), g).toString());
			pw.println(new AliasCompressedFlowObject(g.getPath(edge, terminals), g).toString());
			pw.close();
		}
	}
}
