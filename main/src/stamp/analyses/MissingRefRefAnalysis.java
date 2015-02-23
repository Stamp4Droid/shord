package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefTaintGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager.MissingRefRefTaintRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintWithContextRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.Graph2;
import stamp.missingmodels.util.jcflsolver2.ReachabilitySolver2;
import stamp.missingmodels.util.jcflsolver2.RelationManager;
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
		Graph2 gbar = new Graph2(new MissingRefRefTaintGrammar().getOpt(), new MissingRefRefTaintRelationManager()).transform(new ReachabilitySolver2());
				
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
		
		Graph gbarr = new ReachabilitySolver(relationReader.readGraph(missingRefRefRelations, missingRefRefTaintGrammar), relationReader.readTypeFilter(missingRefRefTaintGrammar)).getResult();
		
		System.out.println("Printing missing refref taint grammar statistics:");
		IOUtils.printGraphStatistics(gbarr);
		
		System.out.println("Printing edges for missing refref taint grammar:");
		IOUtils.printGraphEdges(gbarr, "Src2Sink", true);
		
	}
}
