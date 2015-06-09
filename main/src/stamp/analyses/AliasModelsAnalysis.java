package stamp.analyses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphEdgeFilter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphVertexFilter;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;
import stamp.missingmodels.util.cflsolver.grammars.AliasModelsGrammar;
import stamp.missingmodels.util.cflsolver.grammars.AliasModelsGrammar.AliasModelsLimGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.AliasModelsRelationManager;
import stamp.missingmodels.util.cflsolver.relation.AliasModelsRelationManager.AliasModelsLimRelationManager;
import stamp.missingmodels.util.cflsolver.relation.FilterRelationManager.AliasModelsFilterRelationManager;
import stamp.missingmodels.util.cflsolver.relation.FilterRelationManager.AliasModelsLimFilterRelationManager;
import stamp.missingmodels.util.cflsolver.util.AliasModelsSynthesis;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "alias-models-java")
public class AliasModelsAnalysis extends JavaAnalysis {
	public static Set<String> getVertexFilterHelper(Graph graphBar) {
		final Set<String> rejectedVertices = new HashSet<String>();
		for(int i=0; i<graphBar.getVertices().size(); i++) {
			if(graphBar.getVertices().get(i).startsWith("M")) {
				rejectedVertices.add(graphBar.getVertices().get(i));
			}
		}
		for(EdgeStruct edge : graphBar.getEdgeStructs(new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("FlowPreFull"); }})) {
			rejectedVertices.remove(edge.sinkName);
		}
		return rejectedVertices;
	}
	
	public static Set<String> getVertexFilter() {
		// Run full analysis only to get active methods
		RelationReader reader = new ShordRelationReader();
		ContextFreeGrammarOpt grammar = new AliasModelsLimGrammar().getOpt();
		RelationManager relations = new AliasModelsLimRelationManager();
		RelationManager filterRelations = new AliasModelsLimFilterRelationManager();
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> filter = new GraphEdgeFilter(graph.getVertices(), grammar.getSymbols(), reader.readGraph(filterRelations, grammar.getSymbols()));
		Graph graphBar = graph.transform(new ReachabilitySolver(graph.getVertices(), grammar, filter));
		return getVertexFilterHelper(graphBar);
	}
	
	public static Graph getAliasModels() {
		RelationReader reader = new ShordRelationReader();
		ContextFreeGrammarOpt grammar = new AliasModelsGrammar().getOpt();
		RelationManager relations = new AliasModelsRelationManager(true);
		RelationManager filterRelations = new AliasModelsFilterRelationManager(true);
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> filter = new AndFilter(new GraphEdgeFilter(graph.getVertices(), grammar.getSymbols(), reader.readGraph(filterRelations, grammar.getSymbols())), new GraphVertexFilter(graph.getVertices(), getVertexFilter()));
		return graph.transform(new ReachabilitySolver(graph.getVertices(), grammar, filter));
	}
	
	@Override
	public void run() {
		Graph graphBar = getAliasModels();
		for(Edge flowEdge : graphBar.getEdges(new Filter<Edge>() { public boolean filter(Edge e) { return e.symbol.symbol.equals("FlowNew"); }})) {
			if(flowEdge.weight == (short)0) {
				continue;
			}
			System.out.println("FLOW EDGE: " + flowEdge.toString(true));
			List<Pair<EdgeStruct,Boolean>> path = new ArrayList<Pair<EdgeStruct,Boolean>>();
			for(Pair<Edge,Boolean> pair : flowEdge.getPath()) {
				System.out.println("PATH EDGE: " + pair.getX().toString(true));
				path.add(new Pair<EdgeStruct,Boolean>(pair.getX().getStruct(), pair.getY()));
			}
			for(Pair<EdgeStruct,Boolean> pair : AliasModelsSynthesis.synthesize(path)) {
				String text = (pair.getX().weight > (short)0) ? "MODEL EDGE: " : "INTERMEDIATE EDGE: ";
				System.out.println(text + pair.getX().toString(true) + " (" + pair.getY() + ")");
			}
		}
		IOUtils.printGraphStatistics(graphBar);
	}
}
