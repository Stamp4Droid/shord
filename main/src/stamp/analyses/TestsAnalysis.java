package stamp.analyses;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shord.analyses.DomM;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.grammars.CallgraphTaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
import stamp.missingmodels.util.cflsolver.util.AbductiveInferenceUtils;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "tests")
public class TestsAnalysis extends JavaAnalysis {
	//private static ContextFreeGrammar taintGrammar = new TaintPointsToGrammar();
	private static ContextFreeGrammar taintGrammar = new CallgraphTaintGrammar();
	
	public static int getNumReachableMethods() {
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("reachableM");
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
	
	public static class CallgraphCompleter {
		private Set<String> processed = new HashSet<String>();
		private MultivalueMap<String,String> staticCallgraphReverse;
		private MultivalueMap<String,String> completeDynamicCallgraph;
		
		public CallgraphCompleter() {
			this.staticCallgraphReverse = getStaticCallgraphReverse();
			this.completeDynamicCallgraph = new MultivalueMap<String,String>();
		}
		
		private void makeReachable(String method) {
			if(this.processed.contains(method)) {
				return;
			}
			this.processed.add(method);
			for(String parent : staticCallgraphReverse.get(method)) {
				System.out.println("Adding dynamic callgraph completion: " + parent + " -> " + method);
				this.completeDynamicCallgraph.add(parent, method);
				this.makeReachable(parent);
			}
		}
		
		public MultivalueMap<String,String> completeDynamicCallgraph(MultivalueMap<String,String> dynamicCallgraph) {			
			for(String caller : dynamicCallgraph.keySet()) {
				this.makeReachable(caller);
				for(String callee : dynamicCallgraph.get(caller)) {
					this.makeReachable(callee);
				}
			}
			return this.completeDynamicCallgraph;
		}
	}
	
	@Override
	public void run() {
		String[] tokens = System.getProperty("stamp.out.dir").split("_");
		List<String> reachedMethods = TraceReader.getReachableMethods("profiler/traceouts/", tokens[tokens.length-1]);
		
		List<Pair<String,String>> testReachedCallgraph = TraceReader.getOrderedCallgraph("profiler/traceouts", tokens[tokens.length-1]);
		testReachedCallgraph.addAll(TraceReader.getOrderedCallgraph("profiler/traceouts_test", tokens[tokens.length-1]));
		
		int numReachableMethods = getNumReachableMethods();
		
		MultivalueMap<String,String> callgraph = new CallgraphCompleter().completeDynamicCallgraph(TraceReader.getCallgraph("profiler/traceouts/", tokens[tokens.length-1]));
		List<String> testReachedMethods = TraceReader.getReachableMethods("profiler/traceouts_test/", tokens[tokens.length-1]);
		
		System.out.println("Method coverage: " + reachedMethods.size());
		System.out.println("Number of reachable methods: " + numReachableMethods);
		System.out.println("Percentage method coverage: " + (double)reachedMethods.size()/numReachableMethods);
		System.out.println("Test number of reached callgraph edges: " + testReachedCallgraph.size());
		System.out.println("Test coverage: " + (double)testReachedMethods.size()/numReachableMethods);
		
		RelationReader relationReader = new ShordRelationReader();
		Graph g = relationReader.readGraph(new DynamicCallgraphRelationManager(callgraph), taintGrammar.getSymbols());
		MultivalueMap<EdgeStruct,Integer> results = AbductiveInferenceUtils.runInference(taintGrammar.getOpt(), g, relationReader.readFilter(g.getVertices(), taintGrammar.getSymbols()), true, 2); 
		
		IOUtils.printAbductionResult(results, true);
	}
}
