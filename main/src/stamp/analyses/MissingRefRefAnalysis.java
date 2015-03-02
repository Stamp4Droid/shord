package stamp.analyses;

import java.util.HashSet;

import lpsolve.LpSolveException;
import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.abduction.AbductiveInferenceRunner;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar.TaintPointsToGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.reader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.DynamicParamRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.jcflsolver2.AbductiveInferenceRunner2;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.Edge;
import stamp.missingmodels.util.jcflsolver2.Edge.EdgeStruct;
import stamp.missingmodels.util.jcflsolver2.Graph2;
import stamp.missingmodels.util.jcflsolver2.RelationManager;
import stamp.missingmodels.util.jcflsolver2.RelationManager.RelationReader;
import stamp.missingmodels.util.jcflsolver2.Util.MultivalueMap;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "missing-refref-java")
public class MissingRefRefAnalysis extends JavaAnalysis {
	public static HashSet<Graph.Edge> graphEdges = new HashSet<Graph.Edge>();
	public static HashSet<Edge> graph2Edges = new HashSet<Edge>();
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
		ContextFreeGrammar taintGrammar = new TaintPointsToGrammar();
		Graph g = relationReader.readGraph(relations, taintGrammar);
		TypeFilter t = relationReader.readTypeFilter(taintGrammar);
		try {
			MultivalueMap<Graph.EdgeStruct,Integer> results = AbductiveInferenceRunner.runInference(g, t, true, 2);
		} catch (LpSolveException e1) {
			e1.printStackTrace();
		}
		
		Graph2 g2 = new Graph2(new TaintPointsToGrammar().getOpt(), new DynamicParamRelationManager(new MultivalueMap<String,String>()));
		try {
			MultivalueMap<EdgeStruct,Integer> results2 = AbductiveInferenceRunner2.runInference(g2, true, 2);
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		//IOUtils.printAbductionResult(results, true);
		
		HashSet<String> graphEdgeStrings = new HashSet<String>();
		for(Graph.Edge edge : graphEdges) {
			graphEdgeStrings.add(edge.source.name + "-" + edge.symbol.symbol + "[" + edge.field.field + "]-" + edge.sink.name);
		}
		HashSet<String> graph2EdgeStrings = new HashSet<String>();
		for(Edge edge : graph2Edges) {
			graph2EdgeStrings.add(edge.toString());
		}
		
		System.out.println("DIFF1:");
		for(String edge : graphEdgeStrings) {
			if(!graph2EdgeStrings.contains(edge)) {
				System.out.println(edge);
			}
		}
		System.out.println("DIFF2:");
		for(String edge : graph2EdgeStrings) {
			if(!graphEdgeStrings.contains(edge)) {
				System.out.println(edge);
			}
		}
	}
}
