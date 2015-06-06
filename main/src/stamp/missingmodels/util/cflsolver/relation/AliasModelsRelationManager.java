package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;

public class AliasModelsRelationManager extends RelationManager {
	public static class TaintAliasModelsPointsToRelationManager extends UnionRelationManager {
		public TaintAliasModelsPointsToRelationManager() {
			super(new PointsToRelationManager(), new TaintRelationManager(), new AliasModelsRelationManager());
			this.clearRelationsBySymbol("Flow");
		}
	}
	
	public AliasModelsRelationManager() {
		this.add(new IndexRelation("Assign", "V", 1, "V", 0, "assign"));
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param"));
		this.add(new IndexRelation("return", "V", 1, "V", 0, "return"));
		this.add(new IndexRelation("Store", "V", 2, "V", 0, "store", 1, (short)0));
		this.add(new IndexRelation("Load", "V", 1, "V", 0, "load", 2, (short)0));
		
		this.add(new IndexRelation("Bassign", "V", 1, "M", 0, "Bassign", null, (short)1));
		this.add(new IndexRelation("assignE", "M", 0, "V", 1, "assignE", null, (short)1));
		this.add(new IndexRelation("Flow", "C", 0, "V", 2, "Flow", null, (short)0));
		this.add(new IndexRelation("FlowField", "C", 0, "C", 2, "FlowField", 1, (short)0));
	}
}
