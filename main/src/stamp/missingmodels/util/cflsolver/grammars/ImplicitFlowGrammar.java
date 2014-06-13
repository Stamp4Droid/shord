package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;

public class ImplicitFlowGrammar extends TaintPointsToGrammar {
	public ImplicitFlowGrammar() {
		this.addBinaryProduction("Obj2RefT", "Flow", "ref2RefImp");
		this.addBinaryProduction("Obj2PrimT", "Flow", "ref2PrimImp");
		
		this.addProduction("Label2ObjX", new String[]{"Label2Prim", "prim2RefImp", "Flow"}, new boolean[]{false, false, true});
		this.addBinaryProduction("Label2Prim", "Label2Prim", "prim2PrimImp");
	}
}
