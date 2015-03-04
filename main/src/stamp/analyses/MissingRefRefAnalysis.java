package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefTaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "missing-refref-java")
public class MissingRefRefAnalysis extends JavaAnalysis {
	@Override
	public void run() {
		/*
		RelationManager missingRefRefRelations = new MissingRefRefTaintRelationManager();
		RelationManager taintRelations = new TaintWithContextRelationManager();

		Graph gbar = new ReachabilitySolver(relationReader.readGraph(taintRelations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();

		System.out.println("Printing taint grammar statistics:");
		IOUtils.printGraphStatistics(gbar);

		System.out.println("Printing edges for taint grammar:");
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		 */

		//Graph2 gbar = new Graph2(new MissingRefRefTaintGrammar().getOpt(), new MissingRefRefTaintRelationManager()).transform(new ReachabilitySolver2());
		//Graph gbarr = new ReachabilitySolver(relationReader.readGraph(missingRefRefRelations, missingRefRefTaintGrammar), relationReader.readTypeFilter(missingRefRefTaintGrammar)).getResult();

		RelationReader relationReader = new ShordRelationReader();
		
		//RelationManager relations = new DynamicParamRelationManager(new MultivalueMap<String,String>());
		//ContextFreeGrammarOpt grammar = new TaintPointsToGrammar().getOpt();
		ContextFreeGrammarOpt grammar = new MissingRefRefTaintGrammar().getOpt();
		RelationManager relations = new MissingRefRefRelationManager();
		
		Graph g = relationReader.readGraph(relations, grammar.getSymbols());
		Graph gbar = g.transform(new ReachabilitySolver(g.getVertices(), grammar));
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		
		//MultivalueMap<EdgeStruct,Integer> results = AbductiveInferenceUtils.runInference(grammar, g, relationReader.readFilter(g.getVertices(), grammar.getSymbols()), true, 2);
		//IOUtils.printAbductionResult(results, true);
	}
}
