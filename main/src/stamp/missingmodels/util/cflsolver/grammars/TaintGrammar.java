package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;

public class TaintGrammar extends ContextFreeGrammar {
	public TaintGrammar() {
		// pt rules
		this.addUnaryProduction("Flow", "alloc");
		
		this.addBinaryProduction("Flow", "Flow", "assign");
		
		this.addBinaryProduction("Flow", "Flow", "param");
		this.addBinaryProduction("Flow", "Flow", "return");
		
		this.addProduction("FlowField", new String[]{"Flow", "store", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Flow", new String[]{"FlowField", "Flow", "load"});
		
		this.addBinaryProduction("FlowStatField", "Flow", "storeStat");
		this.addBinaryProduction("Flow", "FlowStatField", "loadStat");
		
		this.addProduction("FlowFieldArr", new String[]{"Flow", "storeArr", "Flow"}, new boolean[]{false, false, true});

		// object annotations
		this.addBinaryProduction("Obj2RefT", "Flow", "ref2RefT");
		this.addBinaryProduction("Obj2PrimT", "Flow", "ref2PrimT");
		this.addBinaryProduction("Obj2RefT", "FlowFieldArr", "Obj2RefT");
		this.addBinaryProduction("Obj2PrimT", "FlowFieldArr", "Obj2PrimT");
		
		this.addBinaryProduction("Label2ObjT", "label2RefT", "Flow", false, true);
		this.addBinaryProduction("Label2ObjT", "Label2ObjT", "FlowField", false, true, true);
		
		// sinkf
		this.addBinaryProduction("SinkF2Obj", "sinkF2RefF", "Flow", false, true);
		this.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Obj", "Flow", "ref2RefF", "Flow"}, new boolean[]{false, false, false, true, true});
		this.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Prim", "ref2PrimF", "Flow"}, new boolean[]{false, false, true, true});
		this.addBinaryProduction("SinkF2Obj", "SinkF2Obj", "FieldFlow", false, true, true);
		
		this.addUnaryProduction("SinkF2Prim", "sinkF2PrimF");
		this.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Obj", "Flow", "prim2RefF"}, new boolean[]{false, false, false, true});
		this.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Prim", "prim2PrimF"}, new boolean[]{false, false, true});
		
		// source-sink flow
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2Obj", "SinkF2Obj"}, new boolean[]{false, false, true});
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2Prim", "SinkF2Prim"}, new boolean[]{false, false, true});
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2PrimFld", "SinkF2Obj"}, new boolean[]{false, false, true}, true);
		
		// label-obj flow
		this.addUnaryProduction("Label2Obj", "Label2ObjT");
		this.addUnaryProduction("Label2Obj", "Label2ObjX");

		this.addProduction("Label2ObjX", new String[]{"Label2Obj", "Obj2RefT", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Label2ObjX", new String[]{"Label2Prim", "prim2RefT", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Label2ObjX", new String[]{"Label2PrimFldArr", "Obj2RefT", "Flow"}, new boolean[]{false, false, true});
		this.addBinaryProduction("Label2ObjX", "Label2ObjX", "FlowFieldArr", false, true);
		
		// label-prim flow
		this.addUnaryProduction("Label2Prim", "label2PrimT");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "assignPrim");
		
		this.addBinaryProduction("Label2Prim", "Label2Prim", "paramPrim");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "returnPrim");

		this.addBinaryProduction("Label2Prim", "Label2Obj", "Obj2PrimT");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "prim2PrimT");

		this.addProduction("Label2Prim", new String[]{"Label2ObjT", "Flow", "loadPrim"}, true);
		this.addProduction("Label2Prim", new String[]{"Label2ObjX", "Flow", "loadPrim"});
		this.addBinaryProduction("Label2Prim", "Label2PrimFldArr", "Obj2PrimT");

		this.addProduction("Label2Prim", new String[]{"Label2PrimFld", "Flow", "loadPrim"});
		this.addBinaryProduction("Label2Prim", "Label2PrimFldStat", "loadStatPrim");
		
		this.addProduction("Label2PrimFld", new String[]{"Label2Prim", "storePrim", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Label2PrimFldArr", new String[]{"Label2Prim", "storePrimArr", "Flow"}, new boolean[]{false, false, true});
		this.addBinaryProduction("Label2PrimFldStat", "Label2Prim", "storeStatPrim");
	}
}
