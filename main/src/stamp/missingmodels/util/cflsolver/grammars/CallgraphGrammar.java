package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;

public class CallgraphGrammar extends ContextFreeGrammar {
	public CallgraphGrammar() {
		/*
		 * Notes:
		 * - reachableBase = stamp.harness
		 * - callgraph = new base edges to cut
		 * - need to include MV and MU
		 */
		this.addUnaryProduction("Reachable", "reachableBase");
		this.addBinaryProduction("Reachable", "Reachable", "callgraph");
		
		this.addBinaryProduction("ReachableVar", "Reachable", "MV");
		this.addAuxProduction("paramTemp", "paramInput", "ReachableVar", true);
		this.addAuxProduction("param", "paramTemp", "ReachableVar", false, false, true);

		this.addBinaryProduction("ReachablePrim", "Reachable", "MU");
		this.addAuxProduction("paramPrimTemp", "paramPrimInput", "ReachablePrim", true);
		this.addAuxProduction("paramPrim", "paramPrimTemp", "ReachablePrim", false, false, true);
	}
}
