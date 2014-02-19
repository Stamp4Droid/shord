package stamp.analyses;

import java.util.Set;

import shord.analyses.CastVarNode;
import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.analyses.LocalVarNode;
import shord.analyses.ParamVarNode;
import shord.analyses.RetVarNode;
import shord.analyses.ThisVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.Graph;
import stamp.missingmodels.util.cflsolver.Graph.Edge;
import stamp.missingmodels.util.cflsolver.ReachabilitySolver;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar taintGrammar = new ContextFreeGrammar();
	static {
		// pt rules
		taintGrammar.addUnaryProduction("Flow", "newCtxt", true);
		taintGrammar.addBinaryProduction("Flow", "Flow", "assignCtxt", false, true);
		taintGrammar.addBinaryProduction("Flow", "Flow", "assignCCtxt", false, true);
		taintGrammar.addProduction("FlowField", new String[]{"Flow", "storeCtxt", "Flow"}, new boolean[]{false, true, true});
		taintGrammar.addProduction("Flow", new String[]{"FlowField", "Flow", "loadCtxt"}, new boolean[]{false, false, true});
		taintGrammar.addBinaryProduction("FlowStatField", "Flow", "storeStatCtxt", false, true);
		taintGrammar.addBinaryProduction("Flow", "FlowStatField", "loadStatCtxt", false, true);
		
		taintGrammar.addProduction("FlowFieldArr", new String[]{"Flow", "storeCtxtArr", "Flow"}, new boolean[]{false, true, true});
		
		// object annotations
		taintGrammar.addBinaryProduction("Obj2RefT", "Flow", "ref2RefT");
		taintGrammar.addBinaryProduction("Obj2PrimT", "Flow", "ref2PrimT");
		taintGrammar.addBinaryProduction("Obj2RefT", "FlowFieldArr", "Obj2RefT"); // TODO: FlowFieldArr
		taintGrammar.addBinaryProduction("Obj2PrimT", "FlowFieldArr", "Obj2PrimT");
		
		taintGrammar.addBinaryProduction("Label2ObjT", "label2RefT", "Flow", false, true);
		taintGrammar.addBinaryProduction("Label2ObjT", "label2ObjT", "FlowField", false, true, true);
		
		// sinkf
		taintGrammar.addBinaryProduction("SinkF2Obj", "sinkF2RefF", "Flow", false, true);
		taintGrammar.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Obj", "Flow", "ref2RefF", "Flow"}, new boolean[]{false, false, false, true, true});
		taintGrammar.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Prim", "ref2RefPrimF", "Flow"}, new boolean[]{false, false, true, true});
		taintGrammar.addBinaryProduction("SinkF2Obj", "SinkF2Obj", "FieldFlow", false, true, true);
		
		taintGrammar.addUnaryProduction("SinkF2Prim", "sinkF2PrimF");
		taintGrammar.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Obj", "Flow", "prim2RefF"}, new boolean[]{false, false, false, true});
		taintGrammar.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Prim", "Prim2PrimF"}, new boolean[]{false, false, true});
		
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
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "assignPrimCtxt", false, true);
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "assignPrimCCtxt", false, true);

		taintGrammar.addBinaryProduction("Label2Prim", "Label2Obj", "Obj2PrimT");
		taintGrammar.addBinaryProduction("Label2Prim", "Label2Prim", "Prim2PrimT");

		taintGrammar.addProduction("Label2Prim", new String[]{"Label2ObjT", "Flow", "loadPrimCtxt"}, new boolean[]{false, false, true}, true);
		taintGrammar.addProduction("Label2Prim", new String[]{"Label2ObjX", "Flow", "loadPrimCtxtArr"}, new boolean[]{false, false, true});
		taintGrammar.addBinaryProduction("Label2Prim", "Label2PrimFldArr", "Obj2PrimT");

		taintGrammar.addProduction("Label2Prim", new String[]{"Label2PrimFld", "Flow", "loadPrimCtxt"}, new boolean[]{false, false, true});
		taintGrammar.addBinaryProduction("Label2Prim", "Label2PrimFldStat", "loadStatPrimCtxt", false, true);
		
		taintGrammar.addProduction("Label2PrimFld", new String[]{"Label2Prim", "storePrimCtxt", "Flow"}, new boolean[]{false, true, true});
		taintGrammar.addProduction("Label2PrimFldArr", new String[]{"Label2Prim", "storePrimCtxtArr", "Flow"}, new boolean[]{false, true, true});
		taintGrammar.addBinaryProduction("Label2PrimFldStat", "Label2Prim", "storeStatPrimCtxt", false, true);		
	}

	public static void main(String[] args) {
		System.out.println("CFG:");
		System.out.println(taintGrammar.toString());

		Graph g = new Graph(taintGrammar);
		g.addEdge("x", "o1", "newCtxt", (short)1);
		g.addEdge("z", "o2", "newCtxt", (short)1);
		g.addEdge("y", "x", "assignCtxt", (short)1);
		g.addEdge("y", "z", "storeCtxt", "f", (short)1);
		g.addEdge("w", "x", "assignCtxt", (short)1);
		g.addEdge("v", "w", "loadCtxt", "f", (short)1);

		new ReachabilitySolver().solve(taintGrammar, g);	

		System.out.println("Solution:");
		System.out.println(g.toString());
		
		MultivalueMap<String,Edge> sortedEdges = g.getSortedEdges();
		for(String label : sortedEdges.keySet()) {
			System.out.println(label + ": " + sortedEdges.get(label).size());
		}
		for(Set<Edge> edges : sortedEdges.values()) {
			for(Edge edge : edges) {
				System.out.println("Inputs for " + edge);
				for(Edge input : g.getPositiveWeightInputs(edge)) {
					System.out.println(input);
				}
			}
		}
	}

	@Override
	public void run() {
		Graph g = new Graph(taintGrammar);

		for(String label : taintGrammar.getLabelNames()) {
			for(Relation relation : ConversionUtils.getChordRelationsFor(label)) {
				try {
					// STEP 1: Load the Shord relation.
					final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getRelationName());
					rel.load();
					Iterable<int[]> res = rel.getAryNIntTuples();

					// STEP 2: Iterate over relation and add to the graph.
					for(int[] tuple : res) {
						String sourceName = relation.getSourceFromTuple(tuple);
						String sinkName = relation.getSinkFromTuple(tuple);

						if(relation.hasLabel()) {
							g.addEdge(sourceName, sinkName, label, Integer.toString(relation.getLabelFromTuple(tuple)), relation.getWeightFromTuple(tuple));
						} else {
							g.addEdge(sourceName, sinkName, label, relation.getWeightFromTuple(tuple));
						}
					}
					
					rel.close();
				} catch(RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		
		new ReachabilitySolver().solve(taintGrammar, g);
		//System.out.println(g.toString());

		System.out.println("Printing CFL outputs:");
		MultivalueMap<String,Edge> sortedEdges = g.getSortedEdges();
		for(String label : sortedEdges.keySet()) {
			System.out.println(label + ": " + sortedEdges.get(label).size());
		}
		System.out.println();
		
		System.out.println("Printing positive weight inputs:");
		for(Edge edge : sortedEdges.get("Src2Sink")) {
			System.out.println("Src2Sink edge: " + edge.toString());
			for(Edge input : g.getInputs(edge, "assignCCtxt")) {
				//System.out.println("Input edge: " + input);
				try {
					System.out.println("Input method edge: " + getMethodSig(g, input) + " with weight " + input.weight);
				} catch(Exception e) {
					e.printStackTrace();
					System.out.println("Error on edge: " + input);
				}
			}
		}
	}
	
	private static String getMethodSig(Graph graph, Edge edge) {
		VarNode v;
		String[] tokens = graph.getVertexName(edge.source).split("_");
		if(tokens[0].startsWith("V")) {
			DomV dom = (DomV)ClassicProject.g().getTrgt(tokens[0].substring(0,1).toUpperCase());
			v = dom.get(Integer.parseInt(tokens[0].substring(1)));
		} else if(tokens[0].startsWith("U")) {
			DomU dom = (DomU)ClassicProject.g().getTrgt(tokens[0].substring(0,1).toUpperCase());
			v = dom.get(Integer.parseInt(tokens[0].substring(1)));
		} else {
			throw new RuntimeException("Unrecognized vertex: " + tokens[0]);
		}
		if(v instanceof ParamVarNode) {
			return ((ParamVarNode)v).method.toString();
		} else if(v instanceof RetVarNode) {
			return ((RetVarNode)v).method.toString();
		} else if(v instanceof ThisVarNode) {
			return ((ThisVarNode)v).method.toString();
		} else if(v instanceof LocalVarNode) {
			return ((LocalVarNode)v).meth.toString();
		} else if(v instanceof CastVarNode) {
			return ((CastVarNode)v).method.toString();
		} else {
			throw new RuntimeException("Unrecognized variable: " + v);
		}
	}
}
