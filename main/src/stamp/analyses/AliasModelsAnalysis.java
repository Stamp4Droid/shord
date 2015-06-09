package stamp.analyses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphEdgeFilter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphVertexFilter;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util;
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.grammars.AliasModelsLimGrammar;
import stamp.missingmodels.util.cflsolver.grammars.AliasModelsLimGrammar.AliasModelsGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.AliasModelsLimRelationManager;
import stamp.missingmodels.util.cflsolver.relation.AliasModelsLimRelationManager.AliasModelsRelationManager;
import stamp.missingmodels.util.cflsolver.relation.FilterRelationManager.AliasModelsFilterRelationManager;
import stamp.missingmodels.util.cflsolver.relation.FilterRelationManager.AliasModelsLimFilterRelationManager;
import stamp.missingmodels.util.cflsolver.util.AliasModelsUtils;
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
	
	public static Graph run(RelationReader reader, ContextFreeGrammarOpt grammar, RelationManager relations, RelationManager filterRelations, Set<String> vertexFilter) {
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> filter = new AndFilter(new GraphEdgeFilter(graph.getVertices(), grammar.getSymbols(), reader.readGraph(filterRelations, grammar.getSymbols())), new GraphVertexFilter(graph.getVertices(), vertexFilter));
		return graph.transform(new ReachabilitySolver(graph.getVertices(), grammar, filter));		
	}
	
	// Run full analysis only to get active methods
	public static Graph getVertexFilterGraph(Set<String> vertexFilter) {
		return run(new ShordRelationReader(), new AliasModelsLimGrammar().getOpt(), new AliasModelsLimRelationManager(true), new AliasModelsLimFilterRelationManager(true), vertexFilter);
	}
	
	public static Graph getAliasModels(Set<String> vertexFilter) {
		Set<String> vertexFilterFull = Util.union(vertexFilter, getVertexFilterHelper(getVertexFilterGraph(vertexFilter)));
		return run(new ShordRelationReader(), new AliasModelsGrammar().getOpt(), new AliasModelsRelationManager(true), new AliasModelsFilterRelationManager(true), vertexFilterFull);
	}
	
	public static List<List<EdgeStruct>> getAliasModelsForThreshold(int threshold, int maxIters) {
		Set<String> vertexFilter = new HashSet<String>();
		for(int i=0; i<maxIters; i++) {
			Graph graphBar = getAliasModels(vertexFilter);
			List<List<EdgeStruct>> models = AliasModelsUtils.SynthesisUtils.getModelsFromGraph(graphBar);
			Set<String> methodRejects = AliasModelsUtils.SynthesisUtils.getMethodRejects(models, threshold);
			if(threshold < 0 || methodRejects.isEmpty()) {
				return models;
			}
			vertexFilter = Util.union(vertexFilter, methodRejects);
		}
		System.out.println("ERROR: Exceeded max iters!");
		return new ArrayList<List<EdgeStruct>>();
	}
	
	public static Map<Integer,List<List<EdgeStruct>>> getIterativeRun(int maxThreshold, int maxIters) {
		Map<Integer,List<List<EdgeStruct>>> modelsForThreshold = new HashMap<Integer,List<List<EdgeStruct>>>();
		modelsForThreshold.put(-1, getAliasModelsForThreshold(-1, maxIters));
		for(int threshold=0; threshold<=maxThreshold; threshold++) {
			modelsForThreshold.put(threshold, getAliasModelsForThreshold(threshold, maxIters));
		}
		return modelsForThreshold;
	}
	
	public static boolean checkActiveFlowNew() {
		ProgramRel relActiveFlowNew = (ProgramRel)ClassicProject.g().getTrgt("ActiveFlowNew");
		relActiveFlowNew.load();
		return relActiveFlowNew.size() > 0;
	}
	
	@Override
	public void run() {
		if(!checkActiveFlowNew()) {
			System.out.println("ERROR: No active flow edges found!");
			return;
		}
		Map<Integer,List<List<EdgeStruct>>> modelsForThreshold = getIterativeRun(2, 10);
		for(int threshold : modelsForThreshold.keySet()) {
			System.out.println("MODELS FOR THRESHOLD: " + threshold);
			for(List<EdgeStruct> model : modelsForThreshold.get(threshold)) {
				System.out.println("PRINING MODEL");
				for(EdgeStruct edge : model) {
					System.out.println("MODEL EDGE: " + edge.toString(true));
				}
			}
		}
		//System.out.println("FLOW EDGE: " + flowEdge.toString(true));
		//System.out.println("PATH EDGE: " + pair.getX().toString(true));
		//String text = (pair.getX().weight > (short)0) ? "MODEL EDGE: " : "INTERMEDIATE EDGE: ";
		//System.out.println(text + pair.getX().toString(true) + " (" + pair.getY() + ")");
	}
}
