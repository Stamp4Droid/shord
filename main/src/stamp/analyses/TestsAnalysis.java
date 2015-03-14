package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference.AbductiveInferenceHelper;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.Filter;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicParamRelationManager;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

@Chord(name = "tests")
public class TestsAnalysis extends JavaAnalysis {
	public static class TestAbductiveInferenceHelper implements AbductiveInferenceHelper {
		@Override
		public Iterable<Edge> getBaseEdges(Graph gbar) {
			return gbar.getEdges(new Filter<Edge>() {
				@Override
				public boolean filter(Edge edge) {
					return edge.symbol.symbol.equals("param") || edge.symbol.symbol.equals("return")
							|| edge.symbol.symbol.equals("paramPrim") || edge.symbol.symbol.equals("returnPrim");
				}
			});
		}
		
		@Override
		public Iterable<Edge> getInitialEdges(Graph gbar) {
			return gbar.getEdges(new Filter<Edge>() {
				@Override
				public boolean filter(Edge edge) {
					return edge.symbol.symbol.equals("Src2Sink");
				}
			});
		}
	}
	
	@Override
	public void run() {
		RelationReader reader = new ShordRelationReader();
		RelationManager relations = new DynamicParamRelationManager(new MultivalueMap<String,String>());
		ContextFreeGrammarOpt grammar = new TaintPointsToGrammar().getOpt();
		
		Graph g = reader.readGraph(relations, grammar.getSymbols());
		
		Graph gbar = g.transform(new ReachabilitySolver(g.getVertices(), grammar, reader.readFilter(g.getVertices(), grammar.getSymbols())));
		System.out.println("Printing graph edges:");
		IOUtils.printGraphStatistics(gbar);
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		
		MultivalueMap<EdgeStruct,Integer> results = new AbductiveInference(grammar, new TestAbductiveInferenceHelper()).process(g, reader.readFilter(g.getVertices(), grammar.getSymbols()), 2);
		IOUtils.printAbductionResult(results, true);
	}
}
