package stamp.analyses;

import java.util.HashSet;
import java.util.List;
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
import stamp.missingmodels.util.cflsolver.core.Util.AndFilter;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.grammars.AliasModelsShortLimGrammar;
import stamp.missingmodels.util.cflsolver.grammars.AliasModelsShortLimGrammar.AliasModelsShortGrammar;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.AliasModelsLimRelationManager.AliasModelsRelationManager;
import stamp.missingmodels.util.cflsolver.relation.AliasModelsShortLimRelationManager;
import stamp.missingmodels.util.cflsolver.relation.FilterRelationManager.AliasModelsShortFilterRelationManager;
import stamp.missingmodels.util.cflsolver.relation.FilterRelationManager.AliasModelsShortLimFilterRelationManager;
import stamp.missingmodels.util.cflsolver.util.AliasModelsUtils;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "alias-models-short-java")
public class AliasModelsShortAnalysis extends JavaAnalysis {
	public static Set<String> getVertexFilterHelper(Graph graphBar) {
		final Set<String> rejectedVertices = new HashSet<String>();
		for(int i=0; i<graphBar.getVertices().size(); i++) {
			if(graphBar.getVertices().get(i).startsWith("M")) {
				rejectedVertices.add(graphBar.getVertices().get(i));
			}
		}
		for(EdgeStruct edge : graphBar.getEdgeStructs(new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("FlowPreFull0") || edge.symbol.equals("FlowPreFull1"); }})) {
			rejectedVertices.remove(edge.sinkName);
		}
		return rejectedVertices;
	}
	
	public static Graph run(RelationReader reader, ContextFreeGrammarOpt grammar, RelationManager relations, RelationManager filterRelations, Set<String> vertexFilter) {
		System.out.println("Reading graph...");
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		System.out.println("Done!");
		System.out.println("Reading filter...");
		Filter<Edge> filter = new AndFilter(new GraphEdgeFilter(graph.getVertices(), grammar.getSymbols(), reader.readGraph(filterRelations, grammar.getSymbols())), new GraphVertexFilter(graph.getVertices(), vertexFilter));
		System.out.println("Done!");
		System.out.println("Computing transitive closure...");
		Graph graphBar = graph.transform(new ReachabilitySolver(graph.getVertices(), grammar, filter));
		System.out.println("Done!");
		//IOUtils.printGraphStatistics(graphBar);
		return graphBar;
	}
	
	// Run full analysis only to get active methods
	public static Graph getVertexFilterGraph() {
		return run(new ShordRelationReader(), new AliasModelsShortLimGrammar().getOpt(), new AliasModelsShortLimRelationManager(true), new AliasModelsShortLimFilterRelationManager(true), new HashSet<String>());
	}
	
	public static Graph getAliasModels() {
		return run(new ShordRelationReader(), new AliasModelsShortGrammar().getOpt(), new AliasModelsRelationManager(true), new AliasModelsShortFilterRelationManager(true), getVertexFilterHelper(getVertexFilterGraph()));
	}
	
	public static boolean checkActiveFlowNew() {
		ProgramRel relActiveFlowNew = (ProgramRel)ClassicProject.g().getTrgt("ActiveFlowNew");
		relActiveFlowNew.load();
		return relActiveFlowNew.size() > 0;
	}
	
	@Override
	public void run() {

		ProgramRel relFrameworkI = (ProgramRel)ClassicProject.g().getTrgt("FrameworkI");
		relFrameworkI.load();
		System.out.println("FrameworkI size: " + relFrameworkI.size());
		relFrameworkI.close();
		ProgramRel relEscapeH = (ProgramRel)ClassicProject.g().getTrgt("EscapeH");
		relEscapeH.load();
		System.out.println("EscapeH size: " + relEscapeH.size());
		relEscapeH.close();
		ProgramRel relLoad = (ProgramRel)ClassicProject.g().getTrgt("LoadNF");
		relLoad.load();
		System.out.println("Load size: " + relLoad.size());
		relLoad.close();
		ProgramRel relStore = (ProgramRel)ClassicProject.g().getTrgt("StoreNF");
		relStore.load();
		System.out.println("Store size: " + relStore.size());
		relStore.close();
		ProgramRel relAlloc = (ProgramRel)ClassicProject.g().getTrgt("AllocNF");
		relAlloc.load();
		System.out.println("Alloc size: " + relAlloc.size());
		relAlloc.close();
		ProgramRel relAssign = (ProgramRel)ClassicProject.g().getTrgt("AssignNF");
		relAssign.load();
		System.out.println("Assign size: " + relAssign.size());
		relAssign.close();
		System.out.println("PRINTING ACTIVE FLOW DYN");
		IOUtils.printRelation("ActiveFlowDynH");
		System.out.println("PRINTING PHANTOM OBJECT MODELS");
		IOUtils.printRelation("PhantomObjectDyn");
		
		IOUtils.printRelation("AllocNF");
		
		if(!checkActiveFlowNew()) {
			System.out.println("ERROR: No active flow edges found!");
			return;
		}
		for(List<EdgeStruct> model : AliasModelsUtils.SynthesisUtils.getModelsFromGraph(getAliasModels())) {
			System.out.println("PRINING MODEL");
			for(EdgeStruct edge : model) {
				System.out.println("MODEL EDGE: " + edge.toString(true));
			}
		}
	}
}
