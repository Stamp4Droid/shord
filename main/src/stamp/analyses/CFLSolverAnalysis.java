package stamp.analyses;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lpsolve.LpSolveException;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
import stamp.missingmodels.util.cflsolver.relation.DynamicParamRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.FileRelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.cflsolver.util.PrintingUtils;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar taintGrammar = new TaintPointsToGrammar();
	
	private static int getNumReachableMethods() {
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("ci_reachableM");
		relReachableM.load();
		int numReachableMethods = relReachableM.size();
		relReachableM.close();
		return numReachableMethods;
	}
	
	private static MultivalueMap<String,String> getFilteredCallgraph(MultivalueMap<String,String> callgraph, List<String> methodsList, int numMethods) {
		Set<String> includedMethods = new HashSet<String>();
		for(int i=0; i<numMethods; i++) {
			if(i>=methodsList.size()) {
				break;
			}
			includedMethods.add(methodsList.get(i));
		}
		MultivalueMap<String,String> filteredCallgraph = new MultivalueMap<String,String>();
		int filteredCallgraphSize = 0;
		for(String caller : callgraph.keySet()) {
			if(includedMethods.contains(caller)) {
				for(String callee : callgraph.get(caller)) {
					if(includedMethods.contains(callee)) {
						filteredCallgraph.add(caller, callee);
						filteredCallgraphSize++;
					}
				}
			}
		}
		filteredCallgraph.add("<android.app.Activity: void <init>()>", "<edu.stanford.stamp.harness.ApplicationDriver: void registerCallback(edu.stanford.stamp.harness.Callback)>");
		
		System.out.println("Current callgraph size: " + filteredCallgraphSize);
		return filteredCallgraph;
	}
	
	@Override
	public void run() {
		try {
			RelationReader relationReader = new ShordRelationReader();
			String[] tokens = System.getProperty("stamp.out.dir").split("_");
			List<String> reachedMethods = TraceReader.getReachableMethods("../../profiler/traceouts/", tokens[tokens.length-1]);
			int numReachableMethods = getNumReachableMethods();
			System.out.println("Method coverage: " + reachedMethods.size());
			System.out.println("Number of reachable methods: " + numReachableMethods);
			System.out.println("Percentage method coverage: " + (double)reachedMethods.size()/numReachableMethods);
			
			MultivalueMap<String,String> callgraph = TraceReader.getCallgraph("../../profiler/traceouts/", tokens[tokens.length-1]);
			PrintingUtils.printRelation("callgraph");
			
			double fractionMethodIncrement = 0.1;
			int numMethods = reachedMethods.size();
			while(true) {
				if(numMethods >= reachedMethods.size()) {
					System.out.println("Running method coverage: " + (double)reachedMethods.size()/numReachableMethods);
				} else {
					System.out.println("Running method coverage: " + (double)numMethods/numReachableMethods);
				}
				RelationManager relations = new DynamicParamRelationManager(getFilteredCallgraph(callgraph, reachedMethods, numMethods));
				//RelationManager relations = new DynamicCallgraphRelationManager(DroidRecordReader.getCallgraphList("../../callgraphs/", tokens[tokens.length-1]));
				Graph g = relationReader.readGraph(relations, taintGrammar);
				TypeFilter t = relationReader.readTypeFilter(taintGrammar);
				PrintingUtils.printAbductionResult(AbductiveInferenceRunner.runInference(g, t, true, 2), true);
				
				if(numMethods >= reachedMethods.size()) {
					break;
				}
				numMethods += (int)(fractionMethodIncrement*numReachableMethods);
			}
		} catch(LpSolveException e) {
			e.printStackTrace();
		}
	}
	
 	public static void main(String[] args) throws LpSolveException {
		String directoryName = "/home/obastani/Documents/projects/research/stamp/shord_clone/stamp_output/_home_obastani_Documents_projects_research_stamp_stamptest_DarpaApps_1C_tomdroid/cfl";		
		RelationReader relationReader = new FileRelationReader(new File(directoryName));
		PrintingUtils.printAbductionResult(AbductiveInferenceRunner.runInference(relationReader.readGraph(new DynamicCallgraphRelationManager(), taintGrammar), relationReader.readTypeFilter(taintGrammar), false, 2), false);
	}
}
