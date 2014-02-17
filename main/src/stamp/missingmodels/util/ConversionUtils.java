package stamp.missingmodels.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import stamp.analyses.DomCL;
import stamp.analyses.DomL;
import stamp.missingmodels.analysis.JCFLSolverRunner.RelationAdder;
import stamp.missingmodels.util.Relation.IndexRelation;
import stamp.missingmodels.util.Relation.StubIndexRelation;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.srcmap.Expr;
import stamp.srcmap.sourceinfo.RegisterMap;
import stamp.srcmap.sourceinfo.SourceInfo;

/*
 * This class contains code to convert to and from the
 * Shord representation and the JCFLSolver representation.
 * 
 * @author Osbert Bastani
 */
public class ConversionUtils {
	/*
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
	
	/*
	 * An implementation of the chord relation lookup interface.
	 */
	public static class ChordRelationAdder implements RelationAdder {
		/*
		 * Returns relations for which no Chord relation is found.
		 */
		@Override
		public Collection<String> addEdges(Graph g, StubLookup s, StubModelSet m) {
			Set<String> relationsNotFound = new HashSet<String>();
			for(int k=0; k<g.numKinds(); k++) {
				if(g.isTerminal(k)) {
					Collection<Relation> relations = getChordRelationsFor(g.kindToSymbol(k));
					if(relations.isEmpty()) {
						relationsNotFound.add(g.kindToSymbol(k));
					}
					for(Relation rel : relations) {
						rel.addEdges(g.kindToSymbol(k), g, s, m);
					}
				}
			}
			return relationsNotFound;
		}
	}

