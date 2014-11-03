package stamp.analyses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner;
import stamp.missingmodels.util.cflsolver.grammars.CallgraphTaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.cflsolver.util.ConversionUtils;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "tests")
public class TestsAnalysis extends JavaAnalysis {
	//private static ContextFreeGrammar taintGrammar = new TaintPointsToGrammar();
	private static ContextFreeGrammar taintGrammar = new CallgraphTaintGrammar();
	
	private static int getNumReachableMethods() {
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("reachableM");
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
				
		System.out.println("Current callgraph size: " + filteredCallgraphSize);
		return filteredCallgraph;
	}
	
	@Override
	public void run() {
		try {
			RelationReader relationReader = new ShordRelationReader();
			String[] tokens = System.getProperty("stamp.out.dir").split("_");
			
			List<String> reachedMethods = TraceReader.getReachableMethods("profiler/traceouts/", tokens[tokens.length-1]);
			int numReachableMethods = getNumReachableMethods();

			List<Pair<String,String>> testReachedCallgraph = TraceReader.getOrderedCallgraph("profiler/traceouts", tokens[tokens.length-1]);
			testReachedCallgraph.addAll(TraceReader.getOrderedCallgraph("profiler/traceouts_test", tokens[tokens.length-1]));
			
			System.out.println("Method coverage: " + reachedMethods.size());
			System.out.println("Number of reachable methods: " + numReachableMethods);
			System.out.println("Percentage method coverage: " + (double)reachedMethods.size()/numReachableMethods);

			System.out.println("Test number of reached callgraph edges: " + testReachedCallgraph.size());
			
			MultivalueMap<String,String> callgraph = TraceReader.getCallgraph("profiler/traceouts/", tokens[tokens.length-1]);
			//IOUtils.printRelation("callgraph");
			
			//double fractionMethodIncrement = 0.1;
			double fractionMethodIncrement = (double)reachedMethods.size()/(1.5*numReachableMethods);
			//int numMethods = reachedMethods.size();
			int numMethods = 0;
			while(true) {
				double trueSize = numMethods >= reachedMethods.size() ? (double)reachedMethods.size() : (double)numMethods;
				System.out.println("Running method coverage: " + trueSize/numReachableMethods);

				//RelationManager relations = new DynamicParamRelationManager(getFilteredCallgraph(callgraph, reachedMethods, numMethods));
				//RelationManager relations = new DynamicParamRelationManager(DroidRecordReader.getCallgraphList("../../callgraphs/", tokens[tokens.length-1]));
				RelationManager relations = new DynamicCallgraphRelationManager(getFilteredCallgraph(callgraph, reachedMethods, numMethods));
				Graph g = relationReader.readGraph(relations, taintGrammar);
				TypeFilter t = relationReader.readTypeFilter(taintGrammar);
				MultivalueMap<EdgeStruct,Integer> results = AbductiveInferenceRunner.runInference(g, t, true, 2); 
				IOUtils.printAbductionResult(results, true);

				Map<Integer,Integer> minTimeToCrash = new HashMap<Integer,Integer>();
				Map<Integer,String> callgraphEdgeToCrash = new HashMap<Integer,String>();
				for(EdgeStruct edge : results.keySet()) {
					for(int cut : results.get(edge)) {
						Integer curTimeToCrash = minTimeToCrash.get(cut);
						Pair<String,String> callgraphEdge = new Pair<String,String>(ConversionUtils.getMethodSig(edge.sourceName), ConversionUtils.getMethodSig(edge.sinkName));
						int newTimeToCrash = testReachedCallgraph.indexOf(callgraphEdge);
						if(newTimeToCrash == -1) {
							newTimeToCrash = testReachedCallgraph.size();
						}
						if(curTimeToCrash == null || newTimeToCrash < curTimeToCrash) {
							minTimeToCrash.put(cut, newTimeToCrash);
							callgraphEdgeToCrash.put(cut, callgraphEdge.getX() + " -> " + callgraphEdge.getY());
						}
					}
				}
				for(int cut : minTimeToCrash.keySet()) {
					System.out.println("MIN TIME TO CRASH FOR CUT " + cut + ": " + minTimeToCrash.get(cut));
					System.out.println("CALLGRAPH EDGE TO CRASH FOR CUT " + cut + ": " + callgraphEdgeToCrash.get(cut));
				}

				if(numMethods >= reachedMethods.size()) {
					break;
				}
				numMethods += (int)(fractionMethodIncrement*numReachableMethods);
			}
		} catch(LpSolveException e) {
			e.printStackTrace();
		}
	}
}
