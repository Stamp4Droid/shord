package stamp.missingmodels.util.cflsolver.grammars;

import java.util.List;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;

public class AliasModelsGrammar extends ContextFreeGrammar {
	public static class TaintAliasModelsGrammar extends UnionGrammar {
		public TaintAliasModelsGrammar() {
			super(new AliasModelsGrammar(), new TaintGrammar());
		}
	}
	
	public static class ActiveAliasModelsGrammar extends ContextFreeGrammar {
		public ActiveAliasModelsGrammar() {
			AliasModelsGrammar g = new AliasModelsGrammar();
			for(List<UnaryProduction> ups : g.unaryProductionsByTarget) {
				for(UnaryProduction up : ups) {
					this.addUnaryProduction(up.target.symbol, up.input.symbol, up.isInputBackwards, up.ignoreFields, up.ignoreContexts);
				}
			}
			for(List<BinaryProduction> bps : g.binaryProductionsByTarget) {
				for(BinaryProduction bp : bps) {
					if(bp.firstInput.symbol.equals("Flow") && bp.secondInput.symbol.equals("Bassign")) {
						continue;
					}
					if(bp.firstInput.symbol.equals("FlowPost") && bp.secondInput.symbol.equals("assignE")) {
						continue;
					}
					this.addBinaryProduction(bp.target.symbol, bp.firstInput.symbol, bp.secondInput.symbol, bp.isFirstInputBackwards, bp.isSecondInputBackwards, bp.ignoreFields, bp.ignoreContexts);
				}
			}
			for(List<AuxProduction> aps : g.auxProductionsByTarget) {
				for(AuxProduction ap : aps) {
					this.addAuxProduction(ap.target.symbol, ap.input.symbol, ap.auxInput.symbol, ap.isAuxInputFirst, ap.isInputBackwards, ap.isAuxInputBackwards, ap.ignoreFields, ap.ignoreContexts);
				}
			}
			this.addProduction("FlowPre", new String[]{"activeObject", "Flow", "Bassign"});
			
			this.addProduction("FlowPre", new String[]{"Flow", "Bassign", "ActiveMethod"});
			this.addAuxProduction("activeObject", "objectSelf", "FlowPre", false);
			
			this.addAuxProduction("ActiveMethod", "methodSelf", "FlowPre", false);
			this.addAuxProduction("ActiveMethod", "methodSelf", "FlowPrePost", false);
			this.addBinaryProduction("FlowPost", "ActiveMethod", "assignE");
		}
	}
	
	public static class TaintActiveAliasModelsGrammar extends UnionGrammar {
		public TaintActiveAliasModelsGrammar() {
			super(new ActiveAliasModelsGrammar(), new TaintGrammar());
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
	}
}
