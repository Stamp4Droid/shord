package stamp.missingmodels.util.cflsolver.relation;

import java.util.HashSet;
import java.util.Set;

import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPointsToRelationManager;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;

public class DynamicParamRelationManager extends TaintPointsToRelationManager {
	private void setNewWeights() {
		this.clearRelationsByName("param");
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param", null, (short)1));
		
		this.clearRelationsByName("return");
		this.add(new IndexRelation("return", "V", 1, "V", 0, "return", null, (short)1));
		
		this.clearRelationsByName("paramPrim");
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", null, (short)1));
		
		this.clearRelationsByName("returnPrim");
		this.add(new IndexRelation("returnPrim", "U", 1, "U", 0, "returnPrim", null, (short)1));
	}
	
	public DynamicParamRelationManager() {
		this.setNewWeights();
	}
	
	private static Set<String> stampMethods = new HashSet<String>();
	static {
		/*
		stampMethods.add("<java.net.StampURLConnection: void connect()>");
		stampMethods.add("<java.net.StampURLConnection: java.io.InputStream getInputStream()>");
		stampMethods.add("<edu.stanford.stamp.harness.Callback: void <init>()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void <init>()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: edu.stanford.stamp.harness.ApplicationDriver getInstance()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void registerCallback(edu.stanford.stamp.harness.Callback)>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void callCallbacks()>");
		stampMethods.add("<edu.stanford.stamp.harness.ApplicationDriver: void <clinit>()>");
		stampMethods.add("<android.content.StampSharedPreferences: void <clinit>()>");
		*/
		stampMethods.add("<java.");
		stampMethods.add("<android.");
		stampMethods.add("<edu.stanford.stamp.");
		
		/*
		stampMethods.add("<java.net.StampURLConnection: ");
		stampMethods.add("<edu.stanford.stamp.harness");
		stampMethods.add("<android.content.StampSharedPreferences: ");
		*/
		stampMethods.add("<c.");
	}
	
	private static MultivalueMap<String,String> paramMethods = new MultivalueMap<String,String>();
	
	public static boolean isParamCallEdge(String caller, String callee) {
		return paramMethods.get(caller).contains(callee);
	}

	public static boolean isStampMethod(String method) {
		for(String name : stampMethods) {
			if(method.startsWith(name)) {
			    System.out.println(method);
				return true;
			}
		}
		return false;
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
			//if(dynamicCallgraph.get(caller).contains(callee) || isStampCallEdge(caller, callee)) {
			if(dynamicCallgraph.get(caller).contains(callee) || isStampMethod(caller) || isParamCallEdge(caller, callee)) {
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
			if(dynamicCallgraph.get(caller).contains(callee) || isStampMethod(callee) || isParamCallEdge(caller, callee)) {
				dynamicCallgraphConverted.add("U" + Integer.toString(tuple[1]), "U" + Integer.toString(tuple[0]));
			}
		}
		paramPrimRel.close();
		
		ProgramRel returnRel = (ProgramRel)ClassicProject.g().getTrgt("return");
		returnRel.load();
		for(int[] tuple : returnRel.getAryNIntTuples()) {
			String caller = ConversionUtils.getMethodForVar(domV.get(tuple[1])).toString();
			String callee = ConversionUtils.getMethodForVar(domV.get(tuple[0])).toString();
			//if(dynamicCallgraph.get(caller).contains(callee) || isStampCallEdge(caller, callee)) {
			if(dynamicCallgraph.get(caller).contains(callee) || isStampMethod(caller)) {
				dynamicCallgraphConverted.add("V" + Integer.toString(tuple[1]), "V" + Integer.toString(tuple[0]));
			}
		}
		returnRel.close();	
		
		ProgramRel returnPrimRel = (ProgramRel)ClassicProject.g().getTrgt("returnPrim");
		returnPrimRel.load();
		for(int[] tuple : returnPrimRel.getAryNIntTuples()) {
			String caller = ConversionUtils.getMethodForVar(domU.get(tuple[1])).toString();
			String callee = ConversionUtils.getMethodForVar(domU.get(tuple[0])).toString();
			if(dynamicCallgraph.get(caller).contains(callee) || isStampMethod(callee)) {
				dynamicCallgraphConverted.add("U" + Integer.toString(tuple[1]), "U" + Integer.toString(tuple[0]));
			}
		}
		returnPrimRel.close();
		
		// STEP 2: Build the extra relations
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param") {
			@Override
			public boolean filter(int[] tuple) {
				return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
			}
		});
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim") {
			@Override
			public boolean filter(int[] tuple) {
				return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
			}
		});
		this.add(new IndexRelation("return", "V", 1, "V", 0, "return") {
			@Override
			public boolean filter(int[] tuple) {
				return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
			}
		});
		this.add(new IndexRelation("returnPrim", "U", 1, "U", 0, "returnPrim") {
			@Override
			public boolean filter(int[] tuple) {
				return dynamicCallgraphConverted.get(this.getSource(tuple)).contains(this.getSink(tuple));
			}
		});
	}
}
