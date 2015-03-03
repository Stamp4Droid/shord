package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicParamRelationManager;
import stamp.missingmodels.util.cflsolver.util.AbductiveInferenceUtils;
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
		ContextFreeGrammar missingRefRefTaintGrammar = new MissingRefRefTaintGrammar();
		ContextFreeGrammar taintGrammar = new TaintGrammar();

		RelationReader relationReader = new ShordRelationReader();
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
		RelationManager relations = new DynamicParamRelationManager(new MultivalueMap<String,String>());
		ContextFreeGrammarOpt grammar = new TaintPointsToGrammar().getOpt();
		Graph g2 = relationReader.readGraph(relations, grammar.getSymbols());
		MultivalueMap<EdgeStruct,Integer> results = AbductiveInferenceUtils.runInference(new TaintPointsToGrammar().getOpt(), g2, true, 2);

		IOUtils.printAbductionResult(results, true);
	}
}
