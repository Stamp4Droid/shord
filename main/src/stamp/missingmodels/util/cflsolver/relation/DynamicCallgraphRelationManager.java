package stamp.missingmodels.util.cflsolver.relation;

import java.util.List;

import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;

public class DynamicCallgraphRelationManager extends TaintRelationManager {
	private void setNewWeights() {
		this.clearRelationsByName("param");
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param", 2, true, 1));
		
		this.clearRelationsByName("paramPrim");
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", 2, true, 1));
	}
	
	public DynamicCallgraphRelationManager() {
		super();
		this.setNewWeights();
	}
	
	public DynamicCallgraphRelationManager(List<Pair<String,String>> dynamicCallgraphList) {
		this(dynamicCallgraphList, -1);
	}
	
	public DynamicCallgraphRelationManager(List<Pair<String,String>> dynamicCallgraphList, int numEdges) {
		super();
		
		// STEP 0: Make param and paramPrim edges weight 1
		this.setNewWeights();
		
		// STEP 1: Extract the dynamic callgraph
		MultivalueMap<String,String> dynamicCallgraph = new MultivalueMap<String,String>();
		int counter = 0;
		for(Pair<String,String> callgraphEdge : dynamicCallgraphList) {
			dynamicCallgraph.add(callgraphEdge.getX(), callgraphEdge.getY());
			if(numEdges != -1 && counter++ > numEdges) {
				break;
			}
		}
		
		// STEP 2: Build the callgraph from param edges
		final MultivalueMap<String,String> dynamicCallgraphConverted = new MultivalueMap<String,String>();
		
		ProgramRel paramRel = (ProgramRel)ClassicProject.g().getTrgt("param");
		DomV domV = (DomV)ClassicProject.g().getTrgt("V");
		paramRel.load();
		for(int[] tuple : paramRel.getAryNIntTuples()) {
			String caller = ConversionUtils.getMethodForVar(domV.get(tuple[1])).toString();
			String callee = ConversionUtils.getMethodForVar(domV.get(tuple[0])).toString();
			if(dynamicCallgraph.get(caller).contains(callee)) {
				System.out.println("dynamic callgraph edge: " + caller + " -> " + callee);
				dynamicCallgraphConverted.add("V" + Integer.toString(tuple[1]), "V" + Integer.toString(tuple[0]));
			}
		}
		paramRel.close();	
		
		ProgramRel paramPrimRel = (ProgramRel)ClassicProject.g().getTrgt("paramPrim");
		DomU domU = (DomU)ClassicProject.g().getTrgt("U");
		paramPrimRel.load();
		for(int[] tuple : paramPrimRel.getAryNIntTuples()) {
			String caller = ConversionUtils.getMethodForVar(domU.get(tuple[1])).toString();
			String callee = ConversionUtils.getMethodForVar(domU.get(tuple[0])).toString();
			if(dynamicCallgraph.get(caller).contains(callee)) {
				System.out.println("dynamic callgraph edge: " + caller + " -> " + callee);
				dynamicCallgraphConverted.add("U" + Integer.toString(tuple[1]), "U" + Integer.toString(tuple[0]));
			}
		}
		paramPrimRel.close();
		
		// STEP 3: Build the extra relations
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param", 2, true) {
			@Override
			public boolean filter(int[] tuple) {
				return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
			}
		});
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", 2, true) {
			@Override
			public boolean filter(int[] tuple) {
				return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
			}
		});
	}
}
