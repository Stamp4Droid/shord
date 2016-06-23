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
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.util.AliasModelsUtils;
import stamp.missingmodels.util.processor.AliasModelsTraceReader;
import chord.project.Chord;

@Chord(name = "dynamic-points-to-parser-java",
consumes = { "H", "V", "M", "ptd", },
produces = { "ptdDynOnly", "ptdDynActive", "PhantomObjectDyn", },
namesOfTypes = {},
types = {},
namesOfSigns = { "ptdDynOnly", "ptdDynActive", "PhantomObjectDyn", },
signs = { "V0,H0:V0_H0", "V0,H0:V0_H0", "M0:M0" })
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
		MultivalueMap<VarNode,Pair<SiteAllocNode,Integer>> ptDyn = AliasModelsUtils.ProcessorUtils.getPtDynRetApp(processor);
		ProgramRel relPtdDynOnly = (ProgramRel)ClassicProject.g().getTrgt("ptdDynOnly");
		ProgramRel relPtdDynActive = (ProgramRel)ClassicProject.g().getTrgt("ptdDynActive");
		relPtdDynOnly.zero();
		relPtdDynActive.zero();
		Map<Pair<VarNode,SiteAllocNode>,Integer> counts = new HashMap<Pair<VarNode,SiteAllocNode>,Integer>();
		for(VarNode var : ptDyn.keySet()) {
			for(Pair<SiteAllocNode,Integer> pair : ptDyn.get(var)) {
				SiteAllocNode alloc = pair.getX();
				int count = pair.getY();
				System.out.println("DYNAMIC POINTS TO: " + var + " -> " + alloc + " (COUNT: " + count + ")");
				try {
					relPtdDynOnly.add(var, alloc);
					Pair<VarNode,SiteAllocNode> ptDynEdge = new Pair<VarNode,SiteAllocNode>(var, alloc);
					if(!pt.contains(ptDynEdge)) {
						System.out.println("ACTIVE");
						relPtdDynActive.add(var, alloc);
						if(!counts.containsKey(ptDynEdge) || counts.get(ptDynEdge) > count) {
							counts.put(ptDynEdge, count);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		relPtdDynOnly.save();
		relPtdDynActive.save();
		
		// STEP 2: Fill in dynamic phantom objects (ret -> framework allocation)
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
		
		// STEP 3: Print active edge first counts
		System.out.println("START PRINTING ACTIVE EDGE COUNTS");
		for(Pair<VarNode,SiteAllocNode> pair : counts.keySet()) {
			System.out.println("ACTIVE DYNAMIC POINTS TO EDGE FOUND: " + pair.getX() + " -> " + pair.getY() + " (COUNT: " + counts.get(pair) + ")");
		}
		System.out.println("END PRINTING ACTIVE EDGE COUNTS");
		
		// STEP 4: Check for aliased phantom objects
		System.out.println("START PRINTING ALIASED PHANTOM OBJECTS");
		int counter = 0;
		for(Set<SootMethod> methods : AliasModelsUtils.ProcessorUtils.getAliasedPhantomObjectDyn(processor)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for(SootMethod method : methods) {
				sb.append(method.toString()).append(", ");
			}
			sb.substring(0, sb.length() - 2);
			System.out.println("ALIASED PHANTOM OBJECT FOUND: " + sb.toString());
			counter++;
		}
		System.out.println("END PRINTING ALIASED PHANTOM OBJECTS");
		if(counter > 1) {
			System.out.println("NUM ALIASED PHANTOM OBJECTS FOUND: " + counter);
		}
	}
}
