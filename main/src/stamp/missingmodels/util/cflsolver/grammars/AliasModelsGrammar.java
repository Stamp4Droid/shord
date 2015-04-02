package stamp.missingmodels.util.cflsolver.grammars;

public class AliasModelsGrammar extends PointsToGrammar {
	public AliasModelsGrammar() {
		// pt rules
		this.addBinaryProduction("FlowPre", "Flow", "BAssign");
		this.addUnaryProduction("FlowPost", "AssignE");
	}
}
