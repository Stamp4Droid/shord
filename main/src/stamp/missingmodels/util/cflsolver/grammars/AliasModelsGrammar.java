package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;

public class AliasModelsGrammar extends ContextFreeGrammar {
	public static class TaintAliasModelsPointsToGrammar extends UnionGrammar {
		public TaintAliasModelsPointsToGrammar() {
			super(new PointsToGrammar(), new AliasModelsGrammar(), new TaintGrammar());
			this.addUnaryProduction("Flow", "FlowNew");
		}
	}
	
	public AliasModelsGrammar() {
		// (1) A_v rules
		
		// Flow -> Flow Assign
		this.addBinaryProduction("FlowPre", "Flow", "Bassign"); // ***
		
		// Flow -> Flow Store[f] FlowBar Flow Load[f]
		this.addProduction("FlowPre", new String[]{"Flow", "store", "FlowPost"}, new boolean[]{false, false, true}, true); // ***
		
		// Flow ->  FlowField Flow Load[f]
		this.addBinaryProduction("FlowPre", "FlowField", "FlowPre", true);
		
		// (2) A^v rules
		
		// Flow -> Flow Assign
		this.addUnaryProduction("FlowPost", "assignE");	// ***
		this.addBinaryProduction("FlowPost", "FlowPost", "assign");
		this.addBinaryProduction("FlowPost", "FlowPost", "param");
		this.addBinaryProduction("FlowPost", "FlowPost", "return");
		
		// Flow -> Flow Store[f] FlowBar Flow Load[f]
		this.addBinaryProduction("FlowPost", "FlowPost", "load", true);
		
		// FlowField -> Flow Store[f] FlowBar
		this.addProduction("FlowFieldPost", new String[]{"FlowPost", "store", "Flow"}, new boolean[]{false, false, true});
		this.addUnaryProduction("FlowFieldAnyPost", "FlowPre", true);
		
		// Flow -> FlowField Flow Load[f]
		this.addProduction("FlowPost", new String[]{"FlowFieldPost", "Flow", "load"});
		this.addProduction("FlowPost", new String[]{"FlowFieldAnyPost", "Flow", "load"}, true);
		
		// (3) A^w_v rules
		
		// Flow -> Flow Assign
		this.addBinaryProduction("FlowPrePost", "FlowPost", "Bassign"); // ***
		
		// Flow -> FlowField Flow Load[f]
		this.addBinaryProduction("FlowPrePost", "FlowFieldPost", "FlowPre", true);
		this.addBinaryProduction("FlowPrePost", "FlowFieldAnyPost", "FlowPre");
		
		// Flow -> Flow Store[f] FlowBar Flow Load[f]
		this.addUnaryProduction("FlowPrePost", "FlowPrePost", true);
		this.addProduction("FlowPrePost", new String[]{"FlowPost", "store", "FlowPost"}, new boolean[]{false, false, true}, true);

		// FLowField -> Flow Store[f] FlowBar
		this.addBinaryProduction("FlowFieldAnyPost", "FlowPrePost", "FlowPre", false, true);
		
		// (4) Stitching rules
		
		// (4i) A rules
		
		// Flow -> Flow Assign
		this.addBinaryProduction("FlowNew", "FlowPre", "assignE");
		
		// Flow -> Flow Store[f] FlowBar Flow Load[f]
		this.addProduction("FlowNew", new String[]{"FlowPre", "FlowPost", "load"}, true);
		
		// FlowField -> Flow Store[f] FlowBar
		this.addBinaryProduction("FlowFieldAny", "FlowPre", "FlowPre", false, true);
		
		// (4ii) A_v rules
		
		// Flow -> Flow Store[f] FlowBar Flow Load[f]
		this.addBinaryProduction("FlowPre", "FlowPre", "FlowPrePost", false, true);
		
		// (4iii) A^v rules
		
		// Flow -> Flow Assign
		this.addBinaryProduction("FlowPost", "FlowPrePost", "assignE");
		
		// Flow -> Flow Store[f] FlowBar Flow Load[f]
		this.addProduction("FlowPost", new String[]{"FlowPrePost", "FlowPost", "load"}, true);
		
		// (4iv) A^w_v rules
		
		// Flow -> Flow Store[f] FlowBar Flow Load[f]
		this.addBinaryProduction("FlowPrePost", "FlowPrePost", "FlowPrePost", false, true);
		
		// semantics for FlowFieldAny
		this.addProduction("FlowNew", new String[]{"FlowFieldAny", "Flow", "load"}, true);
		
		// Traverse return edge
		this.addBinaryProduction("FlowFinal", "FlowNew", "return");
	}
}
