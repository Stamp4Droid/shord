package stamp.missingmodels.util.cflsolver.relation;

public class UnionRelationManager extends RelationManager {
	private void addRelationManager(RelationManager relationManager) {
		for(String name : relationManager.getRelationNames()) {
			for(Relation relation : relationManager.getRelationsByName(name)) {
				this.add(relation);
			}
		}
	}
	
	public UnionRelationManager(RelationManager ... relationManagers) {
		for(RelationManager relationManager : relationManagers) {
			this.addRelationManager(relationManager);
		}
	}
}