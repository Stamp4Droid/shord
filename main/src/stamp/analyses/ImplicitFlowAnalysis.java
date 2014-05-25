package stamp.analyses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.grammars.ImplicitFlowGrammar;
import stamp.missingmodels.util.cflsolver.grammars.PointsToGrammar;
import stamp.missingmodels.util.cflsolver.grammars.TaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.Edge;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeFilter;
import stamp.missingmodels.util.cflsolver.relation.ImplicitFlowRelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.relation.RelationReader.ShordRelationReader;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver;
import chord.bddbddb.Dom;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "implicit-flow-java")
public class ImplicitFlowAnalysis extends JavaAnalysis {
	private static ContextFreeGrammar implicitTaintGrammar = new ImplicitFlowGrammar();
	private static ContextFreeGrammar taintGrammar = new TaintGrammar();
	
	
	private static void printGraphEdges(Graph g, final String symbol) {
		Set<String> edges = new HashSet<String>();
		for(Edge edge : g.getEdges(new EdgeFilter() {
			@Override
			public boolean filter(Edge edge) {
				return edge.getSymbol().equals(symbol); 
			}})) {
			
			Dom<?> domSource = (Dom<?>)ClassicProject.g().getTrgt(Character.toString(edge.source.name.charAt(0)));
			Dom<?> domSink = (Dom<?>)ClassicProject.g().getTrgt(Character.toString(edge.sink.name.charAt(0)));
			
			edges.add(edge.getSymbol() + ": " + domSource.get(Integer.parseInt(edge.source.name.substring(1))) + " -> " + domSink.get(Integer.parseInt(edge.sink.name.substring(1))));
		}
		List<String> edgeList = new ArrayList<String>(edges);
		Collections.sort(edgeList);
		System.out.println("Printing " + symbol + " edges:");
		for(String edge : edgeList) {
			System.out.println(edge);
		}
	}
	
	public static void printRelation(String relationName) {
		System.out.println("Printing " + relationName);
		ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relationName);
		rel.load();
		for(Object[] tuple : rel.getAryNValTuples()) {
			StringBuilder sb = new StringBuilder();
			sb.append(relationName).append(", ");
			for(int i=0; i<tuple.length-1; i++) {
				sb.append(tuple[i]).append(", ");
			}
			sb.append(tuple[tuple.length-1]);
			System.out.println(sb.toString());
		}
		rel.close();
	}
	
	@Override
	public void run() {
		RelationReader relationReader = new ShordRelationReader();
		RelationManager relations = new ImplicitFlowRelationManager();

		Graph gbar = new ReachabilitySolver(relationReader.readGraph(relations, taintGrammar), relationReader.readTypeFilter(taintGrammar)).getResult();
		Graph gbari = new ReachabilitySolver(relationReader.readGraph(relations, implicitTaintGrammar), relationReader.readTypeFilter(implicitTaintGrammar)).getResult();
		
		System.out.println("Printing edges for taint grammar:");
		printGraphEdges(gbar, "Src2Sink");
		
		System.out.println("Printing edges for implicit taint grammar:");
		printGraphEdges(gbari, "Src2Sink");

		printGraphEdges(gbari, "Label2Prim");
		printGraphEdges(gbari, "Label2Ref");
		printGraphEdges(gbari, "prim2RefImp");
		printGraphEdges(gbari, "Flow");
	}
}
