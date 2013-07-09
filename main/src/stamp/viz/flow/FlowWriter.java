package stamp.viz.flow;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.analyses.Ctxt;
import shord.analyses.DomC;
import shord.analyses.DomM;
import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.analyses.LocalVarNode;
import shord.analyses.ParamVarNode;
import shord.analyses.RetVarNode;
import shord.analyses.ThisVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import soot.jimple.Stmt;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import stamp.analyses.DomL;
import stamp.analyses.JCFLSolverAnalysis;
import stamp.analyses.JCFLSolverAnalysis.StubLookupKey;
import stamp.analyses.JCFLSolverAnalysis.StubLookupValue;
import stamp.jcflsolver.Edge;
import stamp.jcflsolver.EdgeData;
import stamp.jcflsolver.Graph;
import stamp.jcflsolver.Util.Counter;
import stamp.jcflsolver.Util.Pair;
import stamp.reporting.TaintedVar;
import stamp.srcmap.Expr;
import stamp.srcmap.RegisterMap;
import stamp.srcmap.SourceInfo;
import stamp.viz.flow.HTMLObject.ButtonObject;
import stamp.viz.flow.HTMLObject.DivObject;

public class FlowWriter {
	// v1_2 -> [v, 1, 2]; src1 -> [src, 1]
	private static String[] toTokensHelper(String node) {
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

	private static Map<String,RegisterMap> regMaps = new HashMap<String,RegisterMap>();
	public static RegisterMap getRegisterMap(SootMethod method) {
		RegisterMap regMap = regMaps.get(method.toString());
		if(regMap == null) {
			regMap = SourceInfo.buildRegMapFor(method);
			regMaps.put(method.toString(), regMap);
		}
		return regMap;
	}

	private static String[] toTokens(String node) {
		try {
			String[] tokens = toTokensHelper(node);
			ArrayList<String> newTokens = new ArrayList<String>(Arrays.asList(tokens));

			// label domain
			if(tokens[0].equals("L")) {
				DomL dom = (DomL)ClassicProject.g().getTrgt(tokens[0].toUpperCase());
				newTokens.set(1, dom.toUniqueString(Integer.parseInt(tokens[1])));
			}

			// variable domains
			if(tokens[0].equals("V") || tokens[0].equals("U")) {
				VarNode register;
				if(tokens[0].equals("V")) {
					DomV dom = (DomV)ClassicProject.g().getTrgt(tokens[0].toUpperCase());
					register = dom.get(Integer.parseInt(tokens[1]));
				} else {
					DomU dom = (DomU)ClassicProject.g().getTrgt(tokens[0].toUpperCase());
					register = dom.get(Integer.parseInt(tokens[1]));
				}

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

				// link the method
				String sourceFileName = method == null ? "" : SourceInfo.filePath(method.getDeclaringClass());
				int methodLineNum = SourceInfo.methodLineNum(method);

				String methStr = sourceFileName + " " + methodLineNum + " " +method.getName() + " ";
				//tokens[0] = "<a onclick=\"showSource('" + sourceFileName + "','false','" + methodLineNum + "')\">" + "[" + method.getName() + "]</a> "/* + tokens[0]*/;

				// link the register
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

				if(registerLineNum != null) {
					newTokens.set(1,sourceFileName + " " + registerLineNum + " ");
					newTokens.set(0, methStr);
				}
				else{
					newTokens.set(0, methStr);
					newTokens.set(1, "");
				}
			}
			/*
	      if(tokens[0].equals("u")) {
	      DomU dom = (DomU)ClassicProject.g().getTrgt(tokens[0].toUpperCase());

	      RegisterMap regMap = getRegisterMap(method);
	      Register register = dom.get(Integer.parseInt(tokens[1]));
	      jq_Method method = dom.getMethod(register);

	      String sourceFileName = method.getDeclaringClass().getSourceFileName();
	      int lineNum = SourceInfo.methodLineNum(method);

	      tokens[0] = "<a onclick=\"showSource('" + sourceFileName + "','false','" + lineNum + "')\">" + "[" + method.getName() + "]</a> " + tokens[0];
	      }
			 */

			// if the context exists, get the context
			if(tokens.length == 3) {
				DomC domC = (DomC)ClassicProject.g().getTrgt("C");
				Ctxt c = domC.get(Integer.parseInt(tokens[2]));
				Unit[] elems = c.getElems();
				for (int i = 0; i < elems.length; ++i) {
					newTokens.add(SourceInfo.containerMethod((Stmt)elems[i]).getName());
				}
			}
			return newTokens.toArray(tokens);
		} catch(Exception e) {
			String[] tokens = new String[1];
			tokens[0] = node;
			return tokens;
		}
	}

	public static String parseNode(String node) {
		String[] tokens = toTokens(node);
		if (tokens.length <= 2) return "";
		StringBuilder sb = new StringBuilder();
		sb.append(tokens[0]);
		sb.append(tokens[1]);
		boolean runSeen = false;
		for (int i = tokens.length - 1; i > 2; --i) {
			sb.append(tokens[i]);
			if (i != 3) sb.append(".");
			if (tokens[i].equals("run")) {
				if (!runSeen) {
				 runSeen = true;
				} else {
				 break;
				}
			}
		}
		return sb.toString() + '\n';
	}

	public static class StubInfo {
		public final String relationName;

		public final SootMethod method;

		public final Integer firstArg;
		public final Integer secondArg;

		public StubInfo(String relationName, int methodId, Integer firstArg, Integer secondArg) {
			this.relationName = relationName;

			DomM dom = (DomM)ClassicProject.g().getTrgt("M");
			this.method = dom.get(methodId);

			this.firstArg = firstArg;
			this.secondArg = secondArg;
		}

		public StubInfo(String relationName, int methodId, Integer arg) {
			this(relationName, methodId, arg, null);
		}

		public StubInfo(String relationName, int methodId) {
			this(relationName, methodId, null, null);
		}

		public StubInfo(StubLookupValue value) {
			this(value.relationName, value.method, value.firstArg, value.secondArg);
		}

		@Override public String toString() {
			return this.relationName + ":" + this.method.toString() + "[" + this.firstArg + "][" + this.secondArg + "]";
		}
	}

	public static StubInfo lookup(EdgeData edge, boolean forward) {
		String symbol = edge.symbol;
		String source = edge.from;
		String sink = edge.to;
		StubLookupKey key = new StubLookupKey(symbol, source, sink);
		StubLookupValue value = JCFLSolverAnalysis.getStub(key);

		return value == null ? null : new StubInfo(value);
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
			StubLookupValue value = entry.getValue();
			StubInfo info = new StubInfo(value);
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
			StubInfo info = new StubInfo(entry.getValue());
			pw.println(key.symbol + "," + key.source + "," + key.sink + " " + info.toString());
		}
		pw.close();
	}

