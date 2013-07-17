package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.grammars.E12;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.Relation.IndexRelation;
import stamp.missingmodels.util.Relation.StubIndexRelation;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubLookup.StubLookupKey;
import stamp.missingmodels.util.StubLookup.StubLookupValue;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.FactsWriter;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.viz.flow.FlowWriter;
import chord.project.Chord;

/*
 * An analysis that runs the JCFLSolver to do the taint analysis.
 * 
 * DOMAIN INFORMATION:
 * 
 * a) Shord domains:
 * variables = V.dom = Register
 * methods = M.dom = jq_Method
 * sources/sinks = SRC.dom/SINK.dom = String
 * contexts = C.dom = Ctxt
 * invocation quads = I.dom = Quad
 * method arg number = Z.dom = Integer
 * integers = K.dom = Integer
 * 
 * b) Stamp domains:
 * labels (sources + sinks) = L.dom = String
 */

@Chord(name = "jcflsolver")
public class JCFLSolverAnalysis extends JavaAnalysis {

	/*
	 * The set of all relations used by the various grammars.
	 */
	private static final MultivalueMap<String,Relation> relations = new MultivalueMap<String,Relation>();
	static {
		/*
		 * The following are points-to analysis information.
		 */
		// ref assign
		relations.add("cs_refAssign", new IndexRelation("AssignCtxt", "V", 2, 0, "V", 1, 0));
		relations.add("cs_refAssign", new IndexRelation("LoadStatCtxt", "F", 2, null, "V", 1, 0));
		relations.add("cs_refAssign", new IndexRelation("StoreStatCtxt", "V", 2, 0, "F", 1, null));

		relations.add("cs_refAssignArg", new IndexRelation("AssignArgCtxt", "V", 3, 2, "V", 1, 0));
		relations.add("cs_refAssignRet", new IndexRelation("AssignRetCtxt", "V", 3, 2, "V", 1, 0));

		// ref alloc
		relations.add("cs_refAlloc", new IndexRelation("AllocCtxt", "C", 2, null, "V", 1, 0));

		// ref load/store
		relations.add("cs_refLoad", new IndexRelation("LoadCtxt", "V", 2, 0, "V", 1, 0, 3));
		relations.add("cs_refStore", new IndexRelation("StoreCtxt", "V", 3, 0, "V", 1, 0, 2));

		// cross load/store
		relations.add("cs_primStore", new IndexRelation("StorePrimCtxt", "U", 3, 0, "V", 1, 0, 2));
		relations.add("cs_primLoad", new IndexRelation("LoadPrimCtxt", "V", 2, 0, "U", 1, 0, 3));

		// prim assign
		relations.add("cs_primAssign", new IndexRelation("AssignPrimCtxt", "U", 2, 0, "U", 1, 0));
		relations.add("cs_primAssign", new IndexRelation("LoadStatPrimCtxt", "F", 2, null, "U", 1, 0));
		relations.add("cs_primAssign", new IndexRelation("StoreStatPrimCtxt", "U", 2, 0, "F", 1, null));

		relations.add("cs_primAssignArg", new IndexRelation("AssignArgPrimCtxt", "U", 3, 2, "U", 1, 0));
		relations.add("cs_primAssignRet", new IndexRelation("AssignRetPrimCtxt", "U", 3, 2, "U", 1, 0));

		/*
		 * The following is the points-to relation, computed by BDDBDDB.
		 */
		relations.add("flowsTo", new IndexRelation("pt", "C", 2, null, "V", 1, 0));
		
		/*
		 * The following are taint information.
		 */

		// ref taint flow
		relations.add("cs_srcRefFlow", new IndexRelation("SrcArgFlowCtxt", "L", 1, null, "V", 2, 0));
		relations.add("cs_srcRefFlow", new IndexRelation("SrcRetFlowCtxt", "L", 1, null, "V", 2, 0));
		relations.add("cs_refSinkFlow", new IndexRelation("ArgSinkFlowCtxt", "V", 1, 0, "L", 2, null));
		relations.add("cs_refRefFlow", new IndexRelation("ArgArgTransferCtxt", "V", 1, 0, "V", 2, 0));
		relations.add("cs_refRefFlow", new IndexRelation("ArgRetTransferCtxt", "V", 1, 0, "V", 2, 0));

		// prim taint flow
		relations.add("cs_srcPrimFlow", new IndexRelation("SrcRetPrimFlowCtxt", "L", 1, null, "U", 2, 0));
		relations.add("cs_primSinkFlow", new IndexRelation("ArgSinkPrimFlowCtxt", "U", 1, 0, "L", 2, null));
		relations.add("cs_primPrimFlow", new IndexRelation("ArgPrimRetPrimTransferCtxt", "U", 1, 0, "U", 2, 0));

		// cross taint flow
		relations.add("cs_primRefFlow", new IndexRelation("ArgPrimArgTransferCtxt", "U", 1, 0, "V", 2, 0));
		relations.add("cs_primRefFlow", new IndexRelation("ArgPrimRetTransferCtxt", "U", 1, 0, "V", 2, 0));
		relations.add("cs_refPrimFlow", new IndexRelation("ArgRetPrimTransferCtxt", "V", 1, 0, "U", 2, 0));

		// ref stub taint flow
		relations.add("cs_passThroughStub", new StubIndexRelation("ArgArgTransferCtxtStub", "V", 1, 0, "V", 2, 0, 3, 4, 5));
		relations.add("cs_passThroughStub", new StubIndexRelation("ArgRetTransferCtxtStub", "V", 1, 0, "V", 2, 0, 3, 4));

		// prim stub taint flow
		relations.add("cs_primPassThroughStub", new StubIndexRelation("ArgPrimRetPrimTransferCtxtStub", "U", 1, 0, "U", 2, 0, 3, 4));
		relations.add("cs_refPrimFlowStub", new StubIndexRelation("ArgRetPrimTransferCtxtStub", "V", 1, 0, "U", 2, 0, 3, 4));

		// cross stub taint flow
		relations.add("cs_primRefFlowStub", new StubIndexRelation("ArgPrimArgTransferCtxtStub", "U", 1, 0, "V", 2, 0, 3, 4, 5));
		relations.add("cs_primRefFlowStub", new StubIndexRelation("ArgPrimRetTransferCtxtStub", "U", 1, 0, "V", 2, 0, 3, 4));

		/*
		 * The following are phantom point-to relations.
		 */
		//relations.add("flowsTo", new PhantomIndexRelation("cs_ptPhantom", 2, "V", 1, 0, "V", 3, 4));

		/*
		 * The following are for source/sink inference purposes.
		 */

		/*
		// ref stub source/sink taint flow
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
	}

	/*
	 * The following code is for building and performing stub lookups.
	 */
	private static StubLookup stubLookup = new StubLookup();
	public static StubLookupValue getStub(StubLookupKey key) {
		return stubLookup.get(key);
	}

	public static Map<StubLookupKey,StubLookupValue> getAllStubs() {
		return stubLookup;
	}

	public static StubLookupValue lookup(EdgeData edge, boolean forward) {
		StubLookupKey key = new StubLookupKey(edge.symbol, edge.from, edge.to);
		return JCFLSolverAnalysis.getStub(key);
	}

	/*
	 * The following code is for running the JCFLSolver analysis.
	 */
	private void fillTerminalEdges(Graph g) {
		for(int k=0; k<g.numKinds(); k++) {
			if(g.isTerminal(k)) {
				if(relations.get(g.kindToSymbol(k)).isEmpty()) {
					System.out.println("No edges found for relation " + g.kindToSymbol(k) + "...");
				}
				for(Relation rel : relations.get(g.kindToSymbol(k))) {
					rel.addEdges(g.kindToSymbol(k), g, stubLookup);
				}
			}
		}
	}
	
	@Override public void run() {
		Graph g = new E12();
		fillTerminalEdges(g);
		g.algo.process();

		File resultsDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");
		resultsDir.mkdirs();
		try {
			FactsWriter.write(g, resultsDir, true);
			FlowWriter.printStubInputs(g, resultsDir);
			FlowWriter.printAllStubs();
			FlowWriter.viz(g, resultsDir);
		} catch(IOException e) { e.printStackTrace(); }
	}
}
