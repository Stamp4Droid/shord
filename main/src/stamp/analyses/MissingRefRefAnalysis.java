package stamp.analyses;

import java.util.HashSet;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefTaintGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Context;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.relation.MissingRefRefRelationManager.MissingRefRefTaintRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.relation.TaintWithContextRelationManager;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import stamp.missingmodels.util.jcflsolver2.Edge;
import stamp.missingmodels.util.jcflsolver2.Graph2;
import stamp.missingmodels.util.jcflsolver2.ReachabilitySolver2;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "missing-refref-java")
public class MissingRefRefAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar missingRefRefTaintGrammar = new MissingRefRefTaintGrammar();
	private static ContextFreeGrammar taintGrammar = new TaintGrammar();
	
	@Override
	public void run() {
		Graph2 gbar = new ReachabilitySolver2(new Graph2(new ContextFreeGrammarOpt(new MissingRefRefTaintGrammar()), new MissingRefRefTaintRelationManager())).process();
				
		RelationReader relationReader = new ShordRelationReader();
		RelationManager missingRefRefRelations = new MissingRefRefTaintRelationManager();
		RelationManager taintRelations = new TaintWithContextRelationManager();
		
		/*
		Graph gbar = new ReachabilitySolver(relationReader.readGraph(taintRelations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		
		System.out.println("Printing taint grammar statistics:");
		IOUtils.printGraphStatistics(gbar);
		
		System.out.println("Printing edges for taint grammar:");
		IOUtils.printGraphEdges(gbar, "Src2Sink", true);
		*/
		
		Graph gbarr = new ReachabilitySolver(relationReader.readGraph(missingRefRefRelations, missingRefRefTaintGrammar), relationReader.readTypeFilter(missingRefRefTaintGrammar)).getResult();
		
		Set<EdgeStruct> edgeStructs = new HashSet<EdgeStruct>();
		for(Graph.Edge edge : gbarr.getEdges()) {
			edgeStructs.add(edge.getStruct());
		}

		Set<EdgeStruct> edgeStructs2 = new HashSet<EdgeStruct>();
		for(Edge edge : gbar) {
			edgeStructs2.add(new EdgeStruct(edge.source.name, edge.sink.name, gbar.c.getSymbol(edge.symbolInt), new Field(edge.field), Context.DEFAULT_CONTEXT));
		}
		
		System.out.println("Common:");
		for(EdgeStruct edge : edgeStructs) {
			//if(!edgeStructs2.contains(edge)) {
				System.out.println(edge);
			//}
		}
		System.out.println("Diff:");
		for(EdgeStruct edge : edgeStructs2) {
			if(!edgeStructs.contains(edge)) {
				System.out.println(edge);
			}
		}
		
		System.out.println("Printing missing refref taint grammar statistics:");
		IOUtils.printGraphStatistics(gbarr);
		
		System.out.println("Printing edges for missing refref taint grammar:");
		IOUtils.printGraphEdges(gbarr, "Src2Sink", true);
		
	}
}
