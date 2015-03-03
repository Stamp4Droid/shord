package stamp.analyses;

import java.io.File;

import lpsolve.LpSolveException;
import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.reader.FileRelationReader;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.RelationManager.RelationReader;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static void run(RelationReader relationReader) {
		ContextFreeGrammar taintGrammar = new TaintGrammar();
		Graph gbar = new ReachabilitySolver(relationReader.readGraph(new TaintRelationManager(), taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		
		System.out.println("Printing graph statistics:");
		IOUtils.printGraphStatistics(gbar);
		
		System.out.println("Printing graph edges:");
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);		
	}
	
	@Override
	public void run() {
		run(new ShordRelationReader());
	}
	
 	public static void main(String[] args) throws LpSolveException {
		String directoryName = "/home/obastani/Documents/projects/research/stamp/shord_clone/stamp_output/_home_obastani_Documents_projects_research_stamp_stamptest_DarpaApps_1C_tomdroid/cfl";		
		run(new FileRelationReader(new File(directoryName)));
	}
}
