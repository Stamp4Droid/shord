package stamp.missingmodels.util.cflsolver.util;

import java.util.ArrayList;
import java.util.List;

import shord.analyses.DomM;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;

public class ShordUtils {
	public static int getNumReachableMethods() {
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("reachableBase");
		relReachableM.load();
		int numReachableMethods = relReachableM.size();
		relReachableM.close();
		return numReachableMethods;
	}
	
	public static MultivalueMap<String,String> getStaticCallgraphReverse() {
		MultivalueMap<String,String> staticCallgraphReverse = new MultivalueMap<String,String>();
		ProgramRel relCallgraph = (ProgramRel)ClassicProject.g().getTrgt("callgraph");
		DomM domM = (DomM)ClassicProject.g().getTrgt("M");
		relCallgraph.load();
		for(int[] tuple : relCallgraph.getAryNIntTuples()) {
			String caller = domM.get(tuple[0]).toString();
			String callee = domM.get(tuple[1]).toString();
			staticCallgraphReverse.add(callee, caller);
		}
		relCallgraph.close();
		return staticCallgraphReverse;
	}
	
	// filtered list of reachable methods (methodList \cap reachableBase)
	public static List<String> getReachableMethods(List<String> methodList) {		
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("reachableBase");
		relReachableM.load();
		List<String> result = new ArrayList<String>();
		for(Object tuple : relReachableM.getAry1ValTuples()) {
			if(methodList.contains(tuple.toString())) {
				// reachable method is dynamically reached
				result.add(tuple.toString());
			}
		}
		relReachableM.close();
		return result;
	}
}
