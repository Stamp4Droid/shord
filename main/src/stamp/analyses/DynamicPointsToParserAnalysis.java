package stamp.analyses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import shord.analyses.SiteAllocNode;
import shord.analyses.VarNode;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Stmt;
import stamp.missingmodels.util.cflsolver.core.Util.Counter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.util.AliasModelsUtils;
import stamp.missingmodels.util.processor.AliasModelsTraceReader;
import stamp.missingmodels.util.processor.AliasModelsTraceReader.Variable;
import chord.project.Chord;

@Chord(name = "dynamic-points-to-parser-java",
consumes = { "H", "V", "M", "ptd", },
produces = { "ptdDynOnly", "ptdDynActive", "PhantomObjectDyn", },
namesOfTypes = {},
types = {},
namesOfSigns = { "ptdDynOnly", "ptdDynActive", "ptphdDynOnly", "PhantomObjectDyn", },
signs = { "V0,H0:V0_H0", "V0,H0:V0_H0", "V0,M0:V0_M0", "M0:M0" })
public class DynamicPointsToParserAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		String[] tokens = System.getProperty("stamp.out.dir").split("_");
		String filename = "../../alias_models/alias_models_traces/" + tokens[tokens.length-1] + ".trace";
		AliasModelsTraceReader processor = new AliasModelsTraceReader(filename);
		
		// STEP 0a: Get the current points-to relation
		Set<Pair<VarNode,SiteAllocNode>> pt = new HashSet<Pair<VarNode,SiteAllocNode>>();
		ProgramRel relPtd = (ProgramRel)ClassicProject.g().getTrgt("ptd");
		relPtd.load();
		for(chord.util.tuple.object.Pair<Object,Object> pair : relPtd.getAry2ValTuples()) {
			VarNode var = (VarNode)pair.val0;
			SiteAllocNode alloc = (SiteAllocNode)pair.val1;
			pt.add(new Pair<VarNode,SiteAllocNode>(var, alloc));
		}
		relPtd.close();
		
		// STEP 0b: Get the current vt relation
		MultivalueMap<VarNode,Type> types = new MultivalueMap<VarNode,Type>();
		ProgramRel relVT = (ProgramRel)ClassicProject.g().getTrgt("VT");
		relVT.load();
		for(chord.util.tuple.object.Pair<Object,Object> pair : relVT.getAry2ValTuples()) {
			VarNode var = (VarNode)pair.val0;
			Type type = (Type)pair.val1;
			types.add(var, type);
		}
		relVT.close();
		
		// STEP 1: Fill in dynamic flow (ret -> app allocation)
		MultivalueMap<VarNode,Pair<SiteAllocNode,Integer>> ptdDyn = AliasModelsUtils.ProcessorUtils.getPtDynRetApp(processor);
		ProgramRel relPtdDynOnly = (ProgramRel)ClassicProject.g().getTrgt("ptdDynOnly");
		ProgramRel relPtdDynActive = (ProgramRel)ClassicProject.g().getTrgt("ptdDynActive");
		relPtdDynOnly.zero();
		relPtdDynActive.zero();
		Map<Pair<VarNode,SiteAllocNode>,Integer> counts = new HashMap<Pair<VarNode,SiteAllocNode>,Integer>();
		for(VarNode var : ptdDyn.keySet()) {
			for(Pair<SiteAllocNode,Integer> pair : ptdDyn.get(var)) {
				SiteAllocNode alloc = pair.getX();
				int count = pair.getY();
				try {
					relPtdDynOnly.add(var, alloc);
					Pair<VarNode,SiteAllocNode> ptDynEdge = new Pair<VarNode,SiteAllocNode>(var, alloc);
					if(!counts.containsKey(ptDynEdge) || counts.get(ptDynEdge) > count) {
						counts.put(ptDynEdge, count);
					}
					if(!pt.contains(ptDynEdge)) {
						System.out.println("ACTIVE DYNAMIC POINTS TO: " + var + " -> " + alloc + " (COUNT: " + count + ")");
						relPtdDynActive.add(var, alloc);
					} else {
						System.out.println("INACTIVE DYNAMIC POINTS TO: " + var + " -> " + alloc + " (COUNT: " + count + ")");
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		relPtdDynOnly.save();
		relPtdDynActive.save();
		
		// STEP 2: Fill in ph dynamic flow (ret -> ph allocation)
		MultivalueMap<VarNode,Pair<SootMethod,Integer>> ptphdDyn = AliasModelsUtils.ProcessorUtils.getPtPhDynRet(processor);
		ProgramRel relPtphdDynOnly = (ProgramRel)ClassicProject.g().getTrgt("ptphdDynOnly");
		relPtphdDynOnly.zero();
		Map<Pair<VarNode,SootMethod>,Integer> phcounts = new HashMap<Pair<VarNode,SootMethod>,Integer>();
		for(VarNode var : ptphdDyn.keySet()) {
			for(Pair<SootMethod,Integer> pair : ptphdDyn.get(var)) {
				SootMethod alloc = pair.getX();
				int count = pair.getY();
				System.out.println("DYNAMIC POINTS TO: " + var + " -> " + alloc + " (COUNT: " + count + ")");
				try {
					relPtphdDynOnly.add(var, alloc);
					Pair<VarNode,SootMethod> ptphdDynEdge = new Pair<VarNode,SootMethod>(var, alloc);
					if(!phcounts.containsKey(ptphdDynEdge) || phcounts.get(ptphdDynEdge) > count) {
						phcounts.put(ptphdDynEdge, count);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		relPtphdDynOnly.save();
		
		// STEP 3: Fill in dynamic phantom objects (ret -> framework allocation)
		MultivalueMap<VarNode,SootMethod> phantomObjectDyn = AliasModelsUtils.ProcessorUtils.getPhantomObjectDyn(processor);
		ProgramRel relPhantomObjectDyn = (ProgramRel)ClassicProject.g().getTrgt("PhantomObjectDyn");
		relPhantomObjectDyn.zero();
		System.out.println("START PRINTING DYNAMIC PHANTOM OBJECTS");
		for(VarNode varNode : phantomObjectDyn.keySet()) {
			for(SootMethod method : phantomObjectDyn.get(varNode)) {
				relPhantomObjectDyn.add(method);
				System.out.println("DYNAMIC PHANTOM OBJECT FOUND: " + method.toString() + " (" + varNode.toString() + ")");
				System.out.println("METHOD RETURN TYPE: " + method.getReturnType().toString());
				for(Type type : types.get(varNode)) {
					System.out.println("RETURN VARIABLE TYPE: " + type.toString());
				}
				if(types.get(varNode).size() > 1) {
					System.out.println("NUM RETURN VARIABLE TYPES: " + types.get(varNode).size());
				}
			}
		}
		relPhantomObjectDyn.save();
		System.out.println("END PRINTING DYNAMIC PHANTOM OBJECTS");
		
		// STEP 4: Print edge first counts
		System.out.println("START PRINTING EDGE FIRST COUNTS");
		for(Pair<VarNode,SiteAllocNode> pair : counts.keySet()) {
			System.out.println("DYNAMIC POINTS TO EDGE FOUND FIRST COUNT: " + pair.getX() + " -> " + pair.getY() + " (COUNT: " + counts.get(pair) + ")");
		}
		System.out.println("END PRINTING EDGE FIRST COUNTS");
		
		// STEP 5: Print ph edge first counts
		System.out.println("START PRINTING PH EDGE FIRST COUNTS");
		for(Pair<VarNode,SootMethod> pair : phcounts.keySet()) {
			System.out.println("DYNAMIC PH POINTS TO EDGE FOUND FIRST COUNT: " + pair.getX() + " -> " + pair.getY() + " (COUNT: " + phcounts.get(pair) + ")");
		}
		System.out.println("END PRINTING PH EDGE FIRST COUNTS");
		
		// STEP 6: Check for aliased phantom objects
		System.out.println("START PRINTING ALIASED PHANTOM OBJECTS");
		int counter = 0;
		for(Set<Stmt> statements : AliasModelsUtils.ProcessorUtils.getAliasedPhantomObjectDyn(processor)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for(Stmt statement : statements) {
				sb.append(statement.toString()).append(", ");
			}
			sb.substring(0, sb.length() - 2);
			System.out.println("ALIASED PHANTOM OBJECT FOUND: " + sb.toString());
			counter++;
		}
		System.out.println("END PRINTING ALIASED PHANTOM OBJECTS");
		if(counter > 1) {
			System.out.println("NUM ALIASED PHANTOM OBJECTS FOUND: " + counter);
		}
		
		// STEP 7: Print counts
		System.out.println("START PRINTING RETURN COUNTS");
		Counter<Pair<Variable,Stmt>> returnCounts = AliasModelsUtils.ProcessorUtils.getReturnCounts(processor);
		for(Pair<Variable,Stmt> pair : returnCounts.sortedKeySet()) {
			System.out.println(returnCounts.getCount(pair) + ": " + pair.toString());
		}
		System.out.println("END PRINTING RETURN COUNTS");
		System.out.println("START PRINTING ALLOCATION COUNTS");
		Counter<Pair<Variable,Stmt>> allocationCounts = AliasModelsUtils.ProcessorUtils.getAllocationCounts(processor);
		for(Pair<Variable,Stmt> pair : allocationCounts.sortedKeySet()) {
			System.out.println(allocationCounts.getCount(pair) + ": " + pair.toString());
		}
		System.out.println("END PRINTING ALLOCATION COUNTS");
		System.out.println("START PRINTING ARGUMENT COUNTS");
		Counter<Pair<Variable,Stmt>> argumentCounts = AliasModelsUtils.ProcessorUtils.getArgumentCounts(processor);
		for(Pair<Variable,Stmt> pair : argumentCounts.sortedKeySet()) {
			System.out.println(argumentCounts.getCount(pair) + ": " + pair.toString());
		}
		System.out.println("END PRINTING ARGUMENT COUNTS");
		System.out.println("START PRINTING PARAMETER COUNTS");
		Counter<SootMethod> parameterCounts = AliasModelsUtils.ProcessorUtils.getParameterCounts(processor);
		for(SootMethod method : parameterCounts.sortedKeySet()) {
			System.out.println(parameterCounts.getCount(method) + ": " + method);
		}
		System.out.println("END PRINTING PARAMTER COUNTS");
		
		// STEP 8: Print objects and stubs into which they escape
		
		
	}
}