	/*
	 * The set of all relations used by the various grammars.
	 */	
	private static final MultivalueMap<String,Relation> relations = new MultivalueMap<String,Relation>();
	static {
		/*
		 * Taint annotations
		 */
		
		// source/sink labels
		relations.add("src2Label", new IndexRelation("Src2Label", "L", 0, null, "L", 0, null));
		relations.add("sink2Label", new IndexRelation("Sink2Label", "L", 0, null, "L", 0, null));

		// label annotations
		relations.add("label2RefT", new IndexRelation("Label2RefT", "L", 1, null, "V", 2, 0));
		relations.add("label2PrimT", new IndexRelation("Label2PrimT", "L", 1, null, "U", 2, 0));		
		
		// transfer annotations
		relations.add("ref2RefT", new IndexRelation("Ref2RefArgT", "V", 1, 0, "V", 2, 0));
		relations.add("ref2RefT", new IndexRelation("Ref2RefRetT", "V", 1, 0, "V", 2, 0));
		
		relations.add("prim2RefT", new IndexRelation("Prim2RefArgT", "U", 1, 0, "V", 2, 0));
		relations.add("prim2RefT", new IndexRelation("Prim2RefRetT", "U", 1, 0, "V", 2, 0));

		relations.add("ref2PrimT", new IndexRelation("Ref2PrimRetT", "V", 1, 0, "U", 2, 0));
		relations.add("prim2PrimT", new IndexRelation("Prim2PrimRetT", "U", 1, 0, "U", 2, 0));
		
		// sinkF annotations
		relations.add("sinkF2RefF", new IndexRelation("SinkF2RefF", "L", 1, null, "V", 2, 0));
		relations.add("sinkF2PrimF", new IndexRelation("SinkF2PrimF", "L", 1, null, "U", 2, 0));
		
		// flow annotations
		relations.add("ref2RefF", new IndexRelation("Ref2RefF", "V", 1, 0, "V", 2, 0));
		relations.add("ref2PrimF", new IndexRelation("Ref2PrimF", "V", 1, 0, "U", 2, 0));
		relations.add("prim2RefF", new IndexRelation("Prim2RefF", "U", 1, 0, "V", 2, 0));
		relations.add("prim2PrimF", new IndexRelation("Prim2PrimF", "U", 1, 0, "U", 2, 0));

		/*
		 * Stub annotations
		 */

		relations.add("ref2RefArgTStub", new StubIndexRelation("Ref2RefArgTStub", "V", 1, 0, "V", 2, 0, 3, 4, 5));
		relations.add("ref2RefRetTStub", new StubIndexRelation("Ref2RefRetTStub", "V", 1, 0, "V", 2, 0, 3, 4));
		
		relations.add("prim2RefArgTStub", new StubIndexRelation("Prim2RefArgTStub", "U", 1, 0, "V", 2, 0, 3, 4, 5));
		relations.add("prim2RefRetTStub", new StubIndexRelation("Prim2RefRetTStub", "U", 1, 0, "V", 2, 0, 3, 4));
		
		relations.add("ref2PrimTStub", new StubIndexRelation("Ref2PrimTStub", "V", 1, 0, "U", 2, 0, 3, 4));
		relations.add("prim2PrimTStub", new StubIndexRelation("Prim2PrimTStub", "U", 1, 0, "U", 2, 0, 3, 4));
		
		/*
		 * BDDBDDB pt relations
		 */
		
		// regular points
		relations.add("ptH", new IndexRelation("pt", "V", 1, 0, "C", 2, null));

		// regular fpt
		relations.add("fpt", new IndexRelation("fpt", "C", 0, null, "C", 2, null, 1));

		// fpt arr
		relations.add("fptArr", new IndexRelation("fptArr", "C", 0, null, "C", 1, null));

		// phantom points
		relations.add("ptG", new IndexRelation("phpt", "V", 1, 0, "V", 2, 3));
		relations.add("ptG", new IndexRelation("pt", "V", 1, 0, "C", 2, null));
		
		// phantom fpt
		relations.add("fpt", new IndexRelation("fptph", "C", 0, null, "V", 2, 3, 1));
		relations.add("fpt", new IndexRelation("fphpt", "V", 0, 1, "C", 3, null, 2));
		relations.add("fpt", new IndexRelation("fphptph", "V", 0, 1, "V", 3, 4, 2));
		
		/*
		 * Pointer analysis inputs
		 */
		
		// new
		relations.add("newCtxt", new IndexRelation("NewCtxt", "V", 1, 0, "C", 2, null));

		// assign
		relations.add("assignCtxt", new IndexRelation("AssignCtxt", "V", 1, 0, "V", 2, 0));
		relations.add("assignCtxt", new IndexRelation("AssignCCtxt", "V", 1, 0, "V", 3, 2, (short)1));

		// load/store
		relations.add("storeCtxt", new IndexRelation("StoreCtxt", "V", 1, 0, "V", 3, 0, 2));
		relations.add("storeStatCtxt", new IndexRelation("StoreStatCtxt", "F", 1, null, "V", 2, 0));
		relations.add("storeCtxtArr", new IndexRelation("StoreCtxtArr", "V", 1, 0, "V", 2, 0));

		relations.add("loadCtxt", new IndexRelation("LoadCtxt", "V", 1, 0, "V", 2, 0, 3));
		relations.add("loadStatCtxt", new IndexRelation("LoadStatCtxt", "V", 1, 0, "F", 2, null));
		relations.add("loadCtxtArr", new IndexRelation("LoadCtxtArr", "V", 1, 0, "V", 2, 0));

		// prim assign
		relations.add("assignPrimCtxt", new IndexRelation("AssignPrimCtxt", "U", 1, 0, "U", 2, 0));
		relations.add("assignPrimCCtxt", new IndexRelation("AssignPrimCCtxt", "U", 1, 0, "U", 3, 2));

		// prim load/store
		relations.add("storePrimCtxt", new IndexRelation("StorePrimCtxt", "V", 1, 0, "U", 3, 0, 2));
		relations.add("storeStatPrimCtxt", new IndexRelation("StoreStatPrimCtxt", "F", 1, null, "U", 2, 0));
		relations.add("storePrimCtxtArr", new IndexRelation("StorePrimCtxtArr", "V", 1, 0, "U", 2, 0));

		relations.add("loadPrimCtxt", new IndexRelation("LoadPrimCtxt", "U", 1, 0, "V", 2, 0, 3));
		relations.add("loadStatPrimCtxt", new IndexRelation("LoadStatPrimCtxt", "U", 1, 0, "F", 2, null));
		relations.add("loadPrimCtxtArr", new IndexRelation("LoadPrimCtxtArr", "U", 1, 0, "V", 2, 0));

		/*
		 * Typefilter
		 */
		relations.add("typeFilter", new IndexRelation("TypeFilter", "C", 1, null, "V", 2, 0));

		/*
		 * BDDBDDB partial pt relations
		 */
		relations.add("preFlowsTo", new IndexRelation("ActivePreFlowsTo", "C", 1, null, "V", 2, 0));
		relations.add("postFlowsTo", new IndexRelation("ActivePostFlowsTo", "V", 1, 0, "V", 3, 2));
		relations.add("midFlowsTo", new IndexRelation("ActiveMidFlowsTo", "V", 1, 0, "V", 3, 2));
		relations.add("transfer", new StubIndexRelation("ActiveTransferArg", "V", 1, 0, "V", 2, 0, 3, 4, 5));
		relations.add("transfer", new StubIndexRelation("ActiveTransferRet", "V", 1, 0, "V", 2, 0, 3, 4));
		relations.add("transferSelf", new IndexRelation("ActiveTransferSelf", "V", 1, 0, "V", 2, 0));
	}
	
