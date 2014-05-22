package stamp.missingmodels.util.cflsolver.relation;

import java.util.List;
import java.util.Set;

import shord.analyses.CastVarNode;
import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.analyses.LocalVarNode;
import shord.analyses.ParamVarNode;
import shord.analyses.RetVarNode;
import shord.analyses.StringConstantVarNode;
import shord.analyses.ThisVarNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.SootMethod;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;

public class RelationManager {
	public static SootMethod getMethodForVar(VarNode v) {
		if(v instanceof ParamVarNode) {
			return ((ParamVarNode)v).method;
		} else if(v instanceof RetVarNode) {
			return ((RetVarNode)v).method;
		} else if(v instanceof ThisVarNode) {
			return ((ThisVarNode)v).method;
		} else if(v instanceof LocalVarNode) {
			return ((LocalVarNode)v).meth;
		} else if(v instanceof CastVarNode) {
			return ((CastVarNode)v).method;
		} else if(v instanceof StringConstantVarNode) {
			return ((StringConstantVarNode)v).method;
		} else {
			throw new RuntimeException("Unrecognized variable: " + v);
		}
	}
	
	public static class ShordRelationManager extends RelationManager {
		public ShordRelationManager(List<Pair<String,String>> dynamicCallgraphList, int numEdges) {
			this();
			
			// STEP 1: Extract the dynamic callgraph
			MultivalueMap<String,String> dynamicCallgraph = new MultivalueMap<String,String>();
			int counter = 0;
			for(Pair<String,String> callgraphEdge : dynamicCallgraphList) {
				dynamicCallgraph.add(callgraphEdge.getX(), callgraphEdge.getY());
				if(numEdges != -1 && counter++ > numEdges) {
					break;
				}
			}
			
			// STEP 2: Build the callgraph from param edges
			final MultivalueMap<String,String> dynamicCallgraphConverted = new MultivalueMap<String,String>();
			
			ProgramRel paramRel = (ProgramRel)ClassicProject.g().getTrgt("param");
			DomV domV = (DomV)ClassicProject.g().getTrgt("V");
			paramRel.load();
			for(int[] tuple : paramRel.getAryNIntTuples()) {
				String caller = getMethodForVar(domV.get(tuple[1])).toString();
				String callee = getMethodForVar(domV.get(tuple[0])).toString();
				if(dynamicCallgraph.get(caller).contains(callee)) {
					System.out.println("dynamic callgraph edge: " + caller + " -> " + callee);
					dynamicCallgraphConverted.add("V" + Integer.toString(tuple[1]), "V" + Integer.toString(tuple[0]));
				}
			}
			paramRel.close();	
			
			ProgramRel paramPrimRel = (ProgramRel)ClassicProject.g().getTrgt("paramPrim");
			DomU domU = (DomU)ClassicProject.g().getTrgt("U");
			paramPrimRel.load();
			for(int[] tuple : paramPrimRel.getAryNIntTuples()) {
				String caller = getMethodForVar(domU.get(tuple[1])).toString();
				String callee = getMethodForVar(domU.get(tuple[0])).toString();
				if(dynamicCallgraph.get(caller).contains(callee)) {
					System.out.println("dynamic callgraph edge: " + caller + " -> " + callee);
					dynamicCallgraphConverted.add("U" + Integer.toString(tuple[1]), "U" + Integer.toString(tuple[0]));
				}
			}
			paramPrimRel.close();
			
			// STEP 3: Build the relations			
			this.add(new IndexRelation("dynparam", "V", 1, "V", 0, "param", 2, true) {
				@Override
				public boolean filter(int[] tuple) {
					return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
				}
			});		
			this.add(new IndexRelation("dynparamPrim", "U", 1, "U", 0, "paramPrim", 2, true) {
				@Override
				public boolean filter(int[] tuple) {
					return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
				}
			});
		}

		public ShordRelationManager(List<Pair<String,String>> dynamicCallgraphList) {
			this(dynamicCallgraphList, -1);
		}
		
