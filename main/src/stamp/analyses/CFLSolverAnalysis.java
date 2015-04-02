package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.grammars.AliasModelsGrammar;
import stamp.missingmodels.util.cflsolver.grammars.ImplicitFlowGrammar;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefImplicitFlowGrammar;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefTaintGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.AliasModelsRelationManager;
import stamp.missingmodels.util.cflsolver.relation.ImplicitFlowRelationManager;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager.MissingRefRefImplicitFlowRelationManager;
import stamp.missingmodels.util.cflsolver.relation.SourceSinkRelationManager;
import stamp.missingmodels.util.cflsolver.relation.TaintWithContextRelationManager;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "cflsolver-java")
public class CFLSolverAnalysis extends JavaAnalysis {
	public static void run(RelationReader reader, ContextFreeGrammarOpt grammar, RelationManager relations) {
		Graph graph = reader.readGraph(relations, grammar.getSymbols());
		Graph graphBar = graph.transform(new ReachabilitySolver(graph.getVertices(), grammar));
		System.out.println("Printing graph edges:");
		IOUtils.printGraphStatistics(graphBar);
		IOUtils.printGraphEdges(graphBar, "Src2Sink", true);
	}
	
	@Override
	public void run() {
		run(new ShordRelationReader(), new TaintGrammar().getOpt(), new TaintWithContextRelationManager());
		run(new ShordRelationReader(), new ImplicitFlowGrammar().getOpt(), new ImplicitFlowRelationManager());
		run(new ShordRelationReader(), new MissingRefRefTaintGrammar().getOpt(), new MissingRefRefRelationManager());
		run(new ShordRelationReader(), new MissingRefRefImplicitFlowGrammar().getOpt(), new MissingRefRefImplicitFlowRelationManager());
		
		run(new ShordRelationReader(), new AliasModelsGrammar().getOpt(), new AliasModelsRelationManager());
		run(new ShordRelationReader(), new TaintGrammar().getOpt(), new SourceSinkRelationManager());
	}
}
