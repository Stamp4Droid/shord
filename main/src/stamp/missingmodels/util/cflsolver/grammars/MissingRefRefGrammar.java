package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.ContextFreeGrammar;

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
		this.addUnaryProduction("Ref2RefT", "ref2RefArgTStub");
		this.addUnaryProduction("Ref2RefT", "ref2RefRetTStub");

		this.addUnaryProduction("Prim2RefT", "prim2RefArgTStub");
		this.addUnaryProduction("Prim2RefT", "prim2RefRetTStub");
		
		this.addUnaryProduction("Ref2PrimT", "ref2PrimTStub");
		this.addUnaryProduction("Prim2PrimT", "prim2PrimTStub");
		
		//this.addProduction("FlowField", new String[]{"Flow", "store", "Flow"}, new boolean[]{false, false, true});
	}
}
