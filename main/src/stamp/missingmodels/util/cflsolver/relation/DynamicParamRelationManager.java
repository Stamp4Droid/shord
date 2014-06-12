package stamp.missingmodels.util.cflsolver.relation;

import java.util.HashSet;
import java.util.Set;

import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;

public class DynamicParamRelationManager extends TaintRelationManager {
	private void setNewWeights() {
		this.clearRelationsByName("param");
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param", 2, true, 1));
		
		this.clearRelationsByName("paramPrim");
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", 2, true, 1));
	}
	
	public DynamicParamRelationManager() {
		this.setNewWeights();
	}
	
	private static Set<String> stampMethods = new HashSet<String>();
	static {
		stampMethods.add("<java.net.StampURLConnection: void connect()>");
		stampMethods.add("<java.net.StampURLConnection: java.io.InputStream getInputStream()>");
		stampMethods.add("<edu.stanford.stamp.harness.Callback: void <init>()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void <init>()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: edu.stanford.stamp.harness.ApplicationDriver getInstance()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void registerCallback(edu.stanford.stamp.harness.Callback)>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void callCallbacks()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void <clinit>()>");
		stampMethods.add("<android.content.StampSharedPreferences: void <clinit>()>");		
	}
	
	public DynamicParamRelationManager(MultivalueMap<String,String> dynamicCallgraph) {
		// STEP 0: Make param and paramPrim edges weight 1
		this.setNewWeights();
		
		// STEP 1: Build the callgraph from param edges
		final MultivalueMap<String,String> dynamicCallgraphConverted = new MultivalueMap<String,String>();
		
		ProgramRel paramRel = (ProgramRel)ClassicProject.g().getTrgt("param");
		DomV domV = (DomV)ClassicProject.g().getTrgt("V");
		paramRel.load();
		for(int[] tuple : paramRel.getAryNIntTuples()) {
			String caller = ConversionUtils.getMethodForVar(domV.get(tuple[1])).toString();
			String callee = ConversionUtils.getMethodForVar(domV.get(tuple[0])).toString();
			if(dynamicCallgraph.get(caller).contains(callee) || stampMethods.contains(caller) || stampMethods.contains(callee)) {
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
		
		// STEP 2: Build the extra relations
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
