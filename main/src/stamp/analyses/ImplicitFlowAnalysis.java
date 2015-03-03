package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.grammars.ImplicitFlowGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
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
		/*
		RelationReader relationReader = new ShordRelationReader();
		RelationManager implicitRelations = new ImplicitFlowRelationManager();
		RelationManager taintRelations = new TaintWithContextRelationManager();
		
		Graph gbar = new ReachabilitySolver(relationReader.readGraph(taintRelations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();

		System.out.println("Printing taint grammar statistics:");
		IOUtils.printGraphStatistics(gbar);
		
		System.out.println("Printing edges for taint grammar:");
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		
		Graph gbari = new ReachabilitySolver(relationReader.readGraph(implicitRelations, implicitTaintGrammar), relationReader.readTypeFilter(implicitTaintGrammar)).getResult();
		
		System.out.println("Printing implicit taint grammar statistics:");
		IOUtils.printGraphStatistics(gbari);
		
		System.out.println("Printing edges for implicit taint grammar:");
		IOUtils.printGraphEdges(gbari, "Src2Sink", true);
		*/
	}
}
