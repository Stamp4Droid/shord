package stamp.analyses;

import java.io.File;

import lpsolve.LpSolveException;
import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner;
import stamp.missingmodels.util.cflsolver.grammars.DebugTaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.FileRelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.cflsolver.util.PrintingUtils;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar taintGrammar = new DebugTaintGrammar();
	
	@Override
	public void run() {
		try {
			RelationReader relationReader = new ShordRelationReader();
			String[] tokens = System.getProperty("stamp.out.dir").split("_");
			RelationManager relations = new DynamicCallgraphRelationManager(TraceReader.getCallgraphList("../../profiler/traceouts/", tokens[tokens.length-1]));
			//RelationManager relations = new DynamicCallgraphRelationManager(DroidRecordReader.getCallgraphList("../../callgraphs/", tokens[tokens.length-1]));
			Graph g = relationReader.readGraph(relations, taintGrammar);
			TypeFilter t = relationReader.readTypeFilter(taintGrammar);
			System.out.println("Method coverage: " + TraceReader.getReachableMethods("../../profiler/traceouts/", tokens[tokens.length-1]).size());
			PrintingUtils.printAbductionResult(AbductiveInferenceRunner.runInference(g, t, true, 4), true);
		} catch(LpSolveException e) {
			e.printStackTrace();
		}
	}
	
 	public static void main(String[] args) throws LpSolveException {
		String directoryName = "/home/obastani/Documents/projects/research/stamp/shord_clone/stamp_output/_home_obastani_Documents_projects_research_stamp_stamptest_DarpaApps_1C_tomdroid/cfl";		
		RelationReader relationReader = new FileRelationReader(new File(directoryName));
		PrintingUtils.printAbductionResult(AbductiveInferenceRunner.runInference(relationReader.readGraph(new DynamicCallgraphRelationManager(), taintGrammar), relationReader.readTypeFilter(taintGrammar), false, 4), false);
	}
}
