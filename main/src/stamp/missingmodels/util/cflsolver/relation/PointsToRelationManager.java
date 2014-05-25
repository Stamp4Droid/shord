package stamp.missingmodels.util.cflsolver.relation;

public class PointsToRelationManager extends RelationManager {
	public PointsToRelationManager() {
		this.add(new IndexRelation("AllocNew", "H", 1, "V", 0, "alloc"));
		
		this.add(new IndexRelation("Assign", "V", 1, "V", 0, "assign"));
		
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param", 2, true));
		this.add(new IndexRelation("return", "V", 1, "V", 0, "return", 2, false));
	
		this.add(new IndexRelation("Store", "V", 2, "V", 0, "store", 1));
		this.add(new IndexRelation("Load", "V", 1, "V", 0, "load", 2));
		
		this.add(new IndexRelation("StoreStat", "V", 1, "F", 0, "storeStat"));
		this.add(new IndexRelation("LoadStat", "F", 1, "V", 0, "loadStat"));
	}
}
