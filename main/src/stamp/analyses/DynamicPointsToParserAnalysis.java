package stamp.analyses;

import shord.analyses.SiteAllocNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.SootMethod;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.AliasModelsUtils;
import stamp.missingmodels.util.processor.AliasModelsTraceReader;
import chord.project.Chord;

@Chord(name = "dynamic-points-to-parser-java",
consumes = { "H", "V", "M", },
produces = { "FlowDyn", "PhantomObjectDyn", },
namesOfTypes = {},
types = {},
namesOfSigns = { "FlowDyn", "PhantomObjectDyn", },
signs = { "H0,V0:V0_H0", "M0:M0" })
public class DynamicPointsToParserAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		String[] tokens = System.getProperty("stamp.out.dir").split("_");
		String filename = "../../alias_models/alias_models_traces/" + tokens[tokens.length-1] + ".trace";
		AliasModelsTraceReader processor = new AliasModelsTraceReader(filename);
		
		// STEP 0: Fill in dynamic flow (ret -> app allocation)
		MultivalueMap<VarNode,SiteAllocNode> ptDyn = AliasModelsUtils.ProcessorUtils.getPtDynRetApp(processor);		
		ProgramRel relFlowDyn = (ProgramRel)ClassicProject.g().getTrgt("FlowDyn");
		relFlowDyn.zero();
		for(VarNode varNode : ptDyn.keySet()) {
			for(SiteAllocNode allocNode : ptDyn.get(varNode)) {
				System.out.println("DYNAMIC POINTS TO: " + varNode + " -> " + allocNode);
				try {
					relFlowDyn.add(allocNode, varNode);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		relFlowDyn.save();
		
		// STEP 1: Fill in dynamic phantom objects (ret -> framework allocation)
		MultivalueMap<VarNode,SootMethod> phantomObjectDyn = AliasModelsUtils.ProcessorUtils.getPhantomObjectDyn(processor);
		ProgramRel relPhantomObjectDyn = (ProgramRel)ClassicProject.g().getTrgt("PhantomObjectDyn");
		relPhantomObjectDyn.zero();
		for(VarNode varNode : phantomObjectDyn.keySet()) {
			for(SootMethod method : phantomObjectDyn.get(varNode)) {
				relPhantomObjectDyn.add(method);
			}
		}
		relPhantomObjectDyn.save();
	}
}
