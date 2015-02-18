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

public class G2 extends Graph {
	public G2(RelationManager relations) {
		for(int i=0; i<this.numKinds(); i++) {
			String symbol = this.kindToSymbol(i);
			for(Relation relation : relations.getRelationsBySymbol(symbol)) {
				readRelation(this, relation);
			}
		}
	}
	
	private static void readRelation(Graph g, Relation relation) {
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

			g.addWeightedInputEdge(source, sink, g.symbolToKind(symbol), field.field, (short)weight);
		}
		
		rel.close();
	}
	
	private final ContextFreeGrammarOpt c = new ContextFreeGrammarOpt(new MissingRefRefTaintGrammar());

	public int numKinds() {
		return c.getNumLabels();
	}

	public int symbolToKind(String symbol) {
		return c.getSymbolInt(symbol);
	}

	public String kindToSymbol(int kind) {
		return c.getSymbol(kind);
	}

	public void process(Edge edge) {
		// <-, ->
		for(UnaryProduction unaryProduction : c.unaryProductionsByInput[edge.symbolInt]) {
			this.addEdge2(unaryProduction, edge);
		}
		// <- <-, <- ->, -> <-, -> ->
		for(BinaryProduction binaryProduction : c.binaryProductionsByFirstInput[edge.symbolInt]) {
			Vertex intermediate = binaryProduction.isFirstInputBackwards ? edge.source : edge.sink;
			Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getInEdges(binaryProduction.secondInput) : intermediate.getOutEdges(binaryProduction.secondInput);
			for(Edge secondEdge : secondEdges) {
				this.addEdge2(binaryProduction, edge, secondEdge);
			}
		}
		for(BinaryProduction binaryProduction : c.binaryProductionsBySecondInput[edge.symbolInt]) {
			Vertex intermediate = binaryProduction.isSecondInputBackwards ? edge.sink : edge.source;
			Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutEdges(binaryProduction.firstInput) : intermediate.getInEdges(binaryProduction.firstInput);
			for(Edge firstEdge : firstEdges) {
				this.addEdge2(binaryProduction, firstEdge, edge);
			}
		}
	}
	
	private void addEdge2(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Vertex source = unaryProduction.isInputBackwards ? input.sink : input.source;
		Vertex sink = unaryProduction.isInputBackwards ? input.source : input.sink;
		int symbolInt = unaryProduction.target;
		
		// add edge
		this.addEdge(source, sink, symbolInt, input, !unaryProduction.ignoreFields);
	}

	private void addEdge2(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Vertex source = binaryProduction.isFirstInputBackwards ? firstInput.sink : firstInput.source;
		Vertex sink = binaryProduction.isSecondInputBackwards ? secondInput.source : secondInput.sink;
		int symbolInt = binaryProduction.target;
		
		// add edge
		this.addEdge(source, sink, symbolInt, firstInput, secondInput, !binaryProduction.ignoreFields);
	}

}
