package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;

public class TaintGrammar extends ContextFreeGrammar {
	public static class TaintPointsToGrammar extends UnionGrammar {
		public TaintPointsToGrammar() {
			super(new PointsToGrammar(), new TaintGrammar());
		}
	}
	
	public TaintGrammar() {
		this.addUnaryProduction("Label2RefT", "label2RefT");
		this.addUnaryProduction("Label2PrimT", "label2PrimT");

		this.addUnaryProduction("SinkF2RefF", "sinkF2RefF");
		this.addUnaryProduction("SinkF2PrimF", "sinkF2PrimF");

		this.addUnaryProduction("Ref2RefF", "ref2RefF");
		this.addUnaryProduction("Ref2PrimF", "ref2PrimF");
		this.addUnaryProduction("Prim2RefF", "prim2RefF");
		this.addUnaryProduction("Prim2PrimF", "prim2PrimF");

		this.addUnaryProduction("Ref2RefT", "ref2RefT");
		this.addUnaryProduction("Ref2PrimT", "ref2PrimT");
		this.addUnaryProduction("Prim2RefT", "prim2RefT");
		this.addUnaryProduction("Prim2PrimT", "prim2PrimT");
		
		// object annotations
		this.addBinaryProduction("Obj2RefT", "Flow", "Ref2RefT");
		this.addBinaryProduction("Obj2PrimT", "Flow", "Ref2PrimT");
		this.addBinaryProduction("Obj2RefT", "FlowFieldArr", "Obj2RefT");
		this.addBinaryProduction("Obj2PrimT", "FlowFieldArr", "Obj2PrimT");
		
		this.addBinaryProduction("Label2ObjT", "Label2RefT", "Flow", false, true);
		this.addBinaryProduction("Label2ObjT", "Label2ObjT", "FlowField", false, true, true);
		
		// sinkf
		this.addBinaryProduction("SinkF2Obj", "SinkF2RefF", "Flow", false, true);
		this.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Obj", "Flow", "Ref2RefF", "Flow"}, new boolean[]{false, false, false, true, true});
		this.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Prim", "Ref2PrimF", "Flow"}, new boolean[]{false, false, true, true});
		this.addBinaryProduction("SinkF2Obj", "SinkF2Obj", "FieldFlow", false, true, true);
		
		this.addUnaryProduction("SinkF2Prim", "SinkF2PrimF");
		this.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Obj", "Flow", "Prim2RefF"}, new boolean[]{false, false, false, true});
		this.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Prim", "Prim2PrimF"}, new boolean[]{false, false, true});
		
		// source-sink flow
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2Obj", "SinkF2Obj"}, new boolean[]{false, false, true});
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2Prim", "SinkF2Prim"}, new boolean[]{false, false, true});
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2PrimFld", "SinkF2Obj"}, new boolean[]{false, false, true}, true);
		
		// label-obj flow
		this.addUnaryProduction("Label2Obj", "Label2ObjT");
		this.addUnaryProduction("Label2Obj", "Label2ObjX");

		this.addProduction("Label2ObjX", new String[]{"Label2Obj", "Obj2RefT", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Label2ObjX", new String[]{"Label2Prim", "Prim2RefT", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Label2ObjX", new String[]{"Label2PrimFldArr", "Obj2RefT", "Flow"}, new boolean[]{false, false, true});
		this.addBinaryProduction("Label2ObjX", "Label2ObjX", "FlowFieldArr", false, true);
		
		// label-prim flow
		this.addUnaryProduction("Label2Prim", "Label2PrimT");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "assignPrim");
		
		this.addBinaryProduction("Label2Prim", "Label2Prim", "paramPrim");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "returnPrim");

		this.addBinaryProduction("Label2Prim", "Label2Obj", "Obj2PrimT");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "Prim2PrimT");

		this.addProduction("Label2Prim", new String[]{"Label2ObjT", "Flow", "loadPrim"}, true);
		this.addProduction("Label2Prim", new String[]{"Label2ObjX", "Flow", "loadPrimArr"});
		this.addBinaryProduction("Label2Prim", "Label2PrimFldArr", "Obj2PrimT");

		this.addProduction("Label2Prim", new String[]{"Label2PrimFld", "Flow", "loadPrim"});
		this.addBinaryProduction("Label2Prim", "Label2PrimFldStat", "loadStatPrim");
		
		this.addProduction("Label2PrimFld", new String[]{"Label2Prim", "storePrim", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Label2PrimFldArr", new String[]{"Label2Prim", "storePrimArr", "Flow"}, new boolean[]{false, false, true});
		this.addBinaryProduction("Label2PrimFldStat", "Label2Prim", "storeStatPrim");
		
		// debug
		//this.addBinaryProduction("Label2Ref", "Label2Obj", "Flow");
	}
}
