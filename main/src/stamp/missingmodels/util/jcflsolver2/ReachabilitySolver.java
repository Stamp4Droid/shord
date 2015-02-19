package stamp.missingmodels.util.jcflsolver2;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefGrammar.MissingRefRefTaintGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager.Relation;

public class ReachabilitySolver {
	private final ContextFreeGrammarOpt c = new ContextFreeGrammarOpt(new MissingRefRefTaintGrammar());
	public final Graph g;
	
	public ReachabilitySolver(RelationManager relations) {
		this.g = new Graph(this.c, this);
		for(int i=0; i<c.getNumLabels(); i++) {
			String symbol = c.getSymbol(i);
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				readRelation(g, relation);
			}
		}
	}
	
	private void readRelation(Graph g, Relation relation) {
		final ProgramRel rel = (ProgramRel)ClassicProject.g().getTrgt(relation.getName());
		rel.load();
		
		Iterable<int[]> res = rel.getAryNIntTuples();
		for(int[] tuple : res) {
			if(!relation.filter(tuple)) {
				return;
			}
		
			String source = relation.getSource(tuple);
			String sink = relation.getSink(tuple);
			String symbol = relation.getSymbol();
			Field field = relation.getField(tuple);
			//Context context = relation.getContext(tuple);
			int weight = relation.getWeight(tuple);

			g.addEdge(source, sink, c.getSymbolInt(symbol), field.field, (short)weight);
		}
		
		rel.close();
	}

	public void addEdgeToWorklist(Edge newEdge, Edge oldEdge) {
		if(oldEdge == null) {
			count++;
			this.worklist.push(newEdge);
		} else {
			this.worklist.update(oldEdge, newEdge);
		}
	}

	private BucketHeap worklist = new BucketHeap();
	
	private int count;
	private long time;

	private void addEdge(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = unaryProduction.target;
		
		// get field
		int field = unaryProduction.ignoreFields ? -1 : Math.max(symbolInt, -1);
		
		// add edge
		this.g.addEdge(source, sink, symbolInt, field, input.weight, input, null);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		int symbolInt = binaryProduction.target;
		
		// add edge
		if((firstInput.field == -1) || (secondInput.field == -1) || (firstInput.field == secondInput.field)) {
			int field = binaryProduction.ignoreFields ? -1 : Math.max(firstInput.field, secondInput.field);
			this.g.addEdge(source, sink, symbolInt, field, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);
		}
	}
	
	public void process() {
		System.out.println("Initial num of edges = " + this.worklist.size());
		this.time = System.currentTimeMillis();
		
		while(this.worklist.size() != 0) {
			Edge edge = this.worklist.pop();
			// <-, ->
			for(UnaryProduction unaryProduction : c.unaryProductionsByInput[edge.symbolInt]) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : c.binaryProductionsByFirstInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput) : intermediate.getOutgoingEdges(binaryProduction.secondInput);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : c.binaryProductionsBySecondInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput) : intermediate.getIncomingEdges(binaryProduction.firstInput);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
		}

		long totalTime = System.currentTimeMillis() - this.time;
		System.out.println("Total num of edges = " + count);
		System.out.println("Time: " + totalTime);
		System.out.println("Rate: " + ((double)count/totalTime) + " edges/ms");
	}
}
