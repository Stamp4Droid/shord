package stamp.missingmodels.util.cflsolver.grammars;

public class CallgraphTaintGrammar extends UnionGrammar {
	public CallgraphTaintGrammar() {
		super(new CallgraphGrammar(), new TaintGrammar());
	}
}