		public ShordRelationManager() {
			this.add(new IndexRelation("AllocNew", "H", 1, "V", 0, "alloc"));
			
			this.add(new IndexRelation("Assign", "V", 1, "V", 0, "assign"));
			
			this.add(new IndexRelation("param", "V", 1, "V", 0, "param", 2, true, 1));
			this.add(new IndexRelation("return", "V", 1, "V", 0, "return", 2, false));
	
			this.add(new IndexRelation("Store", "V", 2, "V", 0, "store", 1));
			this.add(new IndexRelation("Load", "V", 1, "V", 0, "load", 2));
			
			this.add(new IndexRelation("StoreStat", "V", 1, "F", 0, "storeStat"));
			this.add(new IndexRelation("LoadStat", "F", 1, "V", 0, "loadStat"));
			
			this.add(new IndexRelation("AssignPrim", "U", 1, "U", 0, "assignPrim"));
			
			this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", 2, true, 1));
			this.add(new IndexRelation("returnPrim", "U", 1, "U", 0, "returnPrim", 2, false));
			
			this.add(new IndexRelation("StorePrim", "U", 2, "V", 0, "storePrim", 1));
			this.add(new IndexRelation("LoadPrim", "V", 1, "U", 0, "loadPrim", 2));
			
			this.add(new IndexRelation("StoreStatPrim", "U", 1, "F", 0, "storeStatPrim"));
			this.add(new IndexRelation("LoadStatPrim", "F", 1, "U", 0, "loadStatPrim"));
	
			this.add(new IndexRelation("Ref2RefT", "V", 1, "V", 2, "ref2RefT"));
			this.add(new IndexRelation("Ref2PrimT", "V", 1, "U", 2, "ref2PrimT"));
			this.add(new IndexRelation("Prim2RefT", "U", 1, "V", 2, "prim2RefT"));
			this.add(new IndexRelation("Prim2PrimT", "U", 1, "U", 2, "prim2PrimT"));
			
			this.add(new IndexRelation("Ref2RefF", "V", 1, "V", 2, "ref2RefF"));
			this.add(new IndexRelation("Ref2PrimF", "V", 1, "U", 2, "ref2PrimF"));
			this.add(new IndexRelation("Prim2RefF", "U", 1, "V", 2, "prim2RefF"));
			this.add(new IndexRelation("Prim2PrimF", "U", 1, "U", 2, "prim2PrimF"));
			
			this.add(new IndexRelation("Label2RefT", "L", 1, "V", 2, "label2RefT"));
			this.add(new IndexRelation("Label2PrimT", "L", 1, "U", 2, "label2PrimT"));
			
			this.add(new IndexRelation("SinkF2RefF", "L", 1, "V", 2, "sinkF2RefF"));
			this.add(new IndexRelation("SinkF2PrimF", "L", 1, "U", 2, "sinkF2PrimF"));
	
			this.add(new IndexRelation("Src2Label", "L", 0, "L", 0, "src2Label"));
			this.add(new IndexRelation("Sink2Label", "L", 0, "L", 0, "sink2Label"));
			
			this.add(new IndexRelation("StoreArr", "V", 1, "V", 0, "storeArr"));
			this.add(new IndexRelation("LoadArr", "V", 1, "V", 0, "loadArr"));
	
			this.add(new IndexRelation("StorePrimArr", "U", 1, "V", 0, "storePrimArr"));
			this.add(new IndexRelation("LoadPrimArr", "V", 1, "U", 0, "loadPrimArr"));
			
			/*
			this.add(new IndexRelation("dynparam", "V", 1, "V", 0, "param", 2, true));
			this.add(new IndexRelation("dynparamPrim", "U", 1, "U", 0, "paramPrim", 2, true));
			
			this.add(new IndexRelation("dynreturn", "V", 1, "V", 0, "return", 2, false));
			this.add(new IndexRelation("dynreturnPrim", "U", 1, "U", 0, "returnPrim", 2, false));
			*/
		}
	}
	
	private final MultivalueMap<String,Relation> relationsByName = new MultivalueMap<String,Relation>();
	private final MultivalueMap<String,Relation> relationsBySymbol = new MultivalueMap<String,Relation>();
	
