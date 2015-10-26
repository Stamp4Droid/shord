package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPointsToRelationManager;

public class CallgraphRelationManager extends TaintPointsToRelationManager {
	public CallgraphRelationManager() {
		this.clearRelationsByName("param");
		this.add(new IndexRelation("param", "V", 1, "V", 0, "paramInput"));
		
		this.clearRelationsByName("paramPrim");
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrimInput"));
		
		this.add(new IndexRelation("reachableBase", "M", 0, "M", 0, "reachableBase"));
		this.add(new IndexRelation("callgraph", "M", 0, "M", 1, "callgraph", null, (short)1));
		this.add(new IndexRelation("MV", "M", 0, "V", 1, "MV"));
		this.add(new IndexRelation("MU", "M", 0, "U", 1, "MU"));
	}
}
