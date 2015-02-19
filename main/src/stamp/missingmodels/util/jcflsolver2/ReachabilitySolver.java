package stamp.missingmodels.util.jcflsolver2;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.AuxProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.UnaryProduction;
import stamp.missingmodels.util.cflsolver.graph.EdgeData.Field;
import stamp.missingmodels.util.cflsolver.relation.RelationManager;
import stamp.missingmodels.util.cflsolver.relation.RelationManager.Relation;

public class ReachabilitySolver {
	public final ContextFreeGrammarOpt c;
	public final Graph g;
	
	public ReachabilitySolver(ContextFreeGrammar c, RelationManager relations) {
		this.c = new ContextFreeGrammarOpt(c);
		this.g = new Graph(this.c);
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

			this.addEdgeHelper(source, sink, c.getSymbolInt(symbol), field.field, (short)weight);
		}
		
		rel.close();
	}

	private BucketHeap worklist = new BucketHeap();
	
	private int count;
	private long time;
	
	public void addEdgeHelper(String sourceName, String sinkName, int symbolInt, int field, short weight) {
		this.addEdgeHelper(this.g.getVertex(sourceName), this.g.getVertex(sinkName), symbolInt, field, weight, null, null);
		/*
		Edge edge = new Edge(symbolInt, this.getVertex(sourceName), this.getVertex(sinkName), field);
		edge.weight = weight;
		
		Edge oldEdge = this.getVertex(sourceName).getCurrentOutgoingEdge(edge);
		if(oldEdge == null) {
			this.getVertex(sourceName).addOutgoingEdge(edge);
			this.getVertex(sinkName).addIncomingEdge(edge);
		}
		s.addEdgeToWorklist(edge, oldEdge);
		*/
	}
	
	public void addEdgeHelper(Vertex source, Vertex sink, int symbolInt, int field, short weight, Edge firstInput, Edge secondInput) {
		Edge edge = new Edge(symbolInt, source, sink, field);
		edge.weight = weight;

		edge.firstInput = firstInput;
		edge.secondInput = secondInput;

		Edge oldEdge = source.getCurrentOutgoingEdge(edge);
		if(oldEdge == null) {
			this.count++;
			source.addOutgoingEdge(edge);
			sink.addIncomingEdge(edge);
			this.worklist.push(edge);
		} else {
			if(edge.weight < oldEdge.weight) {
				oldEdge.weight = edge.weight;
				oldEdge.firstInput = edge.firstInput;
				oldEdge.secondInput = edge.secondInput;
				this.worklist.update(oldEdge);
			}
		}
	}

	private void addEdge(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = unaryProduction.target;
		
		// get field
		int field = unaryProduction.ignoreFields ? -1 : Math.max(symbolInt, -1);
		
		// add edge
		this.addEdgeHelper(source, sink, symbolInt, field, input.weight, input, null);
	}

	private void addEdge(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		int symbolInt = binaryProduction.target;
		
		// add edge
		if((firstInput.field == -1) || (secondInput.field == -1) || (firstInput.field == secondInput.field)) {
			int field = binaryProduction.ignoreFields ? -1 : Math.max(firstInput.field, secondInput.field);
			this.addEdgeHelper(source, sink, symbolInt, field, (short)(firstInput.weight + secondInput.weight), firstInput, secondInput);
		}
	}

	private void addEdge(AuxProduction auxProduction, Edge input, Edge auxInput) {
		// get edge base
		Vertex source = auxProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = auxProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = auxProduction.target;
		
		// add edge
		if((input.field == -1) || (auxInput.field == -1) || (input.field == auxInput.field)) {
			int field = auxProduction.ignoreFields ? -1 : Math.max(input.field, auxInput.field);
			this.addEdgeHelper(source, sink, symbolInt, field, (short)(input.weight + auxInput.weight), input, auxInput);
		}
	}
	
	public void process() {
		System.out.println("Initial num of edges = " + this.worklist.size());
		this.time = System.currentTimeMillis();
		
		while(this.worklist.size() != 0) {
			Edge edge = this.worklist.pop();
			// <-, ->
			for(UnaryProduction unaryProduction : this.c.unaryProductionsByInput[edge.symbolInt]) {
				this.addEdge(unaryProduction, edge);
			}
			// <- <-, <- ->, -> <-, -> ->
			for(BinaryProduction binaryProduction : this.c.binaryProductionsByFirstInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getIncomingEdges(binaryProduction.secondInput) : intermediate.getOutgoingEdges(binaryProduction.secondInput);
				for(Edge secondEdge : secondEdges) {
					this.addEdge(binaryProduction, edge, secondEdge);
				}
			}
			for(BinaryProduction binaryProduction : this.c.binaryProductionsBySecondInput[edge.symbolInt]) {
				Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutgoingEdges(binaryProduction.firstInput) : intermediate.getIncomingEdges(binaryProduction.firstInput);
				for(Edge firstEdge : firstEdges) {
					this.addEdge(binaryProduction, firstEdge, edge);
				}
			}
			// <- <-, <- ->, -> <-, -> ->
			for(AuxProduction auxProduction : this.c.auxProductionsByInput[edge.symbolInt]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? edge.sink : edge.source;
				Iterable<Edge> auxEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? intermediate.getOutgoingEdges(auxProduction.auxInput) : intermediate.getIncomingEdges(auxProduction.auxInput);
				for(Edge auxEdge : auxEdges) {
					this.addEdge(auxProduction, edge, auxEdge);
				}
			}
			for(AuxProduction auxProduction : this.c.auxProductionsByAuxInput[edge.symbolInt]) {
				Vertex intermediate = (!auxProduction.isAuxInputFirst) ^ auxProduction.isAuxInputBackwards ? edge.source : edge.sink;
				Iterable<Edge> inputEdges = (!auxProduction.isAuxInputFirst) ^ auxProduction.isInputBackwards ? intermediate.getIncomingEdges(auxProduction.input) : intermediate.getOutgoingEdges(auxProduction.input);
				for(Edge inputEdge : inputEdges) {
					this.addEdge(auxProduction, inputEdge, edge);
				}
			}
		}

		long totalTime = System.currentTimeMillis() - this.time;
		System.out.println("Total num of edges = " + count);
		System.out.println("Time: " + totalTime);
		System.out.println("Rate: " + ((double)count/totalTime) + " edges/ms");
	}
}
