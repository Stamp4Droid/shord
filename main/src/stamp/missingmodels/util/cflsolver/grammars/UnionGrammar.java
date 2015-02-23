package stamp.missingmodels.util.cflsolver.grammars;

import stamp.missingmodels.util.jcflsolver2.ContextFreeGrammar;

public class UnionGrammar extends ContextFreeGrammar {
	private void add(ContextFreeGrammar grammar) {
		for(int i=0; i<grammar.getNumLabels(); i++) {
			for(UnaryProduction unaryProduction : grammar.unaryProductionsByInput.get(i)) {
				this.addUnaryProduction(unaryProduction.target.symbol, unaryProduction.input.symbol, unaryProduction.isInputBackwards, unaryProduction.ignoreFields, unaryProduction.ignoreContexts);
			}
			for(BinaryProduction binaryProduction : grammar.binaryProductionsByFirstInput.get(i)) {
				this.addBinaryProduction(binaryProduction.target.symbol, binaryProduction.firstInput.symbol, binaryProduction.secondInput.symbol, binaryProduction.isFirstInputBackwards, binaryProduction.isSecondInputBackwards, binaryProduction.ignoreFields, binaryProduction.ignoreContexts);
			}
			for(AuxProduction auxProduction : grammar.auxProductionsByInput.get(i)) {
				this.addAuxProduction(auxProduction.target.symbol, auxProduction.input.symbol, auxProduction.auxInput.symbol, auxProduction.isAuxInputFirst, auxProduction.isInputBackwards, auxProduction.isAuxInputBackwards, auxProduction.ignoreFields, auxProduction.ignoreContexts);
			}
		}
	}
	
	public UnionGrammar(ContextFreeGrammar ... grammars) {
		for(ContextFreeGrammar grammar : grammars) {
			this.add(grammar);	
		}
	}
}
