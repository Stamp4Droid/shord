package stamp.missingmodels.util.cflsolver.reader;

import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;
import stamp.missingmodels.util.jcflsolver2.Graph;
import stamp.missingmodels.util.jcflsolver2.Graph.GraphBuilder;
import stamp.missingmodels.util.jcflsolver2.RelationManager;
import stamp.missingmodels.util.jcflsolver2.RelationManager.Relation;
import stamp.missingmodels.util.jcflsolver2.RelationManager.RelationReader;
import stamp.missingmodels.util.jcflsolver2.TypeFilter;

public class ShordRelationReader implements RelationReader {
	@Override
	public Graph readGraph(RelationManager relations, ContextFreeGrammar contextFreeGrammar) {
		GraphBuilder gb = new GraphBuilder(contextFreeGrammar.getOpt());
		for(int i=0; i<gb.getGraph().getContextFreeGrammarOpt().getSymbols().getNumSymbols(); i++) {
			String symbol = gb.getGraph().getContextFreeGrammarOpt().getSymbols().get(i).symbol;
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
		
		/*
		GraphTransformer gt = new EdgeTransformer() {
			@Override
			public void process(GraphBuilder gb, EdgeStruct edgeStruct) {
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
		*/
		
		//return gt.transform(gb.toGraph());
		return gb.getGraph();
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
		/*
		TypeFilter t = new TypeFilter(contextFreeGrammar);
		
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt("ptd");
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			t.add("H" + Integer.toString(tuple[1]), "V" + Integer.toString(tuple[0]));
		}
		
		rel.close();		
		
		return t;
		*/
		return null;
	}
}