package stamp.analyses;

import java.io.*;
import java.util.*;

import chord.project.Chord;

import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.JavaAnalysis;

import stamp.jcflsolver.*;
import stamp.jcflsolver.Util.*;
import stamp.jcflsolver.grammars.*;

@Chord(name = "jcflsolver") public class JCFLSolverAnalysis extends JavaAnalysis {

    //*** CODE FOR RELATION TO EDGE SYMBOL LOOKUPS ***//
    private static final MultivalueMap<String,Relation> relations = new MultivalueMap<String,Relation>();
    static {
	// ref assign
	relations.add("cs_refAssign", new IndexRelation("cfl_cs_assign", "V", 2, 0, "V", 1, 0));
	relations.add("cs_refAssign", new IndexRelation("cfl_cs_loadStat", "F", 2, null, "V", 1, 0));
	relations.add("cs_refAssign", new IndexRelation("cfl_cs_storeStat", "V", 2, 0, "F", 1, null));

	relations.add("cs_refAssignArg", new IndexRelation("cfl_cs_assignInterprocArg", "V", 3, 2, "V", 1, 0));
	relations.add("cs_refAssignRet", new IndexRelation("cfl_cs_assignInterprocRet", "V", 3, 2, "V", 1, 0));

	// ref alloc
	relations.add("cs_refAlloc", new IndexRelation("cfl_cs_alloc", "C", 2, null, "V", 1, 0));

	// ref load/store
	//relations.add("cs_refLoad", new IndexRelation("cfl_cs_loadInst", "V", 2, 0, "V", 1, 0, 3));
	//relations.add("cs_refStore", new IndexRelation("cfl_cs_storeInst", "V", 3, 0, "V", 1, 0, 2));

	// cross load/store
	relations.add("cs_primStore", new IndexRelation("cfl_cs_primStoreInst", "U", 3, 0, "V", 1, 0, 2));
	relations.add("cs_primLoad", new IndexRelation("cfl_cs_primLoadInst", "V", 2, 0, "U", 1, 0, 3));

	// prim assign
	relations.add("cs_primAssign", new IndexRelation("cfl_cs_primAssign", "U", 2, 0, "U", 1, 0));
	relations.add("cs_primAssign", new IndexRelation("cfl_cs_primLoadStat", "F", 2, null, "U", 1, 0));
	relations.add("cs_primAssign", new IndexRelation("cfl_cs_primStoreStat", "U", 2, 0, "F", 1, null));

	relations.add("cs_primAssignArg", new IndexRelation("cfl_cs_primAssignInterprocArg", "U", 3, 2, "U", 1, 0));
	relations.add("cs_primAssignRet", new IndexRelation("cfl_cs_primAssignInterprocRet", "U", 3, 2, "U", 1, 0));

	// ref taint flow
	relations.add("cs_srcRefFlow", new IndexRelation("cfl_cs_fullSrcFlow", "SRC", 1, null, "V", 2, 0));
	relations.add("cs_refRefFlow", new IndexRelation("cfl_cs_fullPassThrough", "V", 1, 0, "V", 2, 0));
	relations.add("cs_refSinkFlow", new IndexRelation("cfl_cs_fullSinkFlow", "V", 1, 0, "SINK", 2, null));

	// prim taint flow
	relations.add("cs_srcPrimFlow", new IndexRelation("cfl_cs_primFullSrcFlow", "SRC", 1, null, "U", 2, 0));
	relations.add("cs_primPrimFlow", new IndexRelation("cfl_cs_primFullPassThrough", "U", 1, 0, "U", 2, 0));
	relations.add("cs_primSinkFlow", new IndexRelation("cfl_cs_primFullSinkFlow", "U", 1, 0, "SINK", 2, null));

	// cross taint flow
	relations.add("cs_primRefFlow", new IndexRelation("cfl_cs_primRefFullFlow", "U", 1, 0, "V", 2, 0));
	relations.add("cs_refPrimFlow", new IndexRelation("cfl_cs_refPrimFullFlow", "V", 1, 0, "U", 2, 0));

	// ref stub taint flow
	relations.add("cs_passThroughStub", new StubIndexRelation("cfl_cs_passThroughArgStub", "V", 1, 0, "V", 2, 0, 3, 4, 5));
	relations.add("cs_passThroughStub", new StubIndexRelation("cfl_cs_passThroughRetStub", "V", 1, 0, "V", 2, 0, 3, 4));

	// prim stub taint flow
	relations.add("cs_primPassThroughStub", new StubIndexRelation("cfl_cs_primPassThroughStub", "U", 1, 0, "U", 2, 0, 3, 4));
	relations.add("cs_refPrimFlowStub", new StubIndexRelation("cfl_cs_refPrimFlowStub", "V", 1, 0, "U", 2, 0, 3, 4));

	// cross stub taint flow
	relations.add("cs_primRefFlowStub", new StubIndexRelation("cfl_cs_primRefArgFlowStub", "U", 1, 0, "V", 2, 0, 3, 4, 5));
	relations.add("cs_primRefFlowStub", new StubIndexRelation("cfl_cs_primRefRetFlowStub", "U", 1, 0, "V", 2, 0, 3, 4));

	// Flows To relation
	relations.add("flowsTo", new IndexRelation("pt", "C", 2, null, "V", 1, 0));
	
	// Phantom flows to relation
	//relations.add("flowsTo", new PhantomIndexRelation("cs_ptPhantom", 2, "V", 1, 0, "V", 3, 4));

	//*** FOR SOURCE/SINK INFERENCE PURPOSES ONLY ***//

	// ref stub source/sink taint flow
	/*
	relations.add("cs_srcFlowStub", new StubIndexRelation("cfl_cs_srcArgFlowStub", "M", 2, 3, "V", 1, 0, 2, 3));
	relations.add("cs_srcFlowStub", new StubIndexRelation("cfl_cs_srcRetFlowStub", "M", 2, null, "V", 1, 0, 2));

	relations.add("cs_sinkFlowStub", new StubIndexRelation("cfl_cs_sinkFlowStub", "V", 1, 0, "M", 2, 3, 2, 3));

	// prim stub source/sink taint flow
	relations.add("cs_primSrcFlowStub", new StubIndexRelation("cfl_cs_primSrcFlowStub", "M", 2, null, "U", 1, 0, 2));
	relations.add("cs_primSinkFlowStub", new StubIndexRelation("cfl_cs_primSinkFlowStub", "U", 1, 0, "M", 2, 3, 2, 3));

	// ref source/sink taint flow
	relations.add("cs_srcRefFlowNew", new StubIndexRelation("cfl_cs_fullSrcArgFlow_new", "M", 3, 4, "V", 2, 0, 3, 4));
	relations.add("cs_srcRefFlowNew", new StubIndexRelation("cfl_cs_fullSrcRetFlow_new", "M", 3, null, "V", 2, 0, 3));

	relations.add("cs_refSinkFlowNew", new StubIndexRelation("cfl_cs_fullSinkFlow_new", "V", 1, 0, "M", 3, 4, 3, 4));

	// prim source/sink taint flow
	relations.add("cs_srcPrimFlowNew", new StubIndexRelation("cfl_cs_primFullSrcFlow_new", "M", 3, null, "U", 2, 0, 3));
	relations.add("cs_primSinkFlowNew", new StubIndexRelation("cfl_cs_primFullSinkFlow_new", "U", 1, 0, "M", 3, 4, 3, 4));
	*/

	//*** END ***//
    }

