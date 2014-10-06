package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;

public class CallgraphTaintGrammar extends UnionGrammar {
	public CallgraphTaintGrammar() {
		super(new CallgraphGrammar(), new TaintPointsToGrammar());
	}
}
