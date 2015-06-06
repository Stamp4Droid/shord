package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;

public class AliasModelsRelationManager extends RelationManager {
	public static class TaintAliasModelsRelationManager extends UnionRelationManager {
		public TaintAliasModelsRelationManager() {
			super(new TaintRelationManager(), new AliasModelsRelationManager());
		}
	}
	
	public AliasModelsRelationManager() {
		this.add(new IndexRelation("Bassign", "V", 1, "M", 0, "Bassign", null, (short)1));
		this.add(new IndexRelation("assignE", "M", 0, "V", 1, "assignE", null, (short)1));
		this.add(new IndexRelation("Flow", "C", 0, "V", 2, "Flow", null, (short)0));
		this.add(new IndexRelation("FlowField", "C", 0, "C", 2, "FlowField", 1, (short)0));
	}
}