    public static class IndexRelation extends Relation {
	protected final int firstVarIndex;
	protected final int firstCtxtIndex;

	protected final int secondVarIndex;
	protected final int secondCtxtIndex;

	protected final int labelIndex;

	protected final String firstVarType;
	protected final String secondVarType;

	protected final String relationName;

	protected final boolean hasFirstCtxt;
	protected final boolean hasSecondCtxt;
	protected final boolean hasLabel;

	public IndexRelation(String relationName, String firstVarType, int firstVarIndex, Integer firstCtxtIndex, String secondVarType, int secondVarIndex, Integer secondCtxtIndex, Integer labelIndex) {
	    this.relationName = relationName;

	    this.hasFirstCtxt = firstCtxtIndex != null;
	    this.hasSecondCtxt = secondCtxtIndex != null;
	    this.hasLabel = labelIndex != null;

	    this.firstVarIndex = firstVarIndex;
	    this.firstCtxtIndex = this.hasFirstCtxt ? firstCtxtIndex : 0;

	    this.secondVarIndex = secondVarIndex;
	    this.secondCtxtIndex = this.hasSecondCtxt ? secondCtxtIndex : 0;

	    this.labelIndex = this.hasLabel ? labelIndex : 0;

	    this.firstVarType = firstVarType;
	    this.secondVarType = secondVarType;
	}

	public IndexRelation(String relationName, String firstVarType, int firstVarIndex, Integer firstCtxtIndex, String secondVarType, int secondVarIndex, Integer secondCtxtIndex) {
	    this(relationName, firstVarType, firstVarIndex, firstCtxtIndex, secondVarType, secondVarIndex, secondCtxtIndex, null);
	}

	@Override protected String getRelationName() {
	    return this.relationName;
	}

	@Override protected String getSourceFromTuple(int[] tuple) {
	    return firstVarType + Integer.toString(tuple[firstVarIndex]) + (hasFirstCtxt ? "_" + Integer.toString(tuple[firstCtxtIndex]) : "");
	}

