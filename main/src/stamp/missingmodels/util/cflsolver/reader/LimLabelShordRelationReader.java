package stamp.missingmodels.util.cflsolver.reader;

import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import stamp.analyses.DomL;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Graph.EdgeTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.VertexMap;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;

public class LimLabelShordRelationReader implements RelationReader {
	public static Set<String> getLabels() {
		Set<String> labels = new HashSet<String>();
		
		// Source labels
		labels.add("$LOCATION");
		labels.add("$getLatitude");
		labels.add("$getLongitude");
		labels.add("$FINE_LOCATION");
		
		labels.add("$getDeviceId");
		labels.add("$SMS");
		labels.add("$CONTACTS");
		//labels.add("$AUDIO");
		//labels.add("$ACCOUNTS");
		labels.add("$CALENDAR");
		//labels.add("$CONTENT_PROVIDER");
		
		// Sink labels
		labels.add("!SOCKET");
		labels.add("!INTERNET");
		labels.add("!sendTextMessage");
		labels.add("!destinationAddress");
		labels.add("!sendDataMessage");
		labels.add("!sendMultipartTextMessage");
		
		return labels;
	}
	
	public static GraphTransformer getSourceSinkFilterTransformer(VertexMap vertices, SymbolMap symbols) {
		final Set<String> labels = getLabels();
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
	
	@Override
	public Iterable<EdgeStruct> readGraph(RelationManager relations, SymbolMap symbols) {
		Set<String> labels = new HashSet<String>();
		Set<EdgeStruct> edges = new HashSet<EdgeStruct>();
		for(EdgeStruct edge : new ShordRelationReader().readGraph(relations, symbols)) {
			DomL dom = (DomL)ClassicProject.g().getTrgt("L");
			String name = null;
			if(edge.sourceName.startsWith("L")) {
				name = dom.get(Integer.parseInt(edge.sourceName.substring(1)));
			} else if(edge.sinkName.startsWith("L")) {
				name = dom.get(Integer.parseInt(edge.sourceName.substring(1)));
			}
			if(name != null && !labels.contains(name)) {
				continue;
			}
			edges.add(edge);
		}
		return edges;
	}
}