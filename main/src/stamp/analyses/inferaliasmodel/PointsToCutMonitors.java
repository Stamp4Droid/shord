package stamp.analyses.inferaliasmodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.core.AbductiveInference;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.AuxProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.BinaryProduction;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.UnaryProduction;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.grammars.PointsToGrammar;
import stamp.missingmodels.util.cflsolver.reader.LimLabelShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.PointsToRelationManager;

public class PointsToCutMonitors {
	private static Set<String> libraryVertices = null;
	private static Set<String> getLibraryVertices() {
		if(libraryVertices == null) {
			libraryVertices = new HashSet<String>();
			ProgramRel relFrameworkVar = (ProgramRel)ClassicProject.g().getTrgt("FrameworkVar");
			relFrameworkVar.load();
			for(int[] var : relFrameworkVar.getAryNIntTuples()) {
				libraryVertices.add("V" + var[0]);
			}
			relFrameworkVar.close();
			ProgramRel relFrameworkAlloc = (ProgramRel)ClassicProject.g().getTrgt("FrameworkObj");
			relFrameworkAlloc.load();
			for(int[] alloc : relFrameworkAlloc.getAryNIntTuples()) {
				libraryVertices.add("H" + alloc[0]);
			}
			relFrameworkAlloc.close();
		}
		return libraryVertices;
	}
	
	private static Filter<EdgeStruct> getBaseEdgeFilter(Iterable<EdgeStruct> ptEdges) {
		Set<String> libraryVertices = getLibraryVertices();
		final Set<EdgeStruct> basePtEdges = new HashSet<EdgeStruct>();
		for(EdgeStruct ptEdge : ptEdges) {
			if(!ptEdge.symbol.equals("Flow")) {
				throw new RuntimeException("Invalid points-to edge!");
			}
			if(!libraryVertices.contains(ptEdge.sourceName) && !libraryVertices.contains(ptEdge.sinkName)) {
				basePtEdges.add(ptEdge);
			}
		}
		return new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return basePtEdges.contains(edge); }};
	}
	
	private static class PointsToWeightedGrammar extends ContextFreeGrammar {
		private PointsToWeightedGrammar() {
			PointsToGrammar grammar = new PointsToGrammar();
			for(List<UnaryProduction> unaryProductions : grammar.unaryProductionsByTarget) {
				for(UnaryProduction unaryProduction : unaryProductions) {
					this.addUnaryProduction(unaryProduction.target.symbol, unaryProduction.input.symbol, unaryProduction.isInputBackwards, unaryProduction.ignoreFields, unaryProduction.ignoreContexts, (short)1);
				}
			}
			for(List<BinaryProduction> binaryProductions : grammar.binaryProductionsByTarget) {
				for(BinaryProduction binaryProduction : binaryProductions) {
					this.addBinaryProduction(binaryProduction.target.symbol, binaryProduction.firstInput.symbol, binaryProduction.secondInput.symbol, binaryProduction.isFirstInputBackwards, binaryProduction.isSecondInputBackwards, binaryProduction.ignoreFields, binaryProduction.ignoreContexts, (short)1);
				}
			}
			for(List<AuxProduction> auxProductions : grammar.auxProductionsByTarget) {
				for(AuxProduction auxProduction : auxProductions) {
					this.addAuxProduction(auxProduction.target.symbol, auxProduction.input.symbol, auxProduction.auxInput.symbol, auxProduction.isAuxInputFirst, auxProduction.isInputBackwards, auxProduction.isAuxInputBackwards, auxProduction.ignoreFields, auxProduction.ignoreContexts, (short)1);
				}
			}
		}
	}
	
	private static MultivalueMap<EdgeStruct,Integer> getFrameworkPointsToCuts(Iterable<EdgeStruct> ptEdges) {
		// STEP 1: Configuration
		RelationReader reader = new LimLabelShordRelationReader();
		RelationManager relations = new PointsToRelationManager();
		ContextFreeGrammarOpt grammar = new PointsToWeightedGrammar().getOpt();
		
		// STEP 2: Compute transitive closure
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		Filter<Edge> filter = new Filter<Edge>() { public boolean filter(Edge edge) { return true; }};
		Graph graphBar = new ReachabilitySolver(graph.getVertices(), grammar, filter).transform(graph.getEdgeStructs());
		
		// STEP 3: Set up abduction inference
		final Set<EdgeStruct> ptEdgeSet = new HashSet<EdgeStruct>();
		for(EdgeStruct ptEdge : ptEdges) {
			System.out.println("INITIAL EDGE: " + ptEdge);
			ptEdgeSet.add(ptEdge);
		}
		Filter<EdgeStruct> baseEdgeFilter = getBaseEdgeFilter(graphBar.getEdgeStructs(new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return edge.symbol.equals("Flow"); }}));
		Filter<EdgeStruct> weightedEdgeFilter = new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return false; }};
		Filter<EdgeStruct> initialEdgeFilter = new Filter<EdgeStruct>() { public boolean filter(EdgeStruct edge) { return ptEdgeSet.contains(edge); }};
		
		// STEP 4: Perform abduction
		return new AbductiveInference(grammar).process(weightedEdgeFilter, baseEdgeFilter, initialEdgeFilter, graph, filter, 1);
	}
	
	public static void printMonitors(Iterable<EdgeStruct> ptEdges) {
		MultivalueMap<EdgeStruct,Integer> cutPtEdges = getFrameworkPointsToCuts(ptEdges);
		for(EdgeStruct cutPtEdge : cutPtEdges.keySet()) {
			System.out.println("CUT PT EDGE: " + cutPtEdge.toString(true));
		}
	}
}
