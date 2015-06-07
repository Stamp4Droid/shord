package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;

public class FilterRelationManager {
	public static class PointsToFilterRelationManager extends RelationManager {
		public PointsToFilterRelationManager() {
			this.add(new IndexRelation("ptd", "H", 1, "V", 0, "Flow", null, (short)0));
			this.add(new IndexRelation("Label2Primd", "L", 0, "U", 1, "Label2Prim", null, (short)0));
		}
	}
	
	public static class TypeFilterRelationManager extends RelationManager {
		public TypeFilterRelationManager() {
			this.add(new IndexRelation("HVFilter", "H", 1, "V", 0, "Flow", null, (short)0));
		}
	}
	
	public static class AliasModelsFilterRelationManager extends RelationManager {
		public AliasModelsFilterRelationManager(boolean useContext) {
			if(useContext) {

				this.add(new IndexWithContextRelation("ActiveFlowPre", "C", 0, null, "M", 2, 1, "FlowPre", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPost", "M", 1, 0, "V", 3, 2, "FlowPost", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPrePost", "M", 1, 0, "M", 3, 2, "FlowPrePost", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPostField", "M", 1, 0, "C", 3, null, "FlowPostField", 2, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPostAnyField", "M", 1, 0, "C", 2, null, "FlowPostAnyField", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowNew", "C", 0, null, "V", 2, 1, "FlowNew", null, (short)0));
			} else {
				this.add(new IndexRelation("ActiveFlowPre", "C", 0, "M", 2, "FlowPre", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPost", "M", 1, "V", 3, "FlowPost", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPrePost", "M", 1, "M", 3, "FlowPrePost", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPostField", "M", 1, "C", 3, "FlowPostField", 2, (short)0));
				this.add(new IndexRelation("ActiveFlowPostAnyField", "M", 1, "C", 2, "FlowPostAnyField", null, (short)0));
				this.add(new IndexRelation("ActiveFlowNew", "C", 0, "V", 2, "FlowNew", null, (short)0));
			}
		}
	}
}
