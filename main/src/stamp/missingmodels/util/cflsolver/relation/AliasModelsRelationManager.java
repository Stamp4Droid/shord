package stamp.missingmodels.util.cflsolver.relation;

public class AliasModelsRelationManager extends PointsToRelationManager {
	public static class TaintAliasModelsRelationManager extends UnionRelationManager {	
		public TaintAliasModelsRelationManager() {
			super(new AliasModelsRelationManager(), new TaintRelationManager());
		}
	}
	
	public static class ActiveAliasModelsRelationManager extends AliasModelsRelationManager {
		public ActiveAliasModelsRelationManager() {
			this.add(new IndexRelation("StubParam", "M", 0, "M", 0, "methodSelf", null, (short)0));
			this.add(new IndexRelation("AllocNew", "H", 1, "H", 1, "objectSelf", null, (short)0));
		}
	}
	
	public static class TaintActiveAliasModelsRelationManager extends UnionRelationManager {
		public TaintActiveAliasModelsRelationManager() {
			super(new ActiveAliasModelsRelationManager(), new TaintRelationManager());
		}
	}
	
	public AliasModelsRelationManager() {
		this.add(new IndexRelation("StubParam", "V", 1, "M", 0, "Bassign", null, (short)1));
		this.add(new IndexRelation("StubReturn", "M", 0, "V", 1, "assignE", null, (short)1));
		this.add(new IndexRelation("pt", "C", 2, "V", 1, "Flow", null, (short)0));
		this.add(new IndexRelation("fpt", "C", 2, "C", 0, "FlowField", 1, (short)0));
	}
}
