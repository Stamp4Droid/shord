package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.jcflsolver2.RelationManager;

public class MissingRefRefRelationManager extends RelationManager {
	public static class MissingRefRefTaintRelationManager extends UnionRelationManager {
		public MissingRefRefTaintRelationManager() {
			super(new MissingRefRefRelationManager(), new TaintWithContextRelationManager());
		}
	}
	
	public static class MissingRefRefImplicitFlowRelationManager extends UnionRelationManager {
		public MissingRefRefImplicitFlowRelationManager() {
			super(new MissingRefRefRelationManager(), new ImplicitFlowRelationManager());
		}
	}
	
	public MissingRefRefRelationManager() {
		this.add(new IndexWithContextRelation("Ref2RefArgTStub", "V", 1, 0, "V", 2, 0, "ref2RefArgTStub", 1));
		this.add(new IndexWithContextRelation("Ref2RefRetTStub", "V", 1, 0, "V", 2, 0, "ref2RefRetTStub", 1));
		this.add(new IndexWithContextRelation("Prim2RefArgTStub", "U", 1, 0, "V", 2, 0, "prim2RefArgTStub", 1));
		this.add(new IndexWithContextRelation("Prim2RefRetTStub", "U", 1, 0, "V", 2, 0, "prim2RefRetTStub", 1));
		this.add(new IndexWithContextRelation("Ref2PrimTStub", "V", 1, 0, "U", 2, 0, "ref2PrimTStub", 1));
		this.add(new IndexWithContextRelation("Prim2PrimTStub", "U", 1, 0, "U", 2, 0, "prim2PrimTStub", 1));
		
		this.add(new IndexWithContextRelation("phpt", "V", 2, 3, "V", 1, 0, "Flow"));
		this.add(new IndexWithContextRelation("fptph", "V", 2, 3, "C", 0, null, "FlowField", 1));
		this.add(new IndexWithContextRelation("fphpt", "C", 3, null, "V", 0, 1, "FlowField", 2));
		this.add(new IndexWithContextRelation("fphptph", "V", 3, 4, "V", 0, 1, "FlowField", 2));
		
		// TODO: do we need phantom fptArr?
	}
}
