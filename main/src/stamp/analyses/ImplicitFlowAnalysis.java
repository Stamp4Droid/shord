package stamp.analyses;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.processor.TraceReader;
import stamp.missingmodels.util.cflsolver.grammars.ImplicitFlowGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.relation.DynamicCallgraphRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "implicit-flow-java")
public class ImplicitFlowAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar implicitTaintGrammar = new ImplicitFlowGrammar();
	private static ContextFreeGrammar taintGrammar = new TaintGrammar();
	
	@Override
	public void run() {
		RelationReader relationReader = new ShordRelationReader();
		String[] tokens = System.getProperty("stamp.out.dir").split("_");
		RelationManager relations = new DynamicCallgraphRelationManager(TraceReader.getCallgraphList("../../profiler/traceouts/", tokens[tokens.length-1]));

		Graph gbar = new ReachabilitySolver(relationReader.readGraph(relations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		Graph gbari = new ReachabilitySolver(relationReader.readGraph(relations, implicitTaintGrammar), relationReader.readTypeFilter(implicitTaintGrammar)).getResult();
		
		System.out.println("Printing edges for taint grammar:");
		CFLSolverAnalysis.printGraphEdges(gbar, "Src2Sink");

		System.out.println("Printing edges for implicit taint grammar:");
		CFLSolverAnalysis.printGraphEdges(gbari, "Src2Sink");
	}
}
