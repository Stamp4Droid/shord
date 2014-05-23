package stamp.missingmodels.util.cflsolver.relation;

public class ImplicitFlowRelationManager extends TaintRelationManager {
	public ImplicitFlowRelationManager() {
		super();
		this.add(new IndexRelation("Ref2RefImp", "V", 1, "V", 2, "ref2RefImp", 1));
		this.add(new IndexRelation("Ref2PrimImp", "V", 1, "U", 2, "ref2PrimImp", 1));
		this.add(new IndexRelation("Prim2RefImp", "U", 1, "V", 2, "prim2RefImp", 1));
		this.add(new IndexRelation("Prim2PrimImp", "U", 1, "U", 2, "prim2PrimImp", 1));
	}
}
