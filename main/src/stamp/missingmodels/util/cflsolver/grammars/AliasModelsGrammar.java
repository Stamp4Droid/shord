package stamp.missingmodels.util.cflsolver.grammars;

public class AliasModelsGrammar extends PointsToGrammar {
	public AliasModelsGrammar() {
		// (1) A_v rules
		this.addBinaryProduction("FlowPre", "Flow", "Bassign");
		this.addUnaryProduction("FlowPre", "FlowFieldAnyPre");
		this.addBinaryProduction("FlowPre", "FlowField", "FlowPre", true);
		this.addUnaryProduction("FlowFieldAnyPre", "FlowPre");
		this.addProduction("FlowFieldAnyPre", new String[]{"Flow", "store", "FlowPost"}, new boolean[]{false, false, true}, true);
		
		// (2) A^v rules
		this.addUnaryProduction("FlowPost", "assignE");
		this.addBinaryProduction("FlowPost", "FlowPost", "assign");
		this.addBinaryProduction("FlowPost", "FlowPost", "param");
		this.addBinaryProduction("FlowPost", "FlowPost", "return");
		this.addBinaryProduction("FlowPost", "FlowPost", "load", true);
		this.addProduction("FlowPost", new String[]{"FlowFieldPost", "Flow", "load"});
		this.addProduction("FlowPost", new String[]{"FlowFieldAnyPost", "Flow", "load"}, true);
		this.addUnaryProduction("FlowFieldAnyPost", "FlowPre", true);
		this.addProduction("FlowFieldPost", new String[]{"FlowPost", "store", "Flow"}, new boolean[]{false, false, true});
		
		// (3) A^w_v rules
		this.addBinaryProduction("FlowPrePost", "FlowPost", "Bassign");
		this.addUnaryProduction("FlowPrePost", "FlowFieldAnyPrePost");
		this.addBinaryProduction("FlowPrePost", "FlowFieldPost", "FlowPre", true);
		this.addBinaryProduction("FlowPrePost", "FlowFieldAnyPost", "FlowPre");
		this.addUnaryProduction("FlowFieldAnyPrePost", "FlowPrePost");
		this.addUnaryProduction("FlowFieldAnyPrePost", "FlowPrePost", true); // "true" is for backwards input
		this.addProduction("FlowFieldAnyPrePost", new String[]{"FlowPost", "store", "FlowPost"}, new boolean[]{false, false, true}, true);
		
		// (4) A rules
		this.addBinaryProduction("Flow", "FlowPre", "assignE");
		this.addProduction("Flow", new String[]{"FlowFieldAnyPre", "FlowPost", "load"}, true);
		this.addBinaryProduction("FlowFieldAny", "FlowPre", "FlowPre", false, true);
		
		this.addBinaryProduction("FlowFieldAnyPre", "FlowPre", "FlowPrePost", false, true);
		
		this.addBinaryProduction("FlowFieldAnyPrePost", "FlowPrePost", "FlowPrePost", false, true);
		
		this.addBinaryProduction("FlowPost", "FlowPrePost", "assignE");
		this.addProduction("FlowPost", new String[]{"FlowFieldAnyPrePost", "FlowPost", "load"}, true);
		this.addBinaryProduction("FlowFieldAnyPost", "FlowPrePost", "FlowPre", false, true);
		
		// semantics for FlowFieldAny
		this.addProduction("Flow", new String[]{"FlowFieldAny", "Flow", "load"}, true);
	}
}
