package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;

public class MissingRefRefJGrammar extends ContextFreeGrammar {	
	public MissingRefRefJGrammar() {
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

		this.addUnaryProduction("Ref2RefT", "ref2RefArgTStub");
		this.addUnaryProduction("Ref2RefT", "ref2RefRetTStub");

		this.addUnaryProduction("Prim2RefT", "prim2RefArgTStub");
		this.addUnaryProduction("Prim2RefT", "prim2RefRetTStub");
		
		this.addUnaryProduction("Ref2PrimT", "ref2PrimTStub");
		this.addUnaryProduction("Prim2PrimT", "prim2PrimTStub");

		this.addProduction("Fpt", new String[]{"ptG", "Store", "ptG"}, new boolean[]{true, false, false});
		this.addUnaryProduction("Fpt", "fpt");
		this.addUnaryProduction("FptArr", "fptArr");

		this.addBinaryProduction("Obj2RefT", "ptG", "Ref2RefT", true, false);
		this.addBinaryProduction("Obj2PrimT", "ptG", "Ref2PrimT", true, false);
		this.addBinaryProduction("Obj2RefT", "FptArr", "Obj2RefT", true, false);
		this.addBinaryProduction("Obj2PrimT", "FptArr", "Obj2PrimT", true, false);

		this.addBinaryProduction("Label2ObjT", "Label2RefT", "ptG");
		this.addBinaryProduction("Label2ObjT", "Label2ObjT", "Fpt", true);
		
		this.addBinaryProduction("SinkF2Obj", "SinkF2RefF", "ptG");
		this.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Obj", "ptG", "Ref2RefF", "ptG"}, new boolean[]{false, false, true, false, false});
		this.addProduction("SinkF2Obj", new String[]{"sink2Label", "Label2Prim", "Ref2PrimF", "ptG"}, new boolean[]{false, false, true, false});
		this.addBinaryProduction("SinkF2Obj", "SinkF2Obj", "Fpt", true);

		this.addUnaryProduction("SinkF2Prim", "SinkF2PrimF");
		this.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Obj", "ptG", "Prim2RefF"}, new boolean[]{false, false, true, true});
		this.addProduction("SinkF2Prim", new String[]{"sink2Label", "Label2Prim", "Prim2PrimF"}, new boolean[]{false, false, true});

		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2Obj", "SinkF2Obj"}, new boolean[]{false, false, true});
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2Prim", "SinkF2Prim"}, new boolean[]{false, false, true});
		this.addProduction("Src2Sink", new String[]{"src2Label", "Label2PrimFld", "SinkF2Obj"}, new boolean[]{false, false, true}, true);

		this.addUnaryProduction("Label2Obj", "Label2ObjT");
		this.addUnaryProduction("Label2Obj", "Label2ObjX");

		this.addProduction("Label2ObjX", new String[]{"Label2Obj", "Obj2RefT", "ptG"});
		this.addProduction("Label2ObjX", new String[]{"Label2Prim", "Prim2RefT", "ptG"});
		this.addProduction("Label2ObjX", new String[]{"Label2PrimFldArr", "Obj2RefT", "ptG"});
		this.addBinaryProduction("Label2ObjX", "Label2ObjX", "FptArr");

		this.addUnaryProduction("Label2Prim", "Label2PrimT");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "assignPrimCtxt", false, true);
		this.addBinaryProduction("Label2Prim", "Label2Prim", "assignPrimCCtxt", false, true);

		this.addBinaryProduction("Label2Prim", "Label2Obj", "Obj2PrimT");
		this.addBinaryProduction("Label2Prim", "Label2Prim", "Prim2PrimT");

		this.addProduction("Label2Prim", new String[]{"Label2ObjT", "ptG", "loadPrimCtxt"}, new boolean[]{false, true, true}, true);
		this.addProduction("Label2Prim", new String[]{"Label2ObjX", "ptG", "loadPrimCtxtArr"}, new boolean[]{false, true, true});
		this.addBinaryProduction("Label2Prim", "Label2PrimFldArr", "Obj2PrimT");

		this.addProduction("Label2Prim", new String[]{"Label2PrimFld", "ptG", "loadPrimCtxt"}, new boolean[]{false, true, true});
		this.addBinaryProduction("Label2Prim", "Label2PrimFldStat", "loadStatPrimCtxt", false, true);

		this.addProduction("Label2PrimFld", new String[]{"Label2Prim", "storePrimCtxt", "ptG"}, new boolean[]{false, true, false});
		this.addProduction("Label2PrimFldArr", new String[]{"Label2Prim", "storePrimCtxtArr", "ptG"}, new boolean[]{false, true, false});
		this.addBinaryProduction("Label2PrimFldStat", "Label2Prim", "storeStatPrimCtxt", false, true);
	}
}
