package stamp.missingmodels.util.cflsolver.grammars;


public class ImplicitFlowGrammar extends TaintGrammar {
	public ImplicitFlowGrammar() {
		super();
		
		this.addBinaryProduction("Obj2RefT", "Flow", "ref2RefImp");
		this.addBinaryProduction("Obj2PrimT", "Flow", "ref2PrimImp");
		
		this.addProduction("Label2ObjX", new String[]{"Label2Prim", "prim2RefImp", "Flow"}, new boolean[]{false, false, true});
		this.addBinaryProduction("Label2Prim", "Label2Prim", "prim2PrimImp");
		
		// debug
		this.addBinaryProduction("Label2Ref", "Label2Obj", "Flow");
		this.addBinaryProduction("Label2PrimFldArrRef", "Label2Prim", "storePrimArr");
		this.addBinaryProduction("Label2PrimFldArrRef2", "Label2PrimFld", "Flow");
	}
}