	public static void printStubInputs(Graph g, File outputDir) throws IOException {
		PrintWriter pw = new PrintWriter(new File("cfl/Src2SinkStubs.out"));
		Counter<String> keys = new Counter<String>();

		for(Edge edge : g.getEdges("Src2Sink")) {
			pw.println(edge.from.getName() + " -> " + edge.to.getName());

			String[] sourceTokens = toTokens(edge.from.getName());
			String source = sourceTokens[1];

			String[] sinkTokens = toTokens(edge.to.getName());
			String sink = sinkTokens[1];
			pw.println(source + " -> " + sink);

			List<Pair<Edge,Boolean>> path = g.getPath(edge);
			for(Pair<Edge,Boolean> pair : path) {
				EdgeData data = pair.getX().getData(g);
				boolean forward = pair.getY();

				if(data.weight > 0) {
					StubInfo info = lookup(data, forward);
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
				StubInfo sourceInfo = lookup(sourceData, source.getY());

				Pair<Edge,Boolean> sink = path.get(path.size()-1);
				EdgeData sinkData = sink.getX().getData(g);
				StubInfo sinkInfo = lookup(sinkData, sink.getY());

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

		StubInfo info = lookup(edge, forward);
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

	public static class SwitchHTMLObject {
		private HTMLObject obj1;
		private HTMLObject obj2;

		private String buttonHTML1;
		private String buttonHTML2;

		public SwitchHTMLObject(HTMLObject obj1, HTMLObject obj2, String buttonHTML1, String buttonHTML2) {
			this.obj1 = obj1;
			this.obj2 = obj2;

			this.buttonHTML1 = buttonHTML1;
			this.buttonHTML2 = buttonHTML2;
		}

		public HTMLObject getObj1() {
			return this.obj1;
		}

		public HTMLObject getObj2() {
			this.obj2.putStyle("display", "none");
			return this.obj2;
		}

		public ButtonObject getButton() {
			ButtonObject button = new ButtonObject(buttonHTML2);
			button.putOnClick("switchHTMLObject(this,'" + buttonHTML1 + "','" + buttonHTML2 + "','" + obj1.getId() + "','" + obj2.getId() + "')");
			return button;
		}

		public void switchObjs() {
			HTMLObject tempObj = this.obj2;
			this.obj2 = this.obj1;
			this.obj1 = tempObj;

			String tempString = this.buttonHTML2;
			this.buttonHTML2 = this.buttonHTML1;
			this.buttonHTML1 = this.buttonHTML2;
		}
	}

	public static abstract class FlowObject extends DivObject {
		private int index;
		private Graph g;
		private List<Pair<Edge,Boolean>> path;
		private boolean subpath;

		public FlowObject(List<Pair<Edge,Boolean>> path, Graph g) {
			this(path, g, false);
		}

		public FlowObject(List<Pair<Edge,Boolean>> path, Graph g, boolean subpath) {
			this.path = path;
			this.g = g;

			if(this.path.size() == 0) return;

			this.index = -this.countUnmatched();
			this.subpath = subpath;

			if(!this.subpath) {
				this.index--;
				this.putStyle("overflow", "scroll");
				this.putStyle("height", "720px");
				this.putStyle("width", "100%");
			}

			this.addObject(this.process());
		}

		public abstract boolean isStart(int i);
		public abstract boolean isEnd(int i);
		public abstract HTMLObject getAlternateObject(int startIndex, int endIndex);
		public abstract String getMainLabel();
		public abstract String getAlternateLabel();

		public boolean isSubpath() {
			return this.subpath;
		}

		public int getSize() {
			return this.path.size();
		}

		public Pair<Edge,Boolean> getEdge(int i) {
			return this.path.get(i);
		}

		public Graph getGraph() {
			return this.g;
		}

		private int countUnmatched() {
			int pre = 0;
			int counter = 0;
			for(int i=0; i<this.path.size(); i++) {
				if(this.isStart(i)) {
					counter++;
				} else if(this.isEnd(i)) {
					if(counter == 0) {
						pre++;
					} else {
						counter--;
					}
				}
			}
			return pre;
		}

		public HTMLObject process() {
			return this.process(true);
		}

		public boolean useAlternate(int startIndex, int endIndex) {
			return this.isStart(startIndex) && this.isEnd(endIndex);
		}

		public HTMLObject process(boolean start) {

			DivObject d = new DivObject();
			d.putStyle("margin-left", "10px");

			while(this.index < path.size()) {
				if(this.index < 0 || (isStart(index) && !start)) {
					// start a new div

					int startIndex = this.index;
					if(this.index < 0) {
						this.index++;
					}

					HTMLObject subdiv = process(true);
					int endIndex = this.index;

					SwitchHTMLObject s = this.useAlternate(startIndex, endIndex) ?
							new SwitchHTMLObject(getAlternateObject(startIndex, endIndex), subdiv, this.getMainLabel(), this.getAlternateLabel())
					: new SwitchHTMLObject(subdiv, getAlternateObject(startIndex, endIndex), this.getAlternateLabel(), this.getMainLabel());

							d.removeLastObject();
							d.addObject(s.getButton());
							d.addObject(s.getObj1());
							d.addObject(s.getObj2());

							if(index < path.size()) {
								Pair<Edge,Boolean> pair = this.path.get(index);
								Edge edge = pair.getX();
								boolean forward = pair.getY();

								if(!subpath || index < path.size()-1) {
									d.addObject(new SpanObject(parseNode(edge.getData(this.g).getTo(forward))));
									d.addBreak();
								}
								this.index++;
							}

							start = false;
				}  else {
					// add the next pair
					Pair<Edge,Boolean> pair = this.path.get(index);
					Edge edge = pair.getX();
					boolean forward = pair.getY();

					if(this.index == 0 && !subpath) {
						d.addObject(new SpanObject(parseNode(edge.getData(this.g).getFrom(forward))));
						d.addBreak();
					}

					SpanObject span = new SpanObject("&nbsp;| " + parseEdge(edge.getData(this.g), forward));
					if(edge.weight > 0) {
						span.putStyle("color", "red");
					}
					d.addObject(span);
					d.addBreak();

					if(isEnd(index)) {
						return d;
					}

					if(!subpath || index < path.size()-1) {
						d.addObject(new SpanObject(parseNode(edge.getData(this.g).getTo(forward))));
						d.addBreak();
					}

					this.index++;

					start = false;
				}
			}
			return d;
		}

		public String getString(boolean start) {
			this.index = 0;
			StringBuilder sb = new StringBuilder();
			while(this.index < path.size()) {
				if(this.index < 0) {
					this.index++;
				}

				Pair<Edge,Boolean> pair = this.path.get(index);
				Edge edge = pair.getX();
				boolean forward = pair.getY();

				if(this.index == 0 && !subpath && !start) {
					sb.append(parseNode(edge.getData(this.g).getFrom(forward)));
				}

				//SpanObject span = new SpanObject(parseEdge(edge.getData(this.g), forward));
				//if(edge.weight > 0) {
				//	span.putStyle("color", "red");
				//}
				//d.addObject(span);
				//d.addBreak();

				if(isEnd(index)) {
					return sb.toString();
				}

				if(!subpath || index < path.size()-1) {
					sb.append(parseNode(edge.getData(this.g).getTo(forward)));
				}

				this.index++;

				start = false;
			}
			return sb.toString();
		}
	}

	public static class MethodCompressedFlowObject extends FlowObject {
		private static Set<String> startSymbols = new HashSet<String>();
		private static Set<String> endSymbols = new HashSet<String>();
		static {
			startSymbols.add("cs_refAssignArg");
			endSymbols.add("cs_refAssignRet");
		}

		public MethodCompressedFlowObject(List<Pair<Edge,Boolean>> path, Graph g, boolean subpath) {
			super(path, g, subpath);
		}

		public MethodCompressedFlowObject(List<Pair<Edge,Boolean>> path, Graph g) {
			super(path, g);
		}

		@Override public String getMainLabel() {
			return "Compress";
		}

		@Override public String getAlternateLabel() {
			return "Expand";
		}

		@Override public boolean isStart(int i) {
			if(i < 0 || i >= super.getSize()) return false;
			Graph g = super.getGraph();
			Pair<Edge,Boolean> pair = super.getEdge(i);
			return (pair.getY() && startSymbols.contains(pair.getX().getData(g).symbol))
					|| (!pair.getY() && endSymbols.contains(pair.getX().getData(g).symbol));
		}

		@Override public boolean isEnd(int i) {
			if(i < 0 || i >= super.getSize()) return false;
			Graph g = super.getGraph();
			Pair<Edge,Boolean> pair = super.getEdge(i);
			return (!pair.getY() && this.startSymbols.contains(pair.getX().getData(g).symbol))
					|| (pair.getY() && this.endSymbols.contains(pair.getX().getData(g).symbol));
		}

		@Override public HTMLObject getAlternateObject(int startIndex, int endIndex) {
			DivObject d = new DivObject();
			d.addObject(new SpanObject("&nbsp;| PassThrough"));
			d.putStyle("margin-left", "10px");
			return d;
		}
	}

	public static class AliasCompressedFlowObject extends FlowObject {
		private static Set<String> symbols = new HashSet<String>();

		static {
			symbols.add("cs_refAssign");
			symbols.add("cs_refAssignArg");
			symbols.add("cs_refAssignRet");
			symbols.add("cs_refAlloc");
			symbols.add("cs_refLoad");
			symbols.add("cs_refStore");
			symbols.add("cs_primStore");
			symbols.add("cs_primLoad");
			symbols.add("cs_primAssign");
			symbols.add("cs_primAssignArg");
			symbols.add("cs_primAssignRet");
		}

		public AliasCompressedFlowObject(List<Pair<Edge,Boolean>> path, Graph g, boolean subpath) {
			super(path, g, subpath);
		}

		public AliasCompressedFlowObject(List<Pair<Edge,Boolean>> path, Graph g) {
			super(path, g);
		}

		@Override public boolean useAlternate(int startIndex, int endIndex) {
			return false;
		}

		@Override public String getMainLabel() {
			return "+";//"View Flow Path";
		}

		@Override public String getAlternateLabel() {
			return "-";//"View Taint Path";
		}

		@Override public boolean isStart(int i) {
			/*
	    if(i <= 0 || i >= super.getSize()) return false;
	    return super.getEdge(i-1).getY() && !super.getEdge(i).getY();
			 */
			if(i < 0 || i >= super.getSize()) return false;
			return super.getEdge(i).getX().getData(super.getGraph()).symbol.equals("FlowsTo") && !super.getEdge(i).getY();
		}

		@Override public boolean isEnd(int i) {
			/*
	    if(i < 0 || i >= super.getSize()-1) return false;
	    return !symbols.contains(super.getEdge(i+1).getX().getData(super.getGraph()).symbol);
			 */
			if(i < 0 || i >= super.getSize()) return false;
			return super.getEdge(i).getX().getData(super.getGraph()).symbol.equals("FlowsTo") && super.getEdge(i).getY();
		}

		@Override public HTMLObject getAlternateObject(int startIndex, int endIndex) {
			if(startIndex < 0 || endIndex >= super.getSize()) {
				DivObject d = new DivObject();
				d.addObject(new SpanObject("&nbsp;| Alias"));
				d.putStyle("margin-left", "10px");
				return d;
			} else {
				List<Pair<Edge,Boolean>> aliasList = new ArrayList<Pair<Edge,Boolean>>();
				for(int i=startIndex; i<=endIndex; i++) {
					aliasList.addAll(super.getGraph().getPath(super.getEdge(i).getX(), super.getEdge(i).getY()));
				}
				return new AliasCompressedFlowObject(aliasList, super.getGraph(), true);
			}
		}
	}

	public static void viz(Graph g, File outputDir) throws IOException {
		Set<String> terminals = new HashSet<String>();
		terminals.add("FlowsTo");

		for(Edge edge : g.getEdges("Src2Sink")) {
			String[] sourceTokens = toTokens(edge.from.getName());
			String source = sourceTokens[1].substring(1);

			String[] sinkTokens = toTokens(edge.to.getName());
			String sink = sinkTokens[1].substring(1);

			PrintWriter pw = new PrintWriter(new File("cfl/" + source + "2" + sink + ".out.test"));
			FlowObject fo = new MethodCompressedFlowObject(g.getPath(edge), g);
			pw.println(new HTMLObject.SpanObject(fo.getString(true)).toString());
			//pw.println(new AliasCompressedFlowObject(g.getPath(edge, terminals), g).toString());
			pw.close();
		}
	}
}
