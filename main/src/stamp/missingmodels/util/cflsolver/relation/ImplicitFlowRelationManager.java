package stamp.missingmodels.util.cflsolver.relation;

public class ImplicitFlowRelationManager extends TaintWithContextRelationManager {
	public ImplicitFlowRelationManager() {
		this.add(new IndexWithContextRelation("Ref2RefImpCtxt", "V", 1, 0, "V", 2, 0, "ref2RefImp", null, (short)1));
		this.add(new IndexWithContextRelation("Ref2PrimImpCtxt", "V", 1, 0, "U", 2, 0, "ref2PrimImp", null, (short)1));
		this.add(new IndexWithContextRelation("Prim2RefImpCtxt", "U", 1, 0, "V", 2, 0, "prim2RefImp", null, (short)1));
		this.add(new IndexWithContextRelation("Prim2PrimImpCtxt", "U", 1, 0, "U", 2, 0, "prim2PrimImp", null, (short)1));
		
		// Exception handling
		this.add(new IndexWithContextRelation("Ref2RefImpCCtxt", "V", 1, 0, "V", 3, 2, "ref2RefImp", null, (short)1));
		this.add(new IndexWithContextRelation("Ref2PrimImpCCtxt", "V", 1, 0, "U", 3, 2, "ref2PrimImp", null, (short)1));
		this.add(new IndexWithContextRelation("Prim2RefImpCCtxt", "U", 1, 0, "V", 3, 2, "prim2RefImp", null, (short)1));
		this.add(new IndexWithContextRelation("Prim2PrimImpCCtxt", "U", 1, 0, "U", 3, 2, "prim2PrimImp", null, (short)1));
	}	
}
