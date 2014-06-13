package stamp.missingmodels.util.cflsolver.relation;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeInfo;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;

public class RelationManager {	
	private final MultivalueMap<String,Relation> relationsByName = new MultivalueMap<String,Relation>();
	private final MultivalueMap<String,Relation> relationsBySymbol = new MultivalueMap<String,Relation>();
	
	public void add(Relation relation) {
		this.relationsByName.add(relation.getName(), relation);
		this.relationsBySymbol.add(relation.getSymbol(), relation);
	}
	
	protected void clearRelationsByName(String name) {
		for(Relation relation : this.relationsByName.get(name)) {
			this.relationsBySymbol.get(relation.getSymbol()).remove(relation);
		}
		this.relationsByName.remove(name);
	}
	
	protected void clearRelationsBySymbol(String symbol) {
		for(Relation relation : this.relationsBySymbol.get(symbol)) {
			this.relationsByName.get(relation.getName()).remove(relation);
		}
		this.relationsBySymbol.remove(symbol);
	}
	
	public Set<String> getRelationNames() {
		return Collections.unmodifiableSet(this.relationsByName.keySet());
	}
	
	public Set<String> getRelationSymbols() {
		return Collections.unmodifiableSet(this.relationsBySymbol.keySet());
	}
	
	public Collection<Relation> getRelationsByName(String name) {
		return Collections.unmodifiableCollection(this.relationsByName.get(name));
	}
	
	public Collection<Relation> getRelationsBySymbol(String symbol) {
		return Collections.unmodifiableCollection(this.relationsBySymbol.get(symbol));
	}
	
	public static abstract class Relation {
		public abstract String getName();
		
		public abstract String getSource(int[] tuple);
		public abstract String getSink(int[] tuple);
		public abstract String getSymbol();
		
		public abstract Context getContext(int[] tuple);
		public abstract Field getField(int[] tuple);
		
		public abstract int getWeight(int[] tuple);
		
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
			int weight = this.getWeight(tuple);
			
			if(curInfo == null || curInfo.weight > weight) {
				gb.addEdge(source, sink, symbol, field, context, new EdgeInfo(weight));
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
		
		private final int weight;
		
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
			
			this.weight = weight;
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
		public int getWeight(int[] tuple) {
			return this.weight;
		}
	}

	public static class IndexWithContextRelation extends Relation {
		private final String name;
		
		private final String sourceName;
		private final String sinkName;

		private final int sourceIndex;
		private final int sinkIndex;

		private final String symbol;
		
		private final int sourceContextIndex;
		private final int sinkContextIndex;
		private final int fieldIndex;

		private final int weight;

		private final boolean hasSourceContext;
		private final boolean hasSinkContext;
		private final boolean hasField;
		
		public IndexWithContextRelation(String relationName, String sourceName, int sourceIndex, Integer sourceContextIndex, String sinkName, int sinkIndex, Integer sinkContextIndex, String symbol, Integer fieldIndex, Integer weight) {
			this.name = relationName;

			this.hasSourceContext = sourceContextIndex != null;
			this.hasSinkContext = sinkContextIndex != null;
			this.hasField = fieldIndex != null;

			this.sourceIndex = sourceIndex;
			this.sourceContextIndex = this.hasSourceContext ? sourceContextIndex : -1;

			this.sinkIndex = sinkIndex;
			this.sinkContextIndex = this.hasSinkContext ? sinkContextIndex : -1;
			
			this.symbol = symbol;

			this.fieldIndex = this.hasField ? fieldIndex : -1;

			this.weight = weight == null ? 0 : weight; 

			this.sourceName = sourceName;
			this.sinkName = sinkName;
		}
		
		public IndexWithContextRelation(String name, String sourceName, int sourceIndex, Integer sourceContextIndex, String sinkName, int sinkIndex, Integer sinkContextIndex, String symbol, Integer fieldIndex) {
			this(name, sourceName, sourceIndex, sourceContextIndex, sinkName, sinkIndex, sinkContextIndex, symbol, fieldIndex, null);
		}
		
		public IndexWithContextRelation(String name, String sourceName, int sourceIndex, Integer sourceContextIndex, String sinkName, int sinkIndex, Integer sinkContextIndex, String symbol) {
			this(name, sourceName, sourceIndex, sourceContextIndex, sinkName, sinkIndex, sinkContextIndex, symbol, null, null);
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getSymbol() {
			return this.symbol;
		}

		@Override
		public String getSource(int[] tuple) {
			return this.sourceName + Integer.toString(tuple[this.sourceIndex]) + (this.hasSourceContext ? "_" + Integer.toString(tuple[this.sourceContextIndex]) : "");
		}

		@Override
		public String getSink(int[] tuple) {
			return this.sinkName + Integer.toString(tuple[this.sinkIndex]) + (this.hasSinkContext ? "_" + Integer.toString(tuple[this.sinkContextIndex]) : "");
		}

		@Override
		public Field getField(int[] tuple) {
			return this.hasField ? new Field(tuple[fieldIndex]) : Field.DEFAULT_FIELD;
		}
		
		@Override
		public int getWeight(int[] tuple) {
			return this.weight;
		}

		@Override
		public boolean filter(int[] tuple) {
			return true;
		}
		
		@Override
		public Context getContext(int[] tuple) {
			return Context.DEFAULT_CONTEXT;
		}
	}
}
