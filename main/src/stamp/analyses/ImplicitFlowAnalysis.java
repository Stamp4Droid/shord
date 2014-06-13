package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.grammars.ImplicitFlowGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.relation.ImplicitFlowRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.util.PrintingUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "implicit-flow-java")
public class ImplicitFlowAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar implicitTaintGrammar = new ImplicitFlowGrammar();
	private static ContextFreeGrammar taintGrammar = new TaintGrammar();
	
	@Override
	public void run() {
		RelationReader relationReader = new ShordRelationReader();
		RelationManager relations = new ImplicitFlowRelationManager();
		
		Graph gbar = new ReachabilitySolver(relationReader.readGraph(relations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		Graph gbari = new ReachabilitySolver(relationReader.readGraph(relations, implicitTaintGrammar), relationReader.readTypeFilter(implicitTaintGrammar)).getResult();
		
		System.out.println("Printing taint grammar statistics:");
		PrintingUtils.printGraphStatistics(gbar);
		//PrintingUtils.printGraphEdges(gbar, "Label2Ref", false);
		
		System.out.println("Printing implicit taint grammar statistics:");
		PrintingUtils.printGraphStatistics(gbari);
		
		System.out.println("Printing edges for taint grammar:");
		PrintingUtils.printGraphEdges(gbar, "Src2Sink", true);
		
		System.out.println("Printing edges for implicit taint grammar:");
		PrintingUtils.printGraphEdges(gbari, "Src2Sink", true);
	}
}
