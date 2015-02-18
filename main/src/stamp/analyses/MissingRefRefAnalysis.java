package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefTaintGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph2;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager.MissingRefRefTaintRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintWithContextRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver2;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "missing-refref-java")
public class MissingRefRefAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar missingRefRefTaintGrammar = new MissingRefRefTaintGrammar();
	private static ContextFreeGrammar taintGrammar = new TaintGrammar();
	
	@Override
	public void run() {
		RelationReader relationReader = new ShordRelationReader();
		RelationManager missingRefRefRelations = new MissingRefRefTaintRelationManager();
		RelationManager taintRelations = new TaintWithContextRelationManager();
		
		/*

		Graph gbar = new ReachabilitySolver(relationReader.readGraph(taintRelations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		
		System.out.println("Printing taint grammar statistics:");
		IOUtils.printGraphStatistics(gbar);
		
		System.out.println("Printing edges for taint grammar:");
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		*/
		
		Graph2 gbar = new ReachabilitySolver2(relationReader.readGraph(missingRefRefRelations, missingRefRefTaintGrammar), relationReader.readTypeFilter(missingRefRefTaintGrammar)).getResult();
		
		/*
		Graph gbarr = new ReachabilitySolver(relationReader.readGraph(missingRefRefRelations, missingRefRefTaintGrammar), relationReader.readTypeFilter(missingRefRefTaintGrammar)).getResult();
		
		System.out.println("Printing missing refref taint grammar statistics:");
		IOUtils.printGraphStatistics(gbarr);
		
		System.out.println("Printing edges for missing refref taint grammar:");
		IOUtils.printGraphEdges(gbarr, "Src2Sink", true);
		*/
	}
}
