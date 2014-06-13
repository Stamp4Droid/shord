package stamp.missingmodels.util.cflsolver.relation;

public class ImplicitFlowRelationManager extends TaintWithContextRelationManager {
	public ImplicitFlowRelationManager() {
		this.add(new IndexWithContextRelation("Ref2RefImpCtxt", "V", 1, 0, "V", 2, 0, "ref2RefImp", null, 1));
		this.add(new IndexWithContextRelation("Ref2PrimImp", "V", 1, 0, "U", 2, 0, "ref2PrimImp", null, 1));
		this.add(new IndexWithContextRelation("Prim2RefImp", "U", 1, 0, "V", 2, 0, "prim2RefImp", null, 1));
		this.add(new IndexWithContextRelation("Prim2PrimImp", "U", 1, 0, "U", 2, 0, "prim2PrimImp", null, 1));
	}	
}
