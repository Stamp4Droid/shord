package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;

public class AliasModelsShortLimGrammar extends ContextFreeGrammar {
	public static class TaintAliasModelsPointsToGrammar extends UnionGrammar {
		public TaintAliasModelsPointsToGrammar() {
			super(new PointsToGrammar(), new AliasModelsShortGrammar(), new TaintGrammar());
			this.addUnaryProduction("Flow", "FlowNew");
		}
	}
	
	public static class AliasModelsShortGrammar extends AliasModelsShortLimGrammar {
		public AliasModelsShortGrammar() {
			// (1) A_v rules
			
			// Flow -> Flow Assign
			this.addBinaryProduction("FlowPre0", "Flow", "Bassign"); // ***
			
			// Flow -> Flow Store[f] FlowBar Flow Load[f]
			this.addProduction("FlowPre1", new String[]{"Flow", "store", "FlowPost0"}, new boolean[]{false, false, true}, true); // ***
			this.addBinaryProduction("FlowPre1", "FlowField", "FlowPre0", true);
			
			// (2) A^v rules
			
			// Flow -> Flow Assign
			this.addUnaryProduction("FlowPost0", "assignE");	// ***
			this.addBinaryProduction("FlowPost0", "FlowPost0", "assign");
			this.addBinaryProduction("FlowPost1", "FlowPost1", "assign");
			this.addBinaryProduction("FlowPost0", "FlowPost0", "param");
			this.addBinaryProduction("FlowPost1", "FlowPost1", "param");
			this.addBinaryProduction("FlowPost0", "FlowPost0", "return");
			this.addBinaryProduction("FlowPost1", "FlowPost1", "return");
			
			// Flow -> Flow Store[f] FlowBar Flow Load[f]
			this.addBinaryProduction("FlowPost1", "FlowPost0", "load", true);
			this.addProduction("FlowFieldPost0", new String[]{"FlowPost0", "store", "Flow"}, new boolean[]{false, false, true});
			this.addProduction("FlowFieldPost1", new String[]{"FlowPost1", "store", "Flow"}, new boolean[]{false, false, true});
			this.addProduction("FlowPost0", new String[]{"FlowFieldPost0", "Flow", "load"});
			this.addProduction("FlowPost1", new String[]{"FlowFieldPost1", "Flow", "load"});
			this.addProduction("FlowPost1", new String[]{"FlowPre0", "Flow", "load"}, new boolean[]{true, false, false}, true);
			
			// (3) A^w_v rules
			
			// Flow -> Flow Assign
			
			// First index refers of PrePost to "post", second to "pre"
			this.addBinaryProduction("FlowPrePost00", "FlowPost0", "Bassign"); // ***
			this.addBinaryProduction("FlowPrePost10", "FlowPost1", "Bassign"); // ***
			
			// Flow -> Flow Store[f] FlowBar Flow Load[f]
			this.addBinaryProduction("FlowPrePost01", "FlowFieldPost0", "FlowPre0", true);
			this.addBinaryProduction("FlowPrePost11", "FlowFieldPost1", "FlowPre0", true);
			this.addBinaryProduction("FlowPrePost11", "FlowPre0", "FlowPre0", true, false);
			this.addProduction("FlowPrePost01", new String[]{"FlowPost0", "store", "FlowPost0"}, new boolean[]{false, false, true}, true);
			this.addProduction("FlowPrePost11", new String[]{"FlowPost1", "store", "FlowPost0"}, new boolean[]{false, false, true}, true);
		}
	}
	
	public AliasModelsShortLimGrammar() {
		this.addUnaryProduction("FlowPreFull0", "FlowPre0");
		this.addUnaryProduction("FlowPreFull1", "FlowPre1");
		
		this.addBinaryProduction("FlowPreFull0", "FlowPreFull0", "FlowPrePost00");
		this.addBinaryProduction("FlowPreFull0", "FlowPreFull1", "FlowPrePost00");
		this.addBinaryProduction("FlowPreFull0", "FlowPreFull0", "FlowPrePost10");
		this.addBinaryProduction("FlowPreFull1", "FlowPreFull0", "FlowPrePost01");
		this.addBinaryProduction("FlowPreFull1", "FlowPreFull1", "FlowPrePost01");
		this.addBinaryProduction("FlowPreFull1", "FlowPreFull0", "FlowPrePost11");
		
		this.addBinaryProduction("FlowNew", "FlowPreFull0", "assignE");
		this.addBinaryProduction("FlowNew", "FlowPreFull1", "assignE");
	}
}
