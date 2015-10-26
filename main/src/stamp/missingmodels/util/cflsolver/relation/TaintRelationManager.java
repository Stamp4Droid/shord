package stamp.missingmodels.util.cflsolver.relation;

import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.IndexWithContextRelation;

public class TaintRelationManager extends RelationManager {
	public static class TaintPointsToRelationManager extends UnionRelationManager {	
		public TaintPointsToRelationManager() {
			super(new PointsToRelationManager(), new TaintRelationManager());
		}
	}
	
	public static class TaintPrecomputedPointsToRelationManager extends TaintRelationManager {
		public TaintPrecomputedPointsToRelationManager() {
			this.add(new IndexRelation("ptd", "H", 1, "V", 0, "Flow"));
			this.add(new IndexRelation("fptd", "H", 2, "H", 0, "FlowField", 1, (short)0));
			this.add(new IndexRelation("fptArrd", "H", 1, "H", 0, "FlowFieldArr"));
			
			this.add(new IndexRelation("FrameworkSource", "V", 1, "V", 1, "src2Label"));
			this.add(new IndexRelation("FrameworkSource", "V", 1, "V", 1, "Label2RefT"));
			this.add(new IndexRelation("FrameworkPrimSource", "U", 1, "U", 1, "src2Label"));
			this.add(new IndexRelation("FrameworkPrimSource", "U", 1, "U", 1, "Label2PrimT"));
			this.add(new IndexRelation("FrameworkSink", "V", 1, "V", 1, "sink2Label"));
			this.add(new IndexRelation("FrameworkSink", "V", 1, "V", 1, "SinkF2RefF"));
			this.add(new IndexRelation("FrameworkPrimSink", "V", 1, "V", 1, "sink2Label"));
			this.add(new IndexRelation("FrameworkPrimSink", "V", 1, "V", 1, "SinkF2PrimF"));
		}
	}
	
	public TaintRelationManager() {
		this.add(new IndexRelation("AssignPrim", "U", 1, "U", 0, "assignPrim"));
		
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim"));
		this.add(new IndexRelation("returnPrim", "U", 1, "U", 0, "returnPrim"));
		
		this.add(new IndexRelation("StorePrim", "U", 2, "V", 0, "storePrim", 1, (short)0));
		this.add(new IndexRelation("LoadPrim", "V", 1, "U", 0, "loadPrim", 2, (short)0));
		
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
