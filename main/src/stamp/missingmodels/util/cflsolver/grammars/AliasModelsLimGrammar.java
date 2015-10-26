package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;

public class AliasModelsLimGrammar extends ContextFreeGrammar {
	public static class TaintAliasModelsPointsToGrammar extends UnionGrammar {
		public TaintAliasModelsPointsToGrammar() {
			super(new PointsToGrammar(), new AliasModelsGrammar(), new TaintGrammar());
			this.addUnaryProduction("Flow", "FlowNew");
		}
	}
	
	public static class AliasModelsGrammar extends AliasModelsLimGrammar {
		public AliasModelsGrammar() {
			// (1) A_v rules
			
			// Flow -> Flow Assign
			this.addBinaryProduction("FlowPre", "Flow", "Bassign"); // ***
			
			// Flow -> Flow Store[f] FlowBar Flow Load[f]
			this.addProduction("FlowPre", new String[]{"Flow", "store", "FlowPost"}, new boolean[]{false, false, true}, true); // ***
			this.addBinaryProduction("FlowPre", "FlowField", "FlowPre", true);
			
			// (2) A^v rules
			
			// Flow -> Flow Assign
			this.addUnaryProduction("FlowPost", "assignE");	// ***
			this.addBinaryProduction("FlowPost", "FlowPost", "assign");
			this.addBinaryProduction("FlowPost", "FlowPost", "param");
			this.addBinaryProduction("FlowPost", "FlowPost", "return");
			
			// Flow -> Flow Store[f] FlowBar Flow Load[f]
			this.addBinaryProduction("FlowPost", "FlowPost", "load", true);
			this.addProduction("FlowFieldPost", new String[]{"FlowPost", "store", "Flow"}, new boolean[]{false, false, true});
			this.addProduction("FlowPost", new String[]{"FlowFieldPost", "Flow", "load"});
			this.addProduction("FlowPost", new String[]{"FlowPre", "Flow", "load"}, new boolean[]{true, false, false}, true);
			
			// (3) A^w_v rules
			
			// Flow -> Flow Assign
			this.addBinaryProduction("FlowPrePost", "FlowPost", "Bassign"); // ***
			
			// Flow -> Flow Store[f] FlowBar Flow Load[f]
			this.addBinaryProduction("FlowPrePost", "FlowFieldPost", "FlowPre", true);
			this.addBinaryProduction("FlowPrePost", "FlowPre", "FlowPre", true, false);
			this.addProduction("FlowPrePost", new String[]{"FlowPost", "store", "FlowPost"}, new boolean[]{false, false, true}, true);
		}
	}
	
	public AliasModelsLimGrammar() {
		this.addUnaryProduction("FlowPreFull", "FlowPre");
		this.addBinaryProduction("FlowPreFull", "FlowPreFull", "FlowPrePost");
		this.addBinaryProduction("FlowNew", "FlowPreFull", "assignE");
	}
}
