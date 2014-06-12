package stamp.missingmodels.util.cflsolver.relation;

import shord.analyses.DomM;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.Util.MultivalueMap;

public class DynamicCallgraphRelationManager extends TaintRelationManager {
	private void setNewWeights() {
		this.clearRelationsByName("param");
		this.add(new IndexRelation("param", "V", 1, "V", 0, "paramInput", 2, true));
		
		this.clearRelationsByName("paramPrim");
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrimInput", 2, true));
		
		this.add(new IndexRelation("reachableBase", "M", 0, "M", 0, "reachableBase"));
		this.add(new IndexRelation("callgraph", "M", 0, "M", 1, "callgraph", null, 1));
		this.add(new IndexRelation("MV", "M", 0, "V", 1, "MV"));
		this.add(new IndexRelation("MU", "M", 0, "U", 1, "MU"));
	}
	
	public DynamicCallgraphRelationManager() {
		this.setNewWeights();
	}
	
	public DynamicCallgraphRelationManager(MultivalueMap<String,String> dynamicCallgraph) {
		// STEP 0: Remove param and paramPrim edge productions and add callgraph productions
		this.setNewWeights();
		
		// STEP 1: Build the dynamic callgraph
		final MultivalueMap<String,String> dynamicCallgraphConverted = new MultivalueMap<String,String>();
		
		ProgramRel callgraphRel = (ProgramRel)ClassicProject.g().getTrgt("callgraph");
		DomM domM = (DomM)ClassicProject.g().getTrgt("M");
		callgraphRel.load();
		for(int[] tuple : callgraphRel.getAryNIntTuples()) {
			String caller = domM.get(tuple[0]).toString();
			String callee = domM.get(tuple[1]).toString();
			if(dynamicCallgraph.get(caller).contains(callee)) {
				System.out.println("dynamic callgraph edge: " + caller + " -> " + callee);
				dynamicCallgraphConverted.add("M" + Integer.toString(tuple[0]), "M" + Integer.toString(tuple[1]));
			}
		}
		callgraphRel.close();
				
		// STEP 2: Build the extra relation
		this.add(new IndexRelation("callgraph", "M", 0, "M", 1, "callgraph") {
			@Override
			public boolean filter(int[] tuple) {
				return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
			}
		});
	}
}
