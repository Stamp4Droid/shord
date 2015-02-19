package stamp.missingmodels.util.cflsolver.graph;

import java.util.HashMap;
import java.util.Map;

public class ContextFreeGrammarOpt {
	
	public final class UnaryProduction {
		public final int target;
		public final int input;
		public final boolean isInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		
		public UnaryProduction(ContextFreeGrammar.UnaryProduction production) {
			this.target = production.target;
			this.input = production.input;
			this.isInputBackwards = production.isInputBackwards;
			this.ignoreFields = production.ignoreFields;
			this.ignoreContexts = production.ignoreContexts;
		}
		
		@Override
		public String toString() {
			return getSymbol(this.target) + " :- " + (this.isInputBackwards ? "_" : "") + getSymbol(this.input) + ".";
		}
	}
	
	public final class BinaryProduction {
		public final int target;
		public final int firstInput;
		public final int secondInput;
		public final boolean isFirstInputBackwards;
		public final boolean isSecondInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		
		public BinaryProduction(ContextFreeGrammar.BinaryProduction production) {
			this.target = production.target;
			this.firstInput = production.firstInput;
			this.secondInput = production.secondInput;
			this.isFirstInputBackwards = production.isFirstInputBackwards;
			this.isSecondInputBackwards = production.isSecondInputBackwards;
			this.ignoreFields = production.ignoreFields;
			this.ignoreContexts = production.ignoreContexts;
		}
		
		@Override
		public String toString() {
			return getSymbol(this.target) + "[" + this.ignoreFields + "][" + this.ignoreContexts + "]" + " :- " + (this.isFirstInputBackwards ? "_" : "") + getSymbol(this.firstInput) + ", " + (this.isSecondInputBackwards ? "_" : "") + getSymbol(this.secondInput) + ".";
		}
	}

	public final class AuxProduction {
		public final int target;
		public final int input;
		public final int auxInput;
		public final boolean isAuxInputFirst;
		public final boolean isInputBackwards;
		public final boolean isAuxInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		
		public AuxProduction(ContextFreeGrammar.AuxProduction production) {
			this.target = production.target;
			this.input = production.input;
			this.auxInput = production.auxInput;
			this.isAuxInputFirst = production.isAuxInputFirst;
			this.isInputBackwards = production.isInputBackwards;
			this.isAuxInputBackwards = production.isAuxInputBackwards;
			this.ignoreFields = production.ignoreFields;
			this.ignoreContexts = production.ignoreContexts;
		}
		
		@Override
		public String toString() {
			if(this.isAuxInputFirst) {
				return getSymbol(this.target) + "[" + this.ignoreFields + "][" + this.ignoreContexts + "]" + " :- (" + (this.isAuxInputBackwards ? "_" : "") + getSymbol(this.auxInput) + "), " + (this.isInputBackwards ? "_" : "") + getSymbol(this.input) + ".";
				
			} else {
				return getSymbol(this.target) + "[" + this.ignoreFields + "][" + this.ignoreContexts + "]" + " :- " + (this.isInputBackwards ? "_" : "") + getSymbol(this.input) + ", (" + (this.isAuxInputBackwards ? "_" : "") + getSymbol(this.auxInput) + ").";
			}
		}
	}
	
	public final UnaryProduction[][] unaryProductionsByInput;
	public final UnaryProduction[][] unaryProductionsByTarget;
	public final BinaryProduction[][] binaryProductionsByFirstInput;
	public final BinaryProduction[][] binaryProductionsBySecondInput;
	public final BinaryProduction[][] binaryProductionsByTarget;
	public final AuxProduction[][] auxProductionsByInput;
	public final AuxProduction[][] auxProductionsByAuxInput;
	public final AuxProduction[][] auxProductionsByTarget;
	
	private final Map<String,Integer> symbolInts = new HashMap<String,Integer>();
	private final Map<Integer,String> symbols = new HashMap<Integer,String>();
	private int numLabels = 0;
	
