package stamp.missingmodels.util.cflsolver.grammars;

public class DebugTaintGrammar extends TaintGrammar {
	public DebugTaintGrammar() {
		this.addBinaryProduction("Label2Ref", "Label2Obj", "Flow");
	}
}
