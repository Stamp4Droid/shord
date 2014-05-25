package stamp.missingmodels.util.cflsolver.relation;

public class TaintRelationManager extends PointsToRelationManager {	
	public TaintRelationManager() {		
		this.add(new IndexRelation("AssignPrim", "U", 1, "U", 0, "assignPrim"));
		
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", 2, true));
		this.add(new IndexRelation("returnPrim", "U", 1, "U", 0, "returnPrim", 2, false));
		
		this.add(new IndexRelation("StorePrim", "U", 2, "V", 0, "storePrim", 1));
		this.add(new IndexRelation("LoadPrim", "V", 1, "U", 0, "loadPrim", 2));
		
		this.add(new IndexRelation("StoreStatPrim", "U", 1, "F", 0, "storeStatPrim"));
		this.add(new IndexRelation("LoadStatPrim", "F", 1, "U", 0, "loadStatPrim"));

		this.add(new IndexRelation("Ref2RefT", "V", 1, "V", 2, "ref2RefT"));
		this.add(new IndexRelation("Ref2PrimT", "V", 1, "U", 2, "ref2PrimT"));
		this.add(new IndexRelation("Prim2RefT", "U", 1, "V", 2, "prim2RefT"));
		this.add(new IndexRelation("Prim2PrimT", "U", 1, "U", 2, "prim2PrimT"));
		
		this.add(new IndexRelation("Ref2RefF", "V", 1, "V", 2, "ref2RefF"));
		this.add(new IndexRelation("Ref2PrimF", "V", 1, "U", 2, "ref2PrimF"));
		this.add(new IndexRelation("Prim2RefF", "U", 1, "V", 2, "prim2RefF"));
		this.add(new IndexRelation("Prim2PrimF", "U", 1, "U", 2, "prim2PrimF"));
		
		this.add(new IndexRelation("Label2RefT", "L", 1, "V", 2, "label2RefT"));
		this.add(new IndexRelation("Label2PrimT", "L", 1, "U", 2, "label2PrimT"));
		
		this.add(new IndexRelation("SinkF2RefF", "L", 1, "V", 2, "sinkF2RefF"));
		this.add(new IndexRelation("SinkF2PrimF", "L", 1, "U", 2, "sinkF2PrimF"));

		this.add(new IndexRelation("Src2Label", "L", 0, "L", 0, "src2Label"));
		this.add(new IndexRelation("Sink2Label", "L", 0, "L", 0, "sink2Label"));
		
		this.add(new IndexRelation("StoreArr", "V", 1, "V", 0, "storeArr"));
		this.add(new IndexRelation("LoadArr", "V", 1, "V", 0, "loadArr"));

		this.add(new IndexRelation("StorePrimArr", "U", 1, "V", 0, "storePrimArr"));
		this.add(new IndexRelation("LoadPrimArr", "V", 1, "U", 0, "loadPrimArr"));
	}
}
