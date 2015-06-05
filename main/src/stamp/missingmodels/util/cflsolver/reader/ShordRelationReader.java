package stamp.missingmodels.util.cflsolver.reader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.analyses.DomL;
import stamp.missingmodels.util.cflsolver.core.ContextFreeGrammar.SymbolMap;
import stamp.missingmodels.util.cflsolver.core.Edge;
import stamp.missingmodels.util.cflsolver.core.Edge.EdgeStruct;
import stamp.missingmodels.util.cflsolver.core.Edge.Field;
import stamp.missingmodels.util.cflsolver.core.Graph;
import stamp.missingmodels.util.cflsolver.core.Graph.EdgeTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphEdgeFilter;
import stamp.missingmodels.util.cflsolver.core.Graph.GraphTransformer;
import stamp.missingmodels.util.cflsolver.core.Graph.SimpleGraphBuilder;
import stamp.missingmodels.util.cflsolver.core.Graph.VertexMap;
import stamp.missingmodels.util.cflsolver.core.RelationManager;
import stamp.missingmodels.util.cflsolver.core.RelationManager.Relation;
import stamp.missingmodels.util.cflsolver.core.RelationManager.RelationReader;
import stamp.missingmodels.util.cflsolver.core.Util.Filter;

public class ShordRelationReader implements RelationReader {
	private final Filter<EdgeStruct> filter;
	
	public ShordRelationReader(Filter<EdgeStruct> filter) {
		this.filter = filter;
	}
	
	public ShordRelationReader() {
		final Set<String> labels = new HashSet<String>();

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
		
		labels.add("!INTERNET");
		
		labels.add("!SOCKET");
		
		labels.add("!sendTextMessage");
		labels.add("!destinationAddress");
		labels.add("!sendDataMessage");
		labels.add("!sendMultipartTextMessage");
		
		//labels.add("!WebView");

		final DomL dom = (DomL)ClassicProject.g().getTrgt("L");
		
		this.filter = new Filter<EdgeStruct>() {
			public boolean filter(EdgeStruct edgeStruct) {
				String name = null;
				if(edgeStruct.sourceName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				} else if(edgeStruct.sinkName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				}
				return name == null || labels.contains(name);
			}};
	}
	
	@Override
	public Graph readGraph(RelationManager relations, SymbolMap symbols) {
		SimpleGraphBuilder gb = new SimpleGraphBuilder(symbols);
		for(int i=0; i<symbols.getNumSymbols(); i++) {
			String symbol = symbols.get(i).symbol;
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				readRelation(gb, relation);
			}
		}
		
		GraphTransformer gt = new EdgeTransformer(gb.getGraph().getVertices(), gb.getGraph().getSymbols()) {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct) {
				if(filter.filter(edgeStruct)) {
					gb.addOrUpdateEdge(edgeStruct);
				}
			}};
		
		return gb.getGraph().transform(gt);
	}
	
	private static void readRelation(SimpleGraphBuilder gb, Relation relation) {
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getName());
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			relation.addEdge(gb, tuple);
		}
		
		rel.close();
	}

	@Override
	public Filter<Edge> readFilter(VertexMap vertices, SymbolMap symbols) {
		List<EdgeStruct> edges = new ArrayList<EdgeStruct>();
		
		ProgramRel relPt = (ProgramRel)ClassicProject.g().getTrgt("ptd");
		relPt.load();
		
		Iterable<int[]> resPt = relPt.getAryNIntTuples();
		for(int[] tuple : resPt) {
			String sourceName = "H" + Integer.toString(tuple[1]);
			String sinkName = "V" + Integer.toString(tuple[0]);
			if(!vertices.contains(sourceName) || !vertices.contains(sinkName)) {
				continue;
			}
			edges.add(new EdgeStruct(sourceName, sinkName, "Flow", Field.DEFAULT_FIELD.field, (short)0));
		}		
		relPt.close();
		
		ProgramRel relPrim = (ProgramRel)ClassicProject.g().getTrgt("Label2Primd");
		relPrim.load();
		
		Iterable<int[]> resPrim = relPrim.getAryNIntTuples();
		for(int[] tuple : resPrim) {
			String sourceName = "L" + Integer.toString(tuple[0]);
			String sinkName = "U" + Integer.toString(tuple[1]);
			if(!vertices.contains(sourceName) || !vertices.contains(sinkName)) {
				continue;
			}
			edges.add(new EdgeStruct(sourceName, sinkName, "Label2Prim", Field.DEFAULT_FIELD.field, (short)0));
		}
		relPrim.close();
		
		return new GraphEdgeFilter(vertices, symbols, edges);
	}
}