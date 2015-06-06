package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;

public class FilterRelationManager {
	public static class PointsToFilterRelationManager extends RelationManager {
		public PointsToFilterRelationManager() {
			this.add(new IndexRelation("ptd", "H", 1, "V", 0, "Flow", null, (short)0));
			this.add(new IndexRelation("Label2Primd", "L", 0, "U", 1, "Label2Prim", null, (short)0));
		}
	}
}
