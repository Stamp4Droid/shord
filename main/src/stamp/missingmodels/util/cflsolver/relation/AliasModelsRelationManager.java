package stamp.missingmodels.util.cflsolver.relation;

public class AliasModelsRelationManager extends PointsToRelationManager {
	public AliasModelsRelationManager() {
		this.add(new IndexRelation("StubParam", "V", 1, "M", 0, "BAssign", null, (short)1));
		this.add(new IndexRelation("StubReturn", "M", 0, "V", 1, "AssignE", null, (short)1));
	}	
}
