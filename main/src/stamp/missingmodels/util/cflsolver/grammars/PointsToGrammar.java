package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;

public class PointsToGrammar extends ContextFreeGrammar {
	public PointsToGrammar() {
		// pt rules
		this.addUnaryProduction("Flow", "alloc");
		
		this.addBinaryProduction("Flow", "Flow", "assign");
		
		this.addBinaryProduction("Flow", "Flow", "param");
		this.addBinaryProduction("Flow", "Flow", "return");
		
		this.addProduction("FlowField", new String[]{"Flow", "store", "Flow"}, new boolean[]{false, false, true});
		this.addProduction("Flow", new String[]{"FlowField", "Flow", "load"});
		
		this.addBinaryProduction("FlowStatField", "Flow", "storeStat", false, false, false, true);
		this.addBinaryProduction("Flow", "FlowStatField", "loadStat", false, false, false, true);
		
		this.addProduction("FlowFieldArr", new String[]{"Flow", "storeArr", "Flow"}, new boolean[]{false, false, true});
	}
}