	@Override protected String getSinkFromTuple(int[] tuple) {
	    return secondVarType + Integer.toString(tuple[secondVarIndex]) + (hasSecondCtxt ? "_" + Integer.toString(tuple[secondCtxtIndex]) : "");
	}

	@Override protected int getLabelFromTuple(int[] tuple) {
	    return tuple[labelIndex];
	}

	@Override protected boolean hasLabel() {
	    return hasLabel;
	}

	@Override protected boolean isStub() {
	    return false;
	}

	@Override protected StubLookupValue getStubLookupValueFromTuple(int[] tuple) {
	    return null;
	}

	@Override protected boolean filter(int[] tuple) {
	    return true;
	}
    }

    public static class PhantomIndexRelation extends IndexRelation {
	private final int methodIndex;

	public PhantomIndexRelation(String relationName, int methodIndex, String firstVarType, int firstVarIndex, int firstCtxtIndex, String secondVarType, int secondVarIndex, Integer secondCtxtIndex) {
	    super(relationName, firstVarType, firstVarIndex, firstCtxtIndex, secondVarType, secondVarIndex, secondCtxtIndex, null);
	    this.methodIndex = methodIndex;
	}

	@Override protected String getSourceFromTuple(int[] tuple) {
	    String source = super.getSourceFromTuple(tuple);
	    return "M" + tuple[this.methodIndex] + "_" + source;
	}
    }

    public static class StubIndexRelation extends IndexRelation {
	private int methodIndex;
	private Integer firstArgIndex;
	private Integer secondArgIndex;

	public StubIndexRelation(String relationName, String firstVarType, int firstVarIndex, Integer firstCtxtIndex, String secondVarType, int secondVarIndex, Integer secondCtxtIndex, Integer labelIndex, int methodIndex, Integer firstArgIndex, Integer secondArgIndex) {
	    super(relationName, firstVarType, firstVarIndex, firstCtxtIndex, secondVarType, secondVarIndex, secondCtxtIndex, labelIndex);
	    this.methodIndex = methodIndex;
	    this.firstArgIndex = firstArgIndex;
	    this.secondArgIndex = secondArgIndex;
	}

	public StubIndexRelation(String relationName, String firstVarType, int firstVarIndex, Integer firstCtxtIndex, String secondVarType, int secondVarIndex, Integer secondCtxtIndex, int methodIndex, Integer firstArgIndex, Integer secondArgIndex) {
	    this(relationName, firstVarType, firstVarIndex, firstCtxtIndex, secondVarType, secondVarIndex, secondCtxtIndex, null, methodIndex, firstArgIndex, secondArgIndex);
	}

	public StubIndexRelation(String relationName, String firstVarType, int firstVarIndex, Integer firstCtxtIndex, String secondVarType, int secondVarIndex, Integer secondCtxtIndex, int methodIndex, Integer argIndex) {
	    this(relationName, firstVarType, firstVarIndex, firstCtxtIndex, secondVarType, secondVarIndex, secondCtxtIndex, null, methodIndex, argIndex, null);
	}

	public StubIndexRelation(String relationName, String firstVarType, int firstVarIndex, Integer firstCtxtIndex, String secondVarType, int secondVarIndex, Integer secondCtxtIndex, int methodIndex) {
	    this(relationName, firstVarType, firstVarIndex, firstCtxtIndex, secondVarType, secondVarIndex, secondCtxtIndex, null, methodIndex, null, null);
	}

	@Override protected boolean isStub() {
	    return true;
	}

	@Override protected StubLookupValue getStubLookupValueFromTuple(int[] tuple) {
	    Integer firstArg = this.firstArgIndex == null ? null : tuple[this.firstArgIndex];
	    Integer secondArg = this.secondArgIndex == null ? null : tuple[this.secondArgIndex];
	    return new StubLookupValue(this.getRelationName(), tuple[this.methodIndex], firstArg, secondArg);
	}

	@Override protected boolean filter(int[] tuple) {
	    return true;
	}
    }

    public static abstract class Relation {
	protected abstract String getRelationName();

	protected abstract String getSourceFromTuple(int[] tuple);
	protected abstract String getSinkFromTuple(int[] tuple);

	protected abstract boolean hasLabel();
	protected abstract int getLabelFromTuple(int[] tuple);

	protected abstract boolean isStub();
	protected abstract StubLookupValue getStubLookupValueFromTuple(int[] tuple);
	protected abstract boolean filter(int[] tuple);

	protected StubLookupKey getStubLookupKeyFromTuple(int[] tuple, String edgeName) {
	    return new StubLookupKey(edgeName, this.getSourceFromTuple(tuple), this.getSinkFromTuple(tuple));
	}

