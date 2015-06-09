package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;

public class AliasModelsRelationManager extends RelationManager {
	public static class TaintAliasModelsPointsToRelationManager extends UnionRelationManager {
		public TaintAliasModelsPointsToRelationManager(boolean useContext) {
			super(new PointsToRelationManager(), new TaintRelationManager(), new AliasModelsRelationManager(useContext));
			this.clearRelationsBySymbol("Flow");
		}
	}
	
	public static class AliasModelsLimRelationManager extends RelationManager {
		public AliasModelsLimRelationManager() {
			this.add(new IndexWithContextRelation("ActiveFlowPre", "C", 0, null, "M", 2, 1, "FlowPre", null, (short)1));
			this.add(new IndexWithContextRelation("ActiveFlowPrePost", "M", 1, 0, "M", 3, 2, "FlowPrePost", null, (short)1));
			this.add(new IndexWithContextRelation("assignECtxt", "M", 1, 0, "V", 2, 0, "assignE", null, (short)0));
		}
	}
	
	public AliasModelsRelationManager(boolean useContext) {
		if(useContext) {
			this.add(new IndexWithContextRelation("AssignCtxt", "V", 2, 0, "V", 1, 0, "assign"));
			this.add(new IndexWithContextRelation("AssignArgCCtxt", "V", 3, 2, "V", 1, 0, "param"));
			this.add(new IndexWithContextRelation("AssignRetCCtxt", "V", 3, 2, "V", 1, 0, "return"));
			this.add(new IndexWithContextRelation("StoreCtxt", "V", 3, 0, "V", 1, 0, "store", 2, (short)0));
			this.add(new IndexWithContextRelation("LoadCtxt", "V", 2, 0, "V", 1, 0, "load", 3, (short)0));
			
			this.add(new IndexWithContextRelation("BassignCtxt", "V", 2, 0, "M", 1, 0, "Bassign", null, (short)1));
			this.add(new IndexWithContextRelation("assignECtxt", "M", 1, 0, "V", 2, 0, "assignE", null, (short)1));
			this.add(new IndexWithContextRelation("Flow", "C", 0, null, "V", 2, 1, "Flow", null, (short)0));
			this.add(new IndexWithContextRelation("FlowField", "C", 0, null, "C", 2, null, "FlowField", 1, (short)0));
		} else {
			this.add(new IndexRelation("Assign", "V", 1, "V", 0, "assign"));
			this.add(new IndexRelation("param", "V", 1, "V", 0, "param"));
			this.add(new IndexRelation("return", "V", 1, "V", 0, "return"));
			this.add(new IndexRelation("Store", "V", 2, "V", 0, "store", 1, (short)0));
			this.add(new IndexRelation("Load", "V", 1, "V", 0, "load", 2, (short)0));
			
			this.add(new IndexRelation("BassignCtxt", "V", 2, "M", 1, "Bassign", null, (short)1));
			this.add(new IndexRelation("assignECtxt", "M", 1, "V", 2, "assignE", null, (short)1));
			this.add(new IndexRelation("Flow", "C", 0, "V", 2, "Flow", null, (short)0));
			this.add(new IndexRelation("FlowField", "C", 0, "C", 2, "FlowField", 1, (short)0));
		}
	}
}
