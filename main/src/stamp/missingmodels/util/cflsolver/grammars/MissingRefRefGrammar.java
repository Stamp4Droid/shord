package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;

public class MissingRefRefGrammar extends ContextFreeGrammar {
	public static class MissingRefRefTaintGrammar extends UnionGrammar {
		public MissingRefRefTaintGrammar() {
			super(new TaintGrammar(), new MissingRefRefGrammar());
		}
	}
	
	public static class MissingRefRefImplicitFlowGrammar extends UnionGrammar {
		public MissingRefRefImplicitFlowGrammar() {
			super(new ImplicitFlowGrammar(), new MissingRefRefGrammar());
		}
	}
	
	public MissingRefRefGrammar() {
		this.addBinaryProduction("Obj2RefT", "Flow", "ref2RefArgTStub");
		this.addBinaryProduction("Obj2RefT", "Flow", "ref2RefRetTStub");

		this.addProduction("Label2ObjX", new String[]{"Label2Prim", "prim2RefArgTStub", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Label2ObjX", new String[]{"Label2Prim", "prim2RefArgTStub", "Flow"}, new boolean[]{false, false, true});

		this.addBinaryProduction("Obj2PrimT", "Flow", "ref2PrimTStub");

		this.addBinaryProduction("Label2Prim", "Label2Prim", "prim2PrimTStub");

		this.addProduction("FlowField", new String[]{"Flow", "store", "Flow"}, new boolean[]{false, false, true});
	}
}
