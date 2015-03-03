package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefImplicitFlowGrammar;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefTaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager.MissingRefRefImplicitFlowRelationManager;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager.MissingRefRefTaintRelationManager;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "missing-refref-implicit-flow-java")
public class MissingRefRefImplicitFlowAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar implicitTaintGrammar = new MissingRefRefImplicitFlowGrammar();
	private static ContextFreeGrammar taintGrammar = new MissingRefRefTaintGrammar();
	
	@Override
	public void run() {
		RelationReader relationReader = new ShordRelationReader();
		RelationManager implicitRelations = new MissingRefRefImplicitFlowRelationManager();
		RelationManager taintRelations = new MissingRefRefTaintRelationManager();
		
		/*
		Graph gbar = new ReachabilitySolver(relationReader.readGraph(taintRelations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		
		System.out.println("Printing taint grammar statistics:");
		IOUtils.printGraphStatistics(gbar);
		
		System.out.println("Printing edges for taint grammar:");
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		*/
		
		/*
		Graph gbari = new ReachabilitySolver(relationReader.readGraph(implicitRelations, implicitTaintGrammar), relationReader.readTypeFilter(implicitTaintGrammar)).getResult();
		
		//System.out.println("Printing implicit taint grammar statistics:");
		//IOUtils.printGraphStatistics(gbari);
		
		System.out.println("Printing tainted edges:");
		//IOUtils.printGraphEdges(gbari, "Label2Ref", true);
		IOUtils.printGraphEdges(gbari, "Label2Ref", true);
		IOUtils.printGraphEdges(gbari, "Label2Prim", true);
		
		System.out.println("Printing edges for implicit taint grammar:");
		IOUtils.printGraphEdges(gbari, "Src2Sink", true);
		*/
	}
}