	public void add(Relation relation) {
		this.relationsByName.add(relation.getName(), relation);
		this.relationsBySymbol.add(relation.getSymbol(), relation);
	}
	
	public Set<Relation> getRelationsByName(String name) {
		return this.relationsByName.get(name);
	}
	
	public Set<Relation> getRelationsBySymbol(String symbol) {
		return this.relationsBySymbol.get(symbol);
	}
	
	public static abstract class Relation {
		public abstract String getName();
		
		public abstract String getSource(int[] tuple);
		public abstract String getSink(int[] tuple);
		public abstract String getSymbol();
		
		public abstract Context getContext(int[] tuple);
		public abstract Field getField(int[] tuple);
		
		public abstract EdgeInfo getInfo(int[] tuple);
		
		public boolean filter(int[] tuple) {
			return true;
		}
		
		public void addEdge(GraphBuilder gb, int[] tuple) {
			if(!this.filter(tuple)) {
				return;
			}
			
			String source = this.getSource(tuple);
			String sink = this.getSink(tuple);
			String symbol = this.getSymbol();
			Field field = this.getField(tuple);
			Context context = this.getContext(tuple);
			
			EdgeInfo curInfo = gb.toGraph().getInfo(source, sink, symbol, field, context);
			EdgeInfo newInfo = this.getInfo(tuple);
			
			if(curInfo == null || curInfo.weight > newInfo.weight) {
				gb.addEdge(source, sink, symbol, field, context, newInfo);
			}
		}
	}
	
	public static class IndexRelation extends Relation {
		private final String name;
		
		private final String sourceName;
		private final int sourceIndex;
		private final String sinkName;
		private final int sinkIndex;
		private final String symbol;
		
		private final Integer fieldIndex;
		private final Integer contextIndex;
		private final boolean contextDirection;
		
		private final EdgeInfo info;
		
		public IndexRelation(String name, String sourceDom, int sourceIndex, String sinkDom, int sinkIndex, String symbol, Integer fieldIndex, Integer contextIndex, boolean contextDirection, int weight) {
			this.name = name;
			
			this.sourceName = sourceDom;
			this.sourceIndex = sourceIndex;
			this.sinkName = sinkDom;
			this.sinkIndex = sinkIndex;
			this.symbol = symbol;
			
			this.fieldIndex = fieldIndex;
			this.contextIndex = contextIndex;
			this.contextDirection = contextDirection;
			
			this.info = new EdgeInfo(weight);
		}

		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol, Integer fieldIndex, int weight) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, fieldIndex, null, true, weight);
		}
		
		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol, Integer contextIndex, boolean contextDirection, int weight) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, null, contextIndex, contextDirection, weight);
		}
		
		/*
		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol, int weight) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, null, null, true, weight);
		}
		*/
		
		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol, Integer fieldIndex, Integer contextIndex, boolean contextDirection) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, fieldIndex, contextIndex, contextDirection, 0);
		}
		
		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol, Integer fieldIndex) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, fieldIndex, null, true, 0);
		}
		
		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol, Integer contextIndex, boolean contextDirection) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, null, contextIndex, contextDirection, 0);
		}
		
		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, null, null, true, 0);
		}
		
		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getSource(int[] tuple) {
			return this.sourceName + Integer.toString(tuple[this.sourceIndex]);
		}

		@Override
		public String getSink(int[] tuple) {
			return this.sinkName + Integer.toString(tuple[this.sinkIndex]);
		}

		@Override
		public String getSymbol() {
			return this.symbol;
		}

		@Override
		public Field getField(int[] tuple) {
			return this.fieldIndex == null ? Field.DEFAULT_FIELD : new Field(tuple[this.fieldIndex]);
		}

		@Override
		public Context getContext(int[] tuple) {
			return this.contextIndex == null ? Context.DEFAULT_CONTEXT : new Context(tuple[this.contextIndex], this.contextDirection);
		}

		@Override
		public EdgeInfo getInfo(int[] tuple) {
			return this.info;
		}
	}
}