	public void addEdges(String edgeName, Graph g) {
	    int kind = g.symbolToKind(edgeName);
	    short weight = g.kindToWeight(kind);

	    final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(getRelationName());
	    rel.load();
	    Iterable<int[]> res = rel.getAryNIntTuples();

	    for(int[] tuple : res) {
		if(this.filter(tuple)) {
		    String sourceName = getSourceFromTuple(tuple);
		    String sinkName = getSinkFromTuple(tuple);

		    if(hasLabel()) {
			g.addWeightedInputEdge(sourceName, sinkName, kind, getLabelFromTuple(tuple), weight);
		    } else {
			g.addWeightedInputEdge(sourceName, sinkName, kind, weight);
		    }

		    if(isStub()) {
			stubInfoLookup.put(this.getStubLookupKeyFromTuple(tuple, edgeName), this.getStubLookupValueFromTuple(tuple));
		    }
		}
	    }

	    rel.close();
	}
    }

    //*** CODE FOR STUB METHOD LOOKUPS ***//
    public static class StubLookupKey {
	public final String symbol;
	public final String source;
	public final String sink;

	public StubLookupKey(String symbol, String source, String sink) {
	    this.symbol = symbol;
	    this.source = source;
	    this.sink = sink;
	}

	@Override public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + symbol.hashCode();
	    result = prime * result + source.hashCode();
	    result = prime * result + sink.hashCode();
	    return result;
	}

	@Override public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    StubLookupKey other = (StubLookupKey) obj;
	    return symbol.equals(other.symbol)
		&& source.equals(other.source)
		&& sink.equals(other.sink);
	}
    }

    public static class StubLookupValue {
	public final String relationName;
	public final int method;
	public final Integer firstArg;
	public final Integer secondArg;	

	public StubLookupValue(String relationName, int method, Integer firstArg, Integer secondArg) {
	    this.relationName = relationName;
	    this.method = method;
	    this.firstArg = firstArg;
	    this.secondArg = secondArg;
	}

	public StubLookupValue(String relationName, int method, Integer arg) {
	    this(relationName, method, arg, null);
	}

	public StubLookupValue(String relationName, int method) {
	    this(relationName, method, null, null);
	}
    }

    private static Map<StubLookupKey,StubLookupValue> stubInfoLookup = new HashMap<StubLookupKey,StubLookupValue>();
    public static StubLookupValue getStub(StubLookupKey key) {
	return stubInfoLookup.get(key);
    }

    public static Map<StubLookupKey,StubLookupValue> getAllStubs() {
	return stubInfoLookup;
    }

    //*** CODE FOR SRC2SINK FLOW LOOKUPS ***//
    private static Map<Pair<Integer,Integer>,Integer> src2sink = new HashMap<Pair<Integer,Integer>,Integer>();
    public static Map<Pair<Integer,Integer>,Integer> getSrc2Sink() {
	return src2sink;
    }

    // TODO: currently only printing true source-sink flows (not inferred ones)
    public void fillSrc2Sink(Graph g) {
	for(Edge edge : g.getEdges("Src2Sink")) {
	    if(edge.from.getName().startsWith("SRC") && edge.to.getName().startsWith("SINK")) {
		int source = Integer.parseInt(edge.from.getName().replaceAll("[a-zA-Z]", ""));
		int sink = Integer.parseInt(edge.to.getName().replaceAll("[a-zA-Z]", ""));
		src2sink.put(new Pair<Integer,Integer>(source, sink), (int)edge.weight);
	    }
	}
    }

    //*** CODE FOR ANALYSIS ***/
    private void fillTerminalEdges(Graph g) {
	for(int k=0; k<g.numKinds(); k++) {
	    if(g.isTerminal(k)) {
		if(relations.get(g.kindToSymbol(k)).isEmpty()) {
		    System.out.println("No edges found for relation " + g.kindToSymbol(k) + "...");
		}
		for(Relation rel : relations.get(g.kindToSymbol(k))) {
		    rel.addEdges(g.kindToSymbol(k), g);
		}
	    }
	}
    }

    @Override public void run() {

	// ** INPUTS **
	// variables = V.dom = Register
	// methods = M.dom = jq_Method
	// sources/sinks = SRC.dom/SINK.dom = String
	// contexts = C.dom = Ctxt
	// invocation quads = I.dom = Quad
	// method arg number = Z.dom = Integer
	// integers = K.dom = Integer
	
	// sources = SRC.dom = String
	// sinks = SINK.dom = String

	Graph g = new E12();
	fillTerminalEdges(g);
	g.algo.process();

	fillSrc2Sink(g);

	File resultsDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");
	resultsDir.mkdirs();
	try {
	    FactsWriter.write(g, resultsDir, true);
	} catch(IOException e) { e.printStackTrace(); }
    }
}
