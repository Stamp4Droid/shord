package stamp.missingmodels.util.cflsolver.relation;

import java.util.Set;

import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;

public class RelationManager {
	
	public static RelationManager relations = new RelationManager();
	static {
		relations.add(new IndexRelation("AllocNew", "H", 1, "V", 0, "alloc"));
		
		relations.add(new IndexRelation("Assign", "V", 1, "V", 0, "assign"));
		
		relations.add(new IndexRelation("param", "V", 1, "V", 0, "param", 2, true, 1));
		relations.add(new IndexRelation("return", "V", 1, "V", 0, "return", 2, false, 1));

		relations.add(new IndexRelation("Store", "V", 2, "V", 0, "store", 1));
		relations.add(new IndexRelation("Load", "V", 1, "V", 0, "load", 2));
		
		relations.add(new IndexRelation("StoreStat", "V", 1, "F", 0, "storeStat"));
		relations.add(new IndexRelation("LoadStat", "F", 1, "V", 0, "loadStat"));
		
		relations.add(new IndexRelation("AssignPrim", "U", 1, "U", 0, "assignPrim"));
		
		relations.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", 2, true, 1));
		relations.add(new IndexRelation("returnPrim", "U", 1, "U", 0, "returnPrim", 2, false, 1));
		
		relations.add(new IndexRelation("StorePrim", "U", 2, "V", 0, "storePrim", 1));
		relations.add(new IndexRelation("LoadPrim", "V", 1, "U", 0, "loadPrim", 2));
		
		relations.add(new IndexRelation("StoreStatPrim", "U", 1, "F", 0, "storeStatPrim"));
		relations.add(new IndexRelation("LoadStatPrim", "F", 1, "U", 0, "loadStatPrim"));

		relations.add(new IndexRelation("Ref2RefT", "V", 1, "V", 2, "ref2RefT"));
		relations.add(new IndexRelation("Ref2PrimT", "V", 1, "U", 2, "ref2PrimT"));
		relations.add(new IndexRelation("Prim2RefT", "U", 1, "V", 2, "prim2RefT"));
		relations.add(new IndexRelation("Prim2PrimT", "U", 1, "U", 2, "prim2PrimT"));
		
		relations.add(new IndexRelation("Ref2RefF", "V", 1, "V", 2, "ref2RefF"));
		relations.add(new IndexRelation("Ref2PrimF", "V", 1, "U", 2, "ref2PrimF"));
		relations.add(new IndexRelation("Prim2RefF", "U", 1, "V", 2, "prim2RefF"));
		relations.add(new IndexRelation("Prim2PrimF", "U", 1, "U", 2, "prim2PrimF"));
		
		relations.add(new IndexRelation("Label2RefT", "L", 1, "V", 2, "label2RefT"));
		relations.add(new IndexRelation("Label2PrimT", "L", 1, "U", 2, "label2PrimT"));
		
		relations.add(new IndexRelation("SinkF2RefF", "L", 1, "V", 2, "sinkF2RefF"));
		relations.add(new IndexRelation("SinkF2PrimF", "L", 1, "U", 2, "sinkF2PrimF"));

		relations.add(new IndexRelation("Src2Label", "L", 0, "L", 0, "src2Label"));
		relations.add(new IndexRelation("Sink2Label", "L", 0, "L", 0, "sink2Label"));
		
		relations.add(new IndexRelation("StoreArr", "V", 1, "V", 0, "storeArr"));
		relations.add(new IndexRelation("LoadArr", "V", 1, "V", 0, "loadArr"));

		relations.add(new IndexRelation("StorePrimArr", "U", 1, "V", 0, "storePrimArr"));
		relations.add(new IndexRelation("LoadPrimArr", "V", 1, "U", 0, "loadPrimArr"));

		relations.add(new IndexRelation("dynparam", "V", 1, "V", 0, "param", 2, true));
		relations.add(new IndexRelation("dynreturn", "V", 1, "V", 0, "return", 2, false));
		
		relations.add(new IndexRelation("dynparamPrim", "U", 1, "U", 0, "paramPrim", 2, true));
		relations.add(new IndexRelation("dynreturnPrim", "U", 1, "U", 0, "returnPrim", 2, false));
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
		
		public void addEdge(GraphBuilder gb, int[] tuple) {
			gb.addEdge(this.getSource(tuple), this.getSink(tuple), this.getSymbol(), this.getField(tuple), this.getContext(tuple), this.getInfo(tuple));
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