	public ContextFreeGrammarOpt(ContextFreeGrammar c) {
		for(int i=0; i<c.getNumLabels(); i++) {
			this.symbols.put(i, c.getSymbol(i));
			this.symbolInts.put(c.getSymbol(i), i);
		}
		this.numLabels = c.getNumLabels();
		
		this.unaryProductionsByInput = new UnaryProduction[c.unaryProductionsByInput.size()][];
		for(int i=0; i<c.unaryProductionsByInput.size(); i++) {
			this.unaryProductionsByInput[i] = new UnaryProduction[c.unaryProductionsByInput.get(i).size()];
			for(int j=0; j<c.unaryProductionsByInput.get(i).size(); j++) {
				this.unaryProductionsByInput[i][j] = new UnaryProduction(c.unaryProductionsByInput.get(i).get(j));
			}
		}

		this.unaryProductionsByTarget = new UnaryProduction[c.unaryProductionsByTarget.size()][];
		for(int i=0; i<c.unaryProductionsByTarget.size(); i++) {
			this.unaryProductionsByTarget[i] = new UnaryProduction[c.unaryProductionsByTarget.get(i).size()];
			for(int j=0; j<c.unaryProductionsByTarget.get(i).size(); j++) {
				this.unaryProductionsByTarget[i][j] = new UnaryProduction(c.unaryProductionsByTarget.get(i).get(j));
			}
		}

		this.binaryProductionsByFirstInput = new BinaryProduction[c.binaryProductionsByFirstInput.size()][];
		for(int i=0; i<c.binaryProductionsByFirstInput.size(); i++) {
			this.binaryProductionsByFirstInput[i] = new BinaryProduction[c.binaryProductionsByFirstInput.get(i).size()];
			for(int j=0; j<c.binaryProductionsByFirstInput.get(i).size(); j++) {
				this.binaryProductionsByFirstInput[i][j] = new BinaryProduction(c.binaryProductionsByFirstInput.get(i).get(j));
			}
		}

		this.binaryProductionsBySecondInput = new BinaryProduction[c.binaryProductionsBySecondInput.size()][];
		for(int i=0; i<c.binaryProductionsBySecondInput.size(); i++) {
			this.binaryProductionsBySecondInput[i] = new BinaryProduction[c.binaryProductionsBySecondInput.get(i).size()];
			for(int j=0; j<c.binaryProductionsBySecondInput.get(i).size(); j++) {
				this.binaryProductionsBySecondInput[i][j] = new BinaryProduction(c.binaryProductionsBySecondInput.get(i).get(j));
			}
		}

		this.binaryProductionsByTarget = new BinaryProduction[c.binaryProductionsByTarget.size()][];
		for(int i=0; i<c.binaryProductionsByTarget.size(); i++) {
			this.binaryProductionsByTarget[i] = new BinaryProduction[c.binaryProductionsByTarget.get(i).size()];
			for(int j=0; j<c.binaryProductionsByTarget.get(i).size(); j++) {
				this.binaryProductionsByTarget[i][j] = new BinaryProduction(c.binaryProductionsByTarget.get(i).get(j));
			}
		}

		this.auxProductionsByInput = new AuxProduction[c.auxProductionsByInput.size()][];
		for(int i=0; i<c.auxProductionsByInput.size(); i++) {
			this.auxProductionsByInput[i] = new AuxProduction[c.auxProductionsByInput.get(i).size()];
			for(int j=0; j<c.auxProductionsByInput.get(i).size(); j++) {
				this.auxProductionsByInput[i][j] = new AuxProduction(c.auxProductionsByInput.get(i).get(j));
			}
		}

		this.auxProductionsByAuxInput = new AuxProduction[c.auxProductionsByAuxInput.size()][];
		for(int i=0; i<c.auxProductionsByAuxInput.size(); i++) {
			this.auxProductionsByAuxInput[i] = new AuxProduction[c.auxProductionsByAuxInput.get(i).size()];
			for(int j=0; j<c.auxProductionsByAuxInput.get(i).size(); j++) {
				this.auxProductionsByAuxInput[i][j] = new AuxProduction(c.auxProductionsByAuxInput.get(i).get(j));
			}
		}

		this.auxProductionsByTarget = new AuxProduction[c.auxProductionsByTarget.size()][];
		for(int i=0; i<c.auxProductionsByTarget.size(); i++) {
			this.auxProductionsByTarget[i] = new AuxProduction[c.auxProductionsByTarget.get(i).size()];
			for(int j=0; j<c.auxProductionsByTarget.get(i).size(); j++) {
				this.auxProductionsByTarget[i][j] = new AuxProduction(c.auxProductionsByTarget.get(i).get(j));
			}
		}
	}
	
	public int getNumLabels() {
		return this.numLabels;
	}
	
	public int getSymbolInt(String symbol) {
		return this.symbolInts.get(symbol);
	}
	
	public String getSymbol(int label) {
		return this.symbols.get(label);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<this.numLabels; i++) {
			for(UnaryProduction unaryProduction : this.unaryProductionsByInput[i]) {
				sb.append(unaryProduction.toString()).append("\n");
			}
		}
		for(int i=0; i<this.numLabels; i++) {
			for(BinaryProduction binaryProduction : this.binaryProductionsByFirstInput[i]) {
				sb.append(binaryProduction.toString()).append("\n");
			}
		}
		return sb.toString();
	}
}
