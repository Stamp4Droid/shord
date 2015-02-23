package stamp.missingmodels.util.cflsolver.reader;

import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.analyses.DomL;
import stamp.missingmodels.util.cflsolver.graph.Graph;
import stamp.missingmodels.util.cflsolver.graph.Graph.EdgeStruct;
import stamp.missingmodels.util.cflsolver.graph.GraphBuilder;
import stamp.missingmodels.util.cflsolver.graph.GraphTransformer;
import stamp.missingmodels.util.cflsolver.relation.RelationReader;
import stamp.missingmodels.util.cflsolver.solver.ReachabilitySolver.TypeFilter;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.RelationManager;
import stamp.missingmodels.util.jcflsolver2.RelationManager.Relation;

public class ShordRelationReader implements RelationReader {

	@Override
	public Graph readGraph(RelationManager relations, ContextFreeGrammar contextFreeGrammar) {
		GraphBuilder gb = new GraphBuilder(contextFreeGrammar);
		for(int i=0; i<gb.toGraph().getContextFreeGrammar().getNumLabels(); i++) {
			String symbol = gb.toGraph().getContextFreeGrammar().getSymbol(i);
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				readRelation(gb, relation);
			}
		}
		
		final Set<String> labels = new HashSet<String>();
		labels.add("$LOCATION");
		labels.add("$getLatitude");
		labels.add("$getLongitude");
		labels.add("$FINE_LOCATION");

		labels.add("$getDeviceId");
		//labels.add("$AUDIO");

		//labels.add("$SMS");
		//labels.add("$ACCOUNTS");
		//labels.add("$CONTACTS");
		//labels.add("$CALENDAR");
		//labels.add("$CONTENT_PROVIDER");

		labels.add("!SOCKET");
		labels.add("!INTERNET");
		labels.add("!sendTextMessage");
		labels.add("!destinationAddress");
		labels.add("!sendDataMessage");
		labels.add("!sendMultipartTextMessage");			
		labels.add("!WebView");
		
		GraphTransformer gt = new GraphTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct, int weight) {
				DomL dom = (DomL)ClassicProject.g().getTrgt("L");
				String name = null;
				if(edgeStruct.sourceName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				} else if(edgeStruct.sinkName.startsWith("L")) {
					name = dom.get(Integer.parseInt(edgeStruct.sourceName.substring(1)));
				}
				if(name != null && !labels.contains(name)) {
					return;
				}
				gb.addEdge(edgeStruct, weight);
			}
		};
		
		return gt.transform(gb.toGraph());
	}
	
	private static void readRelation(GraphBuilder gb, Relation relation) {
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getName());
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			relation.addEdge(gb, tuple);
		}
		
		rel.close();
	}

	@Override
	public TypeFilter readTypeFilter(ContextFreeGrammar contextFreeGrammar) {
		TypeFilter t = new TypeFilter(contextFreeGrammar);
		
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("ptd");
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			t.add("H" + Integer.toString(tuple[1]), "V" + Integer.toString(tuple[0]));
		}
		
		rel.close();		
		
		return t;
	}
}