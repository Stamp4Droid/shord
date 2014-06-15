package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "cflsolver")
public class CFLSolverAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar taintGrammar = new TaintPointsToGrammar();
	
	@Override
	public void run() {
		RelationReader relationReader = new ShordRelationReader();
		RelationManager relations = new TaintRelationManager();
		
		Graph gbar = new ReachabilitySolver(relationReader.readGraph(relations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		
		System.out.println("Printing graph statistics:");
		IOUtils.printGraphStatistics(gbar);
		
		System.out.println("Printing graph edges:");
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
	}
}
