package stamp.analyses;

import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.EdgeTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphEdgeFilter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.VertexMap;
import stamp.missingmodels.util.cflsolver.core.ReachabilitySolver;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "cflsolver-java")
public class CFLSolverAnalysis extends JavaAnalysis {
	public static GraphTransformer getSourceSinkFilterTransformer(VertexMap vertices, SymbolMap symbols) {
		final Set<String> labels = new HashSet<String>();
		// Source labels
		labels.add("$LOCATION");
		labels.add("$getLatitude");
		labels.add("$getLongitude");
		labels.add("$FINE_LOCATION");
		labels.add("$CONTACTS");
		labels.add("$getDeviceId");
		//labels.add("$SMS");
		//labels.add("$AUDIO");
		//labels.add("$ACCOUNTS");
		//labels.add("$CALENDAR");
		//labels.add("$CONTENT_PROVIDER");
		
		// Sink labels
		labels.add("!INTERNET");
		labels.add("!SOCKET");
		labels.add("!sendTextMessage");
		labels.add("!destinationAddress");
		labels.add("!sendDataMessage");
		labels.add("!sendMultipartTextMessage");
		
		final DomL dom = (DomL)ClassicProject.g().getTrgt("L");
		final Filter<EdgeStruct> filter = new Filter<EdgeStruct>() {
			public boolean filter(EdgeStruct edgeStruct) {
				String name = null;
				if(edgeStruct.sourceName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				} else if(edgeStruct.sinkName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				}
				return name == null || labels.contains(name);
			}
		};
		return new EdgeTransformer(vertices, symbols) {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct) {
				if(filter.filter(edgeStruct)) {
					gb.addOrUpdateEdge(edgeStruct);
				}
			}
		};
	}
	
	public static void run(RelationReader reader, ContextFreeGrammarOpt grammar, RelationManager relations, RelationManager filterRelations) {
		Graph graph = Graph.getGraph(grammar.getSymbols(), reader.readGraph(relations, grammar.getSymbols()));
		graph = graph.transform(getSourceSinkFilterTransformer(graph.getVertices(), grammar.getSymbols()));
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
