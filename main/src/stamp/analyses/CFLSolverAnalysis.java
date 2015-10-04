package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphEdgeFilter;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.reader.LimLabelShordRelationReader;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "cflsolver-java")
public class CFLSolverAnalysis extends JavaAnalysis {
	public static void run(RelationReader reader, ContextFreeGrammarOpt grammar, RelationManager relations, RelationManager filterRelations) {
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		graph = graph.transform(LimLabelShordRelationReader.getSourceSinkFilterTransformer(graph.getVertices(), grammar.getSymbols()));
		Filter<Edge> filter = new GraphEdgeFilter(graph.getVertices(), grammar.getSymbols(), reader.readGraph(filterRelations, grammar.getSymbols()));
		Graph graphBar = graph.transform(new ReachabilitySolver(graph.getVertices(), grammar, filter));
		IOUtils.printGraphStatistics(graphBar);
	}
	
	public static void run(RelationReader reader, ContextFreeGrammarOpt grammar, RelationManager relations) {
		run(reader, grammar, relations, new RelationManager());
	}
	
	@Override
	public void run() {
		// Basic taint analysis
		//run(new ShordRelationReader(), new TaintGrammar().getOpt(), new TaintWithContextRelationManager());
		
		// Implicit flows and missing refref
		/*
		run(new ShordRelationReader(), new ImplicitFlowGrammar().getOpt(), new ImplicitFlowRelationManager());
		run(new ShordRelationReader(), new MissingRefRefTaintGrammar().getOpt(), new MissingRefRefTaintRelationManager());
		run(new ShordRelationReader(), new MissingRefRefImplicitFlowGrammar().getOpt(), new MissingRefRefImplicitFlowRelationManager());
		run(new ShordRelationReader(), new NegligibleImplicitFlowGrammar().getOpt(), new ImplicitFlowRelationManager());
		*/

		// Source-sink inference
		//run(new ShordRelationReader(), new TaintGrammar().getOpt(), new SourceSinkRelationManager());
		
		// Alias model inference
		//run(new ShordRelationReader(), new TaintAliasModelsPointsToGrammar().getOpt(), new TaintAliasModelsPointsToRelationManager(), new TypeFilterRelationManager());	
	}
}
