package stamp.analyses;

import shord.analyses.SiteAllocNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.util.AliasModelsUtils;
import stamp.missingmodels.util.processor.AliasModelsProcessor;
import chord.project.Chord;

@Chord(name = "dynamic-points-to-parser-java",
consumes = { "H", "V", },
produces = { "FlowDyn" },
namesOfTypes = {},
types = {},
namesOfSigns = { "FlowDyn" },
signs = { "H0,V0:V0_H0" })
public class DynamicPointsToParserAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		String[] tokens = System.getProperty("stamp.out.dir").split("_");
		String filename = "../../alias_models/alias_models_traces/" + tokens[tokens.length-1] + ".trace";
		
		// STEP 0: Fill in dynamic flow (ret -> app allocation)
		MultivalueMap<Pair<VarNode,Integer>,Pair<SiteAllocNode,Integer>> ptDyn = AliasModelsUtils.getPtDynRetApp(new AliasModelsProcessor(filename));		
		ProgramRel relFlowDyn = (ProgramRel)ClassicProject.g().getTrgt("FlowDyn");
		relFlowDyn.zero();
		for(Pair<VarNode,Integer> varNode : ptDyn.keySet()) {
			for(Pair<SiteAllocNode,Integer> allocNode : ptDyn.get(varNode)) {
				System.out.println("DYNAMIC POINTS TO: " + varNode + " -> " + allocNode);
				if(varNode != null && allocNode != null) {
					relFlowDyn.add(new int[]{allocNode.getY(), varNode.getY()});
				}
			}
		}
		relFlowDyn.save();
	}
}
