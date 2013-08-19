package stamp.missingmodels.util;

import java.util.HashMap;

import stamp.missingmodels.util.StubLookup.StubLookupKey;
import stamp.missingmodels.util.StubLookup.StubLookupValue;
import stamp.missingmodels.util.StubModelSet.StubModel;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Edge;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.Graph;

/*
 * A set of stub models, with their setting.
 * 0/null - no (or imprecise) information available
 * 1 - true model
 * 2 - false model
 * 
 * Currently, we only reject false models.
 */
public class StubModelSet extends HashMap<StubModel,Integer> {
	private static final long serialVersionUID = -6501310257337445078L;
	
	public StubModelSet() {
		super();
	}
	
	public StubModelSet(Graph g, StubLookup s) {
		MultivalueMap<Edge,Pair<Edge,Boolean>> positiveWeightEdges = g.getPositiveWeightEdges("Src2Sink");
		for(Edge edge : positiveWeightEdges.keySet()) {
			for(Pair<Edge,Boolean> pair : positiveWeightEdges.get(edge)) {
				EdgeData data = pair.getX().getData(g);					
				this.put(new StubModel(s.get(new StubLookupKey(data.symbol, data.from, data.to))), 0);
			}
		}
	}
	
	@Override
	public Integer get(Object stubModel) {
		Integer value = super.get(stubModel);
		if(value == null) {
			return 0;
		} else {
			return value;
		}
	}
	
	public int get(StubLookupValue value) {
		return this.get(new StubModel(value));
	}
	
	public void putAllValue(StubModelSet m, int value) {
		for(StubModel model : m.keySet()) {
			super.put(model, value);
		}
	}
	
	/*
	 * Represents a stub model. 
	 */
	public static class StubModel {
		private static final String SEPARATOR = ";";
		
		public final String relationName;
		public final String methodName;
		public final Integer firstArg;
		public final Integer secondArg;
		
		public StubModel(String relationName, String methodName, Integer firstArg, Integer secondArg) {
			this.relationName = relationName;
			this.methodName = methodName;
			this.firstArg = firstArg;
			this.secondArg = secondArg;
		}
		
		public StubModel(StubLookupValue value) {
			this.relationName = value.relationName;
			this.methodName = value.method.toString();
			this.firstArg = value.firstArg;
			this.secondArg = value.secondArg;
		}
		
		public StubModel(String representation) {
			String[] tokens = representation.substring(1, representation.length()-1).split(SEPARATOR);
			this.relationName = tokens[0];
			this.methodName = tokens[1];
			this.firstArg = tokens[2].equals("null") ? null : Integer.parseInt(tokens[2]);
			this.secondArg = tokens[3].equals("null") ? null : Integer.parseInt(tokens[3]);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((firstArg == null) ? 0 : firstArg.hashCode());
			result = prime * result
					+ ((methodName == null) ? 0 : methodName.hashCode());
			result = prime * result
					+ ((relationName == null) ? 0 : relationName.hashCode());
			result = prime * result
					+ ((secondArg == null) ? 0 : secondArg.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StubModel other = (StubModel) obj;
			if (firstArg == null) {
				if (other.firstArg != null)
					return false;
			} else if (!firstArg.equals(other.firstArg))
				return false;
			if (methodName == null) {
				if (other.methodName != null)
					return false;
			} else if (!methodName.equals(other.methodName))
				return false;
			if (relationName == null) {
				if (other.relationName != null)
					return false;
			} else if (!relationName.equals(other.relationName))
				return false;
			if (secondArg == null) {
				if (other.secondArg != null)
					return false;
			} else if (!secondArg.equals(other.secondArg))
				return false;
			return true;
		}
		
		@Override public String toString() {
			String firstArgStr = this.firstArg == null ? "null" : this.firstArg.toString();
			String secondArgStr = this.secondArg == null ? "null" : this.secondArg.toString();
			return "{" + this.relationName + SEPARATOR + this.methodName + SEPARATOR + firstArgStr + SEPARATOR + secondArgStr + "}";
		}
	}
}