package stamp.analyses;

import lpsolve.LpSolveException;
import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.AbductiveInferenceRunner;
import stamp.missingmodels.util.cflsolver.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.Graph;
import stamp.missingmodels.util.cflsolver.RelationManager;
import stamp.missingmodels.util.cflsolver.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicParamRelationManager;
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
		//RelationManager relations = new TaintPointsToRelationManager();
		ContextFreeGrammarOpt taintGrammar = new TaintPointsToGrammar().getOpt();
		Graph g2 = new Graph(taintGrammar.getSymbols(), new DynamicParamRelationManager(new MultivalueMap<String,String>()));
		try {
			MultivalueMap<EdgeStruct,Integer> results = AbductiveInferenceRunner.runInference(taintGrammar, g2, true, 2);
			IOUtils.printAbductionResult(results, true);
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}
}
