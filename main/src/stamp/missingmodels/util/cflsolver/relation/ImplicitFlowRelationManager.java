package stamp.missingmodels.util.cflsolver.relation;

public class ImplicitFlowRelationManager extends TaintRelationManager {
	public ImplicitFlowRelationManager() {
		this.add(new IndexRelation("Ref2RefImp", "V", 0, "V", 1, "ref2RefImp", 1));
		this.add(new IndexRelation("Ref2PrimImp", "V", 0, "U", 1, "ref2PrimImp", 1));
		this.add(new IndexRelation("Prim2RefImp", "U", 0, "V", 1, "prim2RefImp", 1));
		this.add(new IndexRelation("Prim2PrimImp", "U", 0, "U", 1, "prim2PrimImp", 1));
	}
}
