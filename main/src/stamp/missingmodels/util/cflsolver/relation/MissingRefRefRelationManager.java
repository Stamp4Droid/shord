package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPointsToRelationManager;

public class MissingRefRefRelationManager extends TaintPointsToRelationManager {
	public MissingRefRefRelationManager() {
		this.add(new IndexRelation("Ref2RefT", "V", 1, "V", 2, "ref2RefArgTStub", 1));
		this.add(new IndexRelation("Ref2RefT", "V", 1, "V", 2, "ref2RefRetTStub", 1));
		this.add(new IndexRelation("Prim2RefT", "U", 1, "V", 2, "prim2RefArgTStub", 1));
		this.add(new IndexRelation("Prim2RefT", "U", 1, "V", 2, "prim2RefRetTStub", 1));
		this.add(new IndexRelation("Ref2PrimT", "V", 1, "U", 2, "ref2PrimStubT", 1));
		this.add(new IndexRelation("Prim2PrimT", "U", 1, "U", 2, "prim2PrimTStub", 1));
	}
}
