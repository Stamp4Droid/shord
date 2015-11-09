package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;

public class FilterRelationManager {
	public static class PointsToFilterRelationManager extends RelationManager {
		public PointsToFilterRelationManager() {
			this.add(new IndexRelation("ptd", "H", 1, "V", 0, "Flow", null, (short)0));
			this.add(new IndexWithContextRelation("fptd", "H", 2, null, "H", 0, null, "FlowField", 1));
			this.add(new IndexWithContextRelation("fptArrd", "H", 1, null, "H", 0, null, "FlowFieldArr"));
			this.add(new IndexRelation("Label2Primd", "L", 0, "U", 1, "Label2Prim", null, (short)0));
		}
	}
	
	public static class TypeFilterRelationManager extends RelationManager {
		public TypeFilterRelationManager() {
			this.add(new IndexRelation("HVFilter", "H", 1, "V", 0, "Flow", null, (short)0));
		}
	}
	
	public static class AliasModelsLimFilterRelationManager extends RelationManager {
		public AliasModelsLimFilterRelationManager(boolean useContext) {
			if(useContext) {
				this.add(new IndexWithContextRelation("ActiveFlowPreFull", "C", 0, null, "M", 2, 1, "FlowPreFull", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowNew", "C", 0, null, "V", 2, 1, "FlowNew", null, (short)0));
			} else {
				this.add(new IndexRelation("ActiveFlowPreFull", "C", 0, "M", 2, "FlowPreFull", null, (short)0));
				this.add(new IndexRelation("ActiveFlowNew", "C", 0, "V", 2, "FlowNew", null, (short)0));
			}
		}
	}
	
	public static class AliasModelsFilterRelationManager extends AliasModelsLimFilterRelationManager {
		public AliasModelsFilterRelationManager(boolean useContext) {
			super(useContext);
			if(useContext) {
				this.add(new IndexWithContextRelation("ActiveFlowPre", "C", 0, null, "M", 2, 1, "FlowPre", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPost", "M", 1, 0, "V", 3, 2, "FlowPost", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPrePost", "M", 1, 0, "M", 3, 2, "FlowPrePost", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowFieldPost", "M", 1, 0, "C", 3, null, "FlowPostField", 2, (short)0));
			} else {
				this.add(new IndexRelation("ActiveFlowPre", "C", 0, "M", 2, "FlowPre", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPost", "M", 1, "V", 3, "FlowPost", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPrePost", "M", 1, "M", 3, "FlowPrePost", null, (short)0));
				this.add(new IndexRelation("ActiveFlowFieldPost", "M", 1, "C", 3, "FlowPostField", 2, (short)0));
			}
		}
	}
	
	public static class AliasModelsShortLimFilterRelationManager extends RelationManager {
		public AliasModelsShortLimFilterRelationManager(boolean useContext) {
			if(useContext) {
				this.add(new IndexWithContextRelation("ActiveFlowPreFull", "C", 0, null, "M", 2, 1, "FlowPreFull0", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPreFull", "C", 0, null, "M", 2, 1, "FlowPreFull1", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowNew", "C", 0, null, "V", 2, 1, "FlowNew", null, (short)0));
			} else {
				this.add(new IndexRelation("ActiveFlowPreFull", "C", 0, "M", 2, "FlowPreFull0", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPreFull", "C", 0, "M", 2, "FlowPreFull1", null, (short)0));
				this.add(new IndexRelation("ActiveFlowNew", "C", 0, "V", 2, "FlowNew", null, (short)0));
			}
		}
	}
	
	public static class AliasModelsShortFilterRelationManager extends AliasModelsShortLimFilterRelationManager {
		public AliasModelsShortFilterRelationManager(boolean useContext) {
			super(useContext);
			if(useContext) {
				this.add(new IndexWithContextRelation("ActiveFlowPre", "C", 0, null, "M", 2, 1, "FlowPre0", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPre", "C", 0, null, "M", 2, 1, "FlowPre1", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPost", "M", 1, 0, "V", 3, 2, "FlowPost0", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPost", "M", 1, 0, "V", 3, 2, "FlowPost1", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPrePost", "M", 1, 0, "M", 3, 2, "FlowPrePost00", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPrePost", "M", 1, 0, "M", 3, 2, "FlowPrePost01", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPrePost", "M", 1, 0, "M", 3, 2, "FlowPrePost10", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowPrePost", "M", 1, 0, "M", 3, 2, "FlowPrePost11", null, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowFieldPost", "M", 1, 0, "C", 3, null, "FlowPostField0", 2, (short)0));
				this.add(new IndexWithContextRelation("ActiveFlowFieldPost", "M", 1, 0, "C", 3, null, "FlowPostField1", 2, (short)0));
			} else {
				this.add(new IndexRelation("ActiveFlowPre", "C", 0, "M", 2, "FlowPre0", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPre", "C", 0, "M", 2, "FlowPre1", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPost", "M", 1, "V", 3, "FlowPost0", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPost", "M", 1, "V", 3, "FlowPost1", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPrePost", "M", 1, "M", 3, "FlowPrePost00", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPrePost", "M", 1, "M", 3, "FlowPrePost01", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPrePost", "M", 1, "M", 3, "FlowPrePost10", null, (short)0));
				this.add(new IndexRelation("ActiveFlowPrePost", "M", 1, "M", 3, "FlowPrePost11", null, (short)0));
				this.add(new IndexRelation("ActiveFlowFieldPost", "M", 1, "C", 3, "FlowPostField0", 2, (short)0));
				this.add(new IndexRelation("ActiveFlowFieldPost", "M", 1, "C", 3, "FlowPostField1", 2, (short)0));
			}
		}
	}
}
