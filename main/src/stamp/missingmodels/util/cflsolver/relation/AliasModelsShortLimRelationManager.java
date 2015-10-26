package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;

public class AliasModelsShortLimRelationManager extends RelationManager {
	private static class IndexFilter implements Filter<int[]> {
		private final int index;
		private final int value;
		private IndexFilter(int index, int value) {
			this.index = index;
			this.value = value;
		}
		public boolean filter(int[] tuple) {
			return tuple[this.index] == value;
		}
	}
	
	public AliasModelsShortLimRelationManager(boolean useContext) {
		if(useContext) {
			this.add(new IndexWithContextRelation("FlowPreK", "C", 0, null, "M", 3, 2, "FlowPre0", null, (short)1, new IndexFilter(1, 0)));
			this.add(new IndexWithContextRelation("FlowPreK", "C", 0, null, "M", 3, 2, "FlowPre0", null, (short)1, new IndexFilter(1, 1)));
			this.add(new IndexWithContextRelation("FlowPrePostK", "M", 2, 1, "M", 5, 4, "FlowPrePost00", null, (short)1, new AndFilter(new IndexFilter(0, 0), new IndexFilter(3, 0))));
			this.add(new IndexWithContextRelation("FlowPrePostK", "M", 2, 1, "M", 5, 4, "FlowPrePost01", null, (short)1, new AndFilter(new IndexFilter(0, 0), new IndexFilter(3, 1))));
			this.add(new IndexWithContextRelation("FlowPrePostK", "M", 2, 1, "M", 5, 4, "FlowPrePost10", null, (short)1, new AndFilter(new IndexFilter(0, 1), new IndexFilter(3, 0))));
			this.add(new IndexWithContextRelation("FlowPrePostK", "M", 2, 1, "M", 5, 4, "FlowPrePost11", null, (short)1, new AndFilter(new IndexFilter(0, 1), new IndexFilter(3, 1))));
			this.add(new IndexWithContextRelation("assignE", "M", 0, 1, "V", 2, 1, "assignE", null, (short)1));
		} else {
			this.add(new IndexRelation("FlowPreK", "C", 0, "M", 3, "FlowPre0", null, (short)1, new IndexFilter(1, 0)));
			this.add(new IndexRelation("FlowPreK", "C", 0, "M", 3, "FlowPre0", null, (short)1, new IndexFilter(1, 1)));
			this.add(new IndexRelation("FlowPrePostK", "M", 2, "M", 5, "FlowPrePost00", null, (short)1, new AndFilter(new IndexFilter(0, 0), new IndexFilter(3, 0))));
			this.add(new IndexRelation("FlowPrePostK", "M", 2, "M", 5, "FlowPrePost01", null, (short)1, new AndFilter(new IndexFilter(0, 0), new IndexFilter(3, 1))));
			this.add(new IndexRelation("FlowPrePostK", "M", 2, "M", 5, "FlowPrePost10", null, (short)1, new AndFilter(new IndexFilter(0, 1), new IndexFilter(3, 0))));
			this.add(new IndexRelation("FlowPrePostK", "M", 2, "M", 5, "FlowPrePost11", null, (short)1, new AndFilter(new IndexFilter(0, 1), new IndexFilter(3, 1))));
			this.add(new IndexRelation("assignE", "M", 0, "V", 2, "assignE", null, (short)1));
		}
	}
}