	/*
	 * Returns the Shord relations associated with a given
	 * JCFLSolver relation. 
	 */
	public static Set<Relation> getChordRelationsFor(String relation) {
		return relations.get(relation);		
	}

	/*
	 * A cache of register maps.
	 */
	private static Map<String,RegisterMap> registerMaps = new HashMap<String,RegisterMap>();

	/*
	 * Gets the node info and concatenates into a String.
	 */
	public static String getNodeInfo(SourceInfo sourceInfo, String node) {
		String[] tokens = getNodeInfoTokens(sourceInfo, node);
		return tokens[0] + (tokens.length >= 2 ? tokens[1] : ""); /*+ (tokens.length == 3 ? "_" + tokens[2] : "")*/
	}

	/*
	 * Returns source information about the node.
	 * a) If the node name is in DomCL (starts with CL), then
	 * maps "CL1" -> ["CL", 1].
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
	public static String[] getNodeInfoTokens(SourceInfo sourceInfo, String node) {
		try {
			// STEP 1: tokenize the node name
			String[] tokens = tokenizeNodeName(node);
			
			// STEP 2: parse labels, reference variables, and primitive variables
			if(tokens[0].equals("CL")) {
				// STEP 2a: if it is a label, then get the string
				DomCL dom = (DomCL)ClassicProject.g().getTrgt("CL");
				tokens[1] = dom.get(Integer.parseInt(tokens[1])).val0;
			} else if(tokens[0].equals("L")) {
				// STEP 2a: if it is a label, then get the string
				DomL dom = (DomL)ClassicProject.g().getTrgt("L");
				tokens[1] = dom.get(Integer.parseInt(tokens[1]));
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
				String sourceFileName = method == null ? "" : sourceInfo.filePath(method.getDeclaringClass());
				int methodLineNum = sourceInfo.methodLineNum(method);
	
				String methStr = "<a onclick=\"showSource('" + sourceFileName + "','false','" + methodLineNum + "')\">" + "[" + method.getName() + "]</a> ";
	
				// HTML hyper link to the register
				RegisterMap regMap = getRegisterMap(sourceInfo, method);
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
			System.out.println("Error parsing node \"" + node + "\"!");
			e.printStackTrace();
			String[] tokens = {node};
			return tokens;
		}
	}

	/*
	 * Returns a register map, caching them as they are requested.
	 */
	private static RegisterMap getRegisterMap(SourceInfo sourceInfo, SootMethod method) {
		RegisterMap registerMap = registerMaps.get(method.toString());
		if(registerMap == null) {
			registerMap = sourceInfo.buildRegMapFor(method);
			registerMaps.put(method.toString(), registerMap);
		}
		return registerMap;
	}

	/*
	 * This function tokenizes graph node names in one of two ways:
	 * a) "V1_2" -> ["V", 1, 2]
	 * b) "CL1" -> ["CL", 1]
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

}
