package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.cflsolver.graph.ContextFreeGrammar;

public class UnionGrammar extends ContextFreeGrammar {
	private void add(ContextFreeGrammar grammar) {
		for(int i=0; i<grammar.getNumLabels(); i++) {
			for(UnaryProduction unaryProduction : grammar.unaryProductionsByInput.get(i)) {
				this.addUnaryProduction(grammar.getSymbol(unaryProduction.target), grammar.getSymbol(unaryProduction.input), unaryProduction.isInputBackwards, unaryProduction.ignoreFields, unaryProduction.ignoreContexts);
			}
			for(BinaryProduction binaryProduction : grammar.binaryProductionsByFirstInput.get(i)) {
				this.addBinaryProduction(grammar.getSymbol(binaryProduction.target), grammar.getSymbol(binaryProduction.firstInput), grammar.getSymbol(binaryProduction.secondInput), binaryProduction.isFirstInputBackwards, binaryProduction.isSecondInputBackwards, binaryProduction.ignoreFields, binaryProduction.ignoreContexts);
			}
			for(AuxProduction auxProduction : grammar.auxProductionsByInput.get(i)) {
				this.addAuxProduction(grammar.getSymbol(auxProduction.target), grammar.getSymbol(auxProduction.input), grammar.getSymbol(auxProduction.auxInput), auxProduction.isAuxInputFirst, auxProduction.isInputBackwards, auxProduction.isAuxInputBackwards, auxProduction.ignoreFields, auxProduction.ignoreContexts);
			}
		}
	}
	
	public UnionGrammar(ContextFreeGrammar ... grammars) {
		for(ContextFreeGrammar grammar : grammars) {
			this.add(grammar);	
		}
	}
}
