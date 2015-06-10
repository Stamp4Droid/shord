package stamp.missingmodels.util.cflsolver.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;

public class RelationManager {
	public interface RelationReader {
		public Iterable<EdgeStruct> readGraph(RelationManager relations, SymbolMap symbols);
	}
	
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
	
	public static interface Relation {
		public String getName();
		public String getSource(int[] tuple);
		public String getSink(int[] tuple);
		public String getSymbol();
		public int getField(int[] tuple);
		public short getWeight(int[] tuple);
		public boolean filter(int[] tuple);
	}
	
	public static Collection<EdgeStruct> readRelation(Relation relation, int[] tuple) {
		List<EdgeStruct> edge = new ArrayList<EdgeStruct>();
		if(!relation.filter(tuple)) {
			return edge;
		}
		
		String source = relation.getSource(tuple);
		String sink = relation.getSink(tuple);
		String symbol = relation.getSymbol();
		int field = relation.getField(tuple);
		short weight = relation.getWeight(tuple);
		
		edge.add(new EdgeStruct(source, sink, symbol, field, weight));
		return edge;
	}
	
	public static class IndexRelation implements Relation {
		private final String name;
		
		private final String sourceName;
		private final int sourceIndex;
		private final String sinkName;
		private final int sinkIndex;
		private final String symbol;
		
		private final Integer fieldIndex;
		
		private final short weight;
		
		private final Filter<int[]> filter;
		
		public IndexRelation(String name, String sourceDom, int sourceIndex, String sinkDom, int sinkIndex, String symbol, Integer fieldIndex, short weight, Filter<int[]> filter) {
			this.name = name;
			
			this.sourceName = sourceDom;
			this.sourceIndex = sourceIndex;
			this.sinkName = sinkDom;
			this.sinkIndex = sinkIndex;
			this.symbol = symbol;
			
			this.fieldIndex = fieldIndex;
			
			this.weight = weight;
			
			this.filter = filter;
		}
		
		public IndexRelation(String name, String sourceDom, int sourceIndex, String sinkDom, int sinkIndex, String symbol, Integer fieldIndex, short weight) {
			this(name, sourceDom, sourceIndex, sinkDom, sinkIndex, symbol, fieldIndex, weight, new Filter<int[]>() { public boolean filter(int[] tuple) { return true; }});
		}
			
		public IndexRelation(String name, String sourceName, int sourceIndex, String sinkName, int sinkIndex, String symbol) {
			this(name, sourceName, sourceIndex, sinkName, sinkIndex, symbol, null, (short)0);
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
		public int getField(int[] tuple) {
			return this.fieldIndex == null ? Field.DEFAULT_FIELD.field : tuple[this.fieldIndex];
		}

		@Override
		public short getWeight(int[] tuple) {
			return this.weight;
		}

		@Override
		public boolean filter(int[] tuple) {
			return this.filter.filter(tuple);
		}
	}

	public static class IndexWithContextRelation implements Relation {
		private final String name;
		
		private final String sourceName;
		private final String sinkName;

		private final int sourceIndex;
		private final int sinkIndex;

		private final String symbol;
		
		private final int sourceContextIndex;
		private final int sinkContextIndex;
		private final int fieldIndex;

		private final short weight;

		private final boolean hasSourceContext;
		private final boolean hasSinkContext;
		private final boolean hasField;
		
		private final Filter<int[]> filter;
		
		public IndexWithContextRelation(String relationName, String sourceName, int sourceIndex, Integer sourceContextIndex, String sinkName, int sinkIndex, Integer sinkContextIndex, String symbol, Integer fieldIndex, short weight, Filter<int[]> filter) {
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

			this.weight = weight;

			this.sourceName = sourceName;
			this.sinkName = sinkName;
			
			this.filter = filter;
		}
		
		public IndexWithContextRelation(String relationName, String sourceName, int sourceIndex, Integer sourceContextIndex, String sinkName, int sinkIndex, Integer sinkContextIndex, String symbol, Integer fieldIndex, short weight) {
			this(relationName, sourceName, sourceIndex, sourceContextIndex, sinkName, sinkIndex, sinkContextIndex, symbol, fieldIndex, weight, new Filter<int[]>() { public boolean filter(int[] tuple) { return true; }});
		}

		
		public IndexWithContextRelation(String name, String sourceName, int sourceIndex, Integer sourceContextIndex, String sinkName, int sinkIndex, Integer sinkContextIndex, String symbol, Integer fieldIndex) {
			this(name, sourceName, sourceIndex, sourceContextIndex, sinkName, sinkIndex, sinkContextIndex, symbol, fieldIndex, (short)0);
		}
		
		public IndexWithContextRelation(String name, String sourceName, int sourceIndex, Integer sourceContextIndex, String sinkName, int sinkIndex, Integer sinkContextIndex, String symbol) {
			this(name, sourceName, sourceIndex, sourceContextIndex, sinkName, sinkIndex, sinkContextIndex, symbol, null, (short)0);
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
		public int getField(int[] tuple) {
			return this.hasField ? tuple[fieldIndex] : Field.DEFAULT_FIELD.field;
		}
		
		@Override
		public short getWeight(int[] tuple) {
			return this.weight;
		}

		@Override
		public boolean filter(int[] tuple) {
			return this.filter.filter(tuple);
		}
	}
}
