package stamp.missingmodels.jgrammars;

import stamp.missingmodels.util.cflsolver.grammars.MissingRefRefJGrammar;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.BinaryProduction;
import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammarOpt.UnaryProduction;
import stamp.missingmodels.util.jcflsolver.Edge;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.jcflsolver.Node;

public class G extends Graph {
	private static final ContextFreeGrammarOpt c = new ContextFreeGrammarOpt(new MissingRefRefJGrammar());

	public boolean isTerminal(int kind) {
		return true;
	}

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
		for(UnaryProduction unaryProduction : c.unaryProductionsByInput[edge.kind]) {
			this.addEdge2(unaryProduction, edge);
		}
		// <- <-, <- ->, -> <-, -> ->
		for(BinaryProduction binaryProduction : c.binaryProductionsByFirstInput[edge.kind]) {
			Node intermediate = binaryProduction.isFirstInputBackwards ? edge.from : edge.to;
			Iterable<Edge> secondEdges = binaryProduction.isSecondInputBackwards ? intermediate.getInEdges(binaryProduction.secondInput) : intermediate.getOutEdges(binaryProduction.secondInput);
			for(Edge secondEdge : secondEdges) {
				this.addEdge2(binaryProduction, edge, secondEdge);
			}
		}
		for(BinaryProduction binaryProduction : c.binaryProductionsBySecondInput[edge.kind]) {
			Node intermediate = binaryProduction.isSecondInputBackwards ? edge.to : edge.from;
			Iterable<Edge> firstEdges = binaryProduction.isFirstInputBackwards ? intermediate.getOutEdges(binaryProduction.firstInput) : intermediate.getInEdges(binaryProduction.firstInput);
			for(Edge firstEdge : firstEdges) {
				this.addEdge2(binaryProduction, firstEdge, edge);
			}
		}
	}
	
	private void addEdge2(UnaryProduction unaryProduction, Edge input) {
		// get edge base
		Node source = unaryProduction.isInputBackwards ? input.to : input.from;
		Node sink = unaryProduction.isInputBackwards ? input.from : input.to;
		int symbolInt = unaryProduction.target;
		
		// add edge
		this.addEdge(source, sink, symbolInt, input, !unaryProduction.ignoreFields);
	}

	private void addEdge2(BinaryProduction binaryProduction, Edge firstInput, Edge secondInput) {
		// get edge base
		Node source = binaryProduction.isFirstInputBackwards ? firstInput.to : firstInput.from;
		Node sink = binaryProduction.isSecondInputBackwards ? secondInput.from : secondInput.to;
		int symbolInt = binaryProduction.target;
		
		// add edge
		this.addEdge(source, sink, symbolInt, firstInput, secondInput, !binaryProduction.ignoreFields);
	}
	
	public String[] outputRels() {
		String[] rels = {};
		return rels;
	}

	public short kindToWeight(int kind) {
		return 0;
	}

	public boolean useReps() { return true; }

}