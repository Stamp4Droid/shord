package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.jcflsolver2.RelationManager;

public class TaintWithContextRelationManager extends RelationManager {	
	public TaintWithContextRelationManager() {
		this.add(new IndexWithContextRelation("pt", "C", 2, null, "V", 1, 0, "Flow"));
		this.add(new IndexWithContextRelation("fpt", "C", 2, null, "C", 0, null, "FlowField", 1));
		this.add(new IndexWithContextRelation("fptArr", "C", 1, null, "C", 0, null, "FlowFieldArr"));
		
		this.add(new IndexWithContextRelation("AssignPrimCtxt", "U", 2, 0, "U", 1, 0, "assignPrim"));
		this.add(new IndexWithContextRelation("AssignPrimCCtxt", "U", 3, 2, "U", 1, 0, "assignPrim"));
		
		this.add(new IndexWithContextRelation("StorePrimCtxt", "U", 3, 0, "V", 1, 0, "storePrim", 2));
		this.add(new IndexWithContextRelation("LoadPrimCtxt", "V", 2, 0, "U", 1, 0, "loadPrim", 3));
		
		this.add(new IndexWithContextRelation("StoreStatPrimCtxt", "U", 2, 0, "F", 1, null, "storeStatPrim"));
		this.add(new IndexWithContextRelation("LoadStatPrimCtxt", "F", 1, null, "U", 1, 0, "loadStatPrim"));

		this.add(new IndexWithContextRelation("Ref2RefT", "V", 1, 0, "V", 2, 0, "ref2RefT"));
		this.add(new IndexWithContextRelation("Ref2PrimT", "V", 1, 0, "U", 2, 0, "ref2PrimT"));
		this.add(new IndexWithContextRelation("Prim2RefT", "U", 1, 0, "V", 2, 0, "prim2RefT"));
		this.add(new IndexWithContextRelation("Prim2PrimT", "U", 1, 0, "U", 2, 0, "prim2PrimT"));
		
		this.add(new IndexWithContextRelation("Ref2RefF", "V", 1, 0, "V", 2, 0, "ref2RefF"));
		this.add(new IndexWithContextRelation("Ref2PrimF", "V", 1, 0, "U", 2, 0, "ref2PrimF"));
		this.add(new IndexWithContextRelation("Prim2RefF", "U", 1, 0, "V", 2, 0, "prim2RefF"));
		this.add(new IndexWithContextRelation("Prim2PrimF", "U", 1, 0, "U", 2, 0, "prim2PrimF"));
		
		this.add(new IndexWithContextRelation("Label2RefT", "L", 1, null, "V", 2, 0, "label2RefT"));
		this.add(new IndexWithContextRelation("Label2PrimT", "L", 1, null, "U", 2, 0, "label2PrimT"));
		
		this.add(new IndexWithContextRelation("SinkF2RefF", "L", 1, null, "V", 2, 0, "sinkF2RefF"));
		this.add(new IndexWithContextRelation("SinkF2PrimF", "L", 1, null, "U", 2, 0, "sinkF2PrimF"));

		this.add(new IndexWithContextRelation("Src2Label", "L", 0, null, "L", 0, null, "src2Label"));
		this.add(new IndexWithContextRelation("Sink2Label", "L", 0, null, "L", 0, null, "sink2Label"));
		
		this.add(new IndexWithContextRelation("StoreCtxtArr", "V", 2, 0, "V", 1, 0, "storeArr"));
		this.add(new IndexWithContextRelation("LoadCtxtArr", "V", 2, 0, "V", 1, 0, "loadArr"));

		this.add(new IndexWithContextRelation("StorePrimCtxtArr", "U", 2, 0, "V", 1, 0, "storePrimArr"));
		this.add(new IndexWithContextRelation("LoadPrimCtxtArr", "V", 2, 0, "U", 1, 0, "loadPrimArr"));
	}
}
