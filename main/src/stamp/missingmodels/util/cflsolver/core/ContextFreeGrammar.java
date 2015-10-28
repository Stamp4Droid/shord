package stamp.missingmodels.util.cflsolver.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextFreeGrammar {
	public static final class Symbol {
		public final String symbol;
		public final int id;
		public Symbol(String symbol, int id) {
			this.symbol = symbol;
			this.id = id;
		}
	}
	
	public static final class SymbolMap {
		private final Map<String,Symbol> symbols = new HashMap<String,Symbol>();
		private final Map<Integer,Symbol> symbolsById = new HashMap<Integer,Symbol>();
		private int numSymbols = 0;
		
		public Symbol get(String symbol) {
			return this.symbols.get(symbol);
		}
		
		public Symbol get(int symbolInt) {
			return this.symbolsById.get(symbolInt);
		}
		
		public Symbol add(String symbol) {
			Symbol result = new Symbol(symbol, this.numSymbols++);
			this.symbols.put(symbol, result);
			this.symbolsById.put(result.id, result);
			return result;
		}
		
		public int getNumSymbols() {
			return this.numSymbols;
		}
		
		public Collection<String> getSymbols() {
			return this.symbols.keySet();
		}
	}
	
	public static final class BinaryProduction {
		public final Symbol target;
		public final Symbol firstInput;
		public final Symbol secondInput;
		public final boolean isFirstInputBackwards;
		public final boolean isSecondInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		public final short weight;

		public BinaryProduction(Symbol target, Symbol firstInput, Symbol secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
			this.target = target;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
			this.isFirstInputBackwards = isFirstInputBackwards;
			this.isSecondInputBackwards = isSecondInputBackwards;
			this.ignoreFields = ignoreFields;
			this.ignoreContexts = ignoreContexts;
			this.weight = weight;
		}

		public BinaryProduction(Symbol target, Symbol firstInput, Symbol secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
			this(target, firstInput, secondInput, isFirstInputBackwards, isSecondInputBackwards, ignoreFields, ignoreContexts, (short)0);
		}
		
		public BinaryProduction(Symbol target, Symbol firstInput, Symbol secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards) {
			this(target, firstInput, secondInput, isFirstInputBackwards, isSecondInputBackwards, false, false);
		}

		public BinaryProduction(Symbol target, Symbol firstInput, Symbol secondInput) {
			this(target, firstInput, secondInput, false, false, false, false);
		}

		@Override
		public String toString() {
			return this.target.symbol + "[" + this.ignoreFields + "][" + this.ignoreContexts + "]" + " :- " + (this.isFirstInputBackwards ? "_" : "") + this.firstInput.symbol + ", " + (this.isSecondInputBackwards ? "_" : "") + this.secondInput.symbol + ".";
		}
	}

	public static final class AuxProduction {
		public final Symbol target;
		public final Symbol input;
		public final Symbol auxInput;
		public final boolean isAuxInputFirst;
		public final boolean isInputBackwards;
		public final boolean isAuxInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		public final short weight;

		public AuxProduction(Symbol target, Symbol input, Symbol auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
			this.target = target;
			this.input = input;
			this.auxInput = auxInput;
			this.isAuxInputFirst = isAuxInputFirst;
			this.isInputBackwards = isInputBackwards;
			this.isAuxInputBackwards = isAuxInputBackwards;
			this.ignoreFields = ignoreFields;
			this.ignoreContexts = ignoreContexts;
			this.weight = weight;
		}

		public AuxProduction(Symbol target, Symbol input, Symbol auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
			this(target, input, auxInput, isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, ignoreContexts, (short)0);
		}
		
		public AuxProduction(Symbol target, Symbol input, Symbol auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards) {
			this(target, input, auxInput, isAuxInputFirst, isInputBackwards, isAuxInputBackwards, false, false);
		}

		public AuxProduction(Symbol target, Symbol input, Symbol auxInput, boolean isAuxInputFirst) {
			this(target, input, auxInput, isAuxInputFirst, false, false, false, false);
		}

		@Override
		public String toString() {
			if(this.isAuxInputFirst) {
				return this.target.symbol + "[" + this.ignoreFields + "][" + this.ignoreContexts + "]" + " :- (" + (this.isAuxInputBackwards ? "_" : "") + this.auxInput.symbol + "), " + (this.isInputBackwards ? "_" : "") + this.input.symbol + ".";

			} else {
				return this.target.symbol + "[" + this.ignoreFields + "][" + this.ignoreContexts + "]" + " :- " + (this.isInputBackwards ? "_" : "") + this.input.symbol + ", (" + (this.isAuxInputBackwards ? "_" : "") + this.auxInput.symbol + ").";
			}
		}
	}

	public final class UnaryProduction {
		public final Symbol target;
		public final Symbol input;
		public final boolean isInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		public final short weight;

		public UnaryProduction(Symbol target, Symbol input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
			this.target = target;
			this.input = input;
			this.isInputBackwards = isInputBackwards;
			this.ignoreFields = ignoreFields;
			this.ignoreContexts = ignoreContexts;
			this.weight = weight;
		}

		public UnaryProduction(Symbol target, Symbol input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
			this(target, input, isInputBackwards, ignoreFields, ignoreContexts, (short)0);
		}
		
		public UnaryProduction(Symbol target, Symbol input, boolean isInputBackwards, boolean ignoreFields) {
			this(target, input, isInputBackwards, ignoreFields, false);
		}

		public UnaryProduction(Symbol target, Symbol input, boolean isInputBackwards) {
			this(target, input, isInputBackwards, false, false);
		}

		public UnaryProduction(Symbol target, Symbol input) {
			this(target, input, false, false, false);
		}

		@Override
		public String toString() {
			return this.target.symbol + " :- " + (this.isInputBackwards ? "_" : "") + this.input.symbol + ".";
		}
	}

	public static final class ContextFreeGrammarOpt {
		public final UnaryProduction[][] unaryProductionsByInput;
		public final UnaryProduction[][] unaryProductionsByTarget;
		public final BinaryProduction[][] binaryProductionsByFirstInput;
		public final BinaryProduction[][] binaryProductionsBySecondInput;
		public final BinaryProduction[][] binaryProductionsByTarget;
		public final AuxProduction[][] auxProductionsByInput;
		public final AuxProduction[][] auxProductionsByAuxInput;
		public final AuxProduction[][] auxProductionsByTarget;

		private final SymbolMap symbols;
	
		private ContextFreeGrammarOpt(ContextFreeGrammar c) {
			this.symbols = c.symbols;

			this.unaryProductionsByInput = new UnaryProduction[c.unaryProductionsByInput.size()][];
			for(int i=0; i<c.unaryProductionsByInput.size(); i++) {
				this.unaryProductionsByInput[i] = new UnaryProduction[c.unaryProductionsByInput.get(i).size()];
				for(int j=0; j<c.unaryProductionsByInput.get(i).size(); j++) {
					this.unaryProductionsByInput[i][j] = c.unaryProductionsByInput.get(i).get(j);
				}
			}
	
			this.unaryProductionsByTarget = new UnaryProduction[c.unaryProductionsByTarget.size()][];
			for(int i=0; i<c.unaryProductionsByTarget.size(); i++) {
				this.unaryProductionsByTarget[i] = new UnaryProduction[c.unaryProductionsByTarget.get(i).size()];
				for(int j=0; j<c.unaryProductionsByTarget.get(i).size(); j++) {
					this.unaryProductionsByTarget[i][j] = c.unaryProductionsByTarget.get(i).get(j);
				}
			}
	
			this.binaryProductionsByFirstInput = new BinaryProduction[c.binaryProductionsByFirstInput.size()][];
			for(int i=0; i<c.binaryProductionsByFirstInput.size(); i++) {
				this.binaryProductionsByFirstInput[i] = new BinaryProduction[c.binaryProductionsByFirstInput.get(i).size()];
				for(int j=0; j<c.binaryProductionsByFirstInput.get(i).size(); j++) {
					this.binaryProductionsByFirstInput[i][j] = c.binaryProductionsByFirstInput.get(i).get(j);
				}
			}
	
			this.binaryProductionsBySecondInput = new BinaryProduction[c.binaryProductionsBySecondInput.size()][];
			for(int i=0; i<c.binaryProductionsBySecondInput.size(); i++) {
				this.binaryProductionsBySecondInput[i] = new BinaryProduction[c.binaryProductionsBySecondInput.get(i).size()];
				for(int j=0; j<c.binaryProductionsBySecondInput.get(i).size(); j++) {
					this.binaryProductionsBySecondInput[i][j] = c.binaryProductionsBySecondInput.get(i).get(j);
				}
			}
	
			this.binaryProductionsByTarget = new BinaryProduction[c.binaryProductionsByTarget.size()][];
			for(int i=0; i<c.binaryProductionsByTarget.size(); i++) {
				this.binaryProductionsByTarget[i] = new BinaryProduction[c.binaryProductionsByTarget.get(i).size()];
				for(int j=0; j<c.binaryProductionsByTarget.get(i).size(); j++) {
					this.binaryProductionsByTarget[i][j] = c.binaryProductionsByTarget.get(i).get(j);
				}
			}
	
			this.auxProductionsByInput = new AuxProduction[c.auxProductionsByInput.size()][];
			for(int i=0; i<c.auxProductionsByInput.size(); i++) {
				this.auxProductionsByInput[i] = new AuxProduction[c.auxProductionsByInput.get(i).size()];
				for(int j=0; j<c.auxProductionsByInput.get(i).size(); j++) {
					this.auxProductionsByInput[i][j] = c.auxProductionsByInput.get(i).get(j);
				}
			}
	
			this.auxProductionsByAuxInput = new AuxProduction[c.auxProductionsByAuxInput.size()][];
			for(int i=0; i<c.auxProductionsByAuxInput.size(); i++) {
				this.auxProductionsByAuxInput[i] = new AuxProduction[c.auxProductionsByAuxInput.get(i).size()];
				for(int j=0; j<c.auxProductionsByAuxInput.get(i).size(); j++) {
					this.auxProductionsByAuxInput[i][j] = c.auxProductionsByAuxInput.get(i).get(j);
				}
			}
	
			this.auxProductionsByTarget = new AuxProduction[c.auxProductionsByTarget.size()][];
			for(int i=0; i<c.auxProductionsByTarget.size(); i++) {
				this.auxProductionsByTarget[i] = new AuxProduction[c.auxProductionsByTarget.get(i).size()];
				for(int j=0; j<c.auxProductionsByTarget.get(i).size(); j++) {
					this.auxProductionsByTarget[i][j] = c.auxProductionsByTarget.get(i).get(j);
				}
			}
		}
		
		public SymbolMap getSymbols() {
			return this.symbols;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<this.symbols.getNumSymbols(); i++) {
				for(UnaryProduction unaryProduction : this.unaryProductionsByInput[i]) {
					sb.append(unaryProduction.toString()).append("\n");
				}
			}
			for(int i=0; i<this.symbols.getNumSymbols(); i++) {
				for(BinaryProduction binaryProduction : this.binaryProductionsByFirstInput[i]) {
					sb.append(binaryProduction.toString()).append("\n");
				}
			}
			return sb.toString();
		}
	}

	public final List<List<UnaryProduction>> unaryProductionsByInput;
	public final List<List<UnaryProduction>> unaryProductionsByTarget;
	public final List<List<BinaryProduction>> binaryProductionsByFirstInput;
	public final List<List<BinaryProduction>> binaryProductionsBySecondInput;
	public final List<List<BinaryProduction>> binaryProductionsByTarget;
	public final List<List<AuxProduction>> auxProductionsByInput;
	public final List<List<AuxProduction>> auxProductionsByAuxInput;
	public final List<List<AuxProduction>> auxProductionsByTarget;

	public ContextFreeGrammar() {
		this.unaryProductionsByInput = new ArrayList<List<UnaryProduction>>();
		this.unaryProductionsByTarget = new ArrayList<List<UnaryProduction>>();
		this.binaryProductionsByFirstInput = new ArrayList<List<BinaryProduction>>();
		this.binaryProductionsBySecondInput = new ArrayList<List<BinaryProduction>>();
		this.binaryProductionsByTarget = new ArrayList<List<BinaryProduction>>();
		this.auxProductionsByInput = new ArrayList<List<AuxProduction>>();
		this.auxProductionsByAuxInput = new ArrayList<List<AuxProduction>>();
		this.auxProductionsByTarget = new ArrayList<List<AuxProduction>>();
	}
	
	public ContextFreeGrammarOpt getOpt() {
		return new ContextFreeGrammarOpt(this);
	}
	
	private final SymbolMap symbols = new SymbolMap();

	public Symbol getSymbol(String symbol) {
		Symbol result = this.symbols.get(symbol);
		if(result == null) {
			this.unaryProductionsByInput.add(new ArrayList<UnaryProduction>());
			this.unaryProductionsByTarget.add(new ArrayList<UnaryProduction>());
			this.binaryProductionsByFirstInput.add(new ArrayList<BinaryProduction>());
			this.binaryProductionsBySecondInput.add(new ArrayList<BinaryProduction>());
			this.binaryProductionsByTarget.add(new ArrayList<BinaryProduction>());
			this.auxProductionsByInput.add(new ArrayList<AuxProduction>());
			this.auxProductionsByAuxInput.add(new ArrayList<AuxProduction>());
			this.auxProductionsByTarget.add(new ArrayList<AuxProduction>());
			result = this.symbols.add(symbol);
		}
		return result;
	}
	
	public SymbolMap getSymbols() {
		return this.symbols;
	}

	public void addUnaryProduction(String target, String input) {
		this.addUnaryProduction(this.getSymbol(target), this.getSymbol(input), false, false, false, (short)0);
	}

	public void addUnaryProduction(String target, String input, boolean isInputBackwards) {
		this.addUnaryProduction(this.getSymbol(target), this.getSymbol(input), isInputBackwards, false, false, (short)0);
	}

	public void addUnaryProduction(String target, String input, boolean isInputBackwards, boolean ignoreFields) {
		this.addUnaryProduction(this.getSymbol(target), this.getSymbol(input), isInputBackwards, ignoreFields, false, (short)0);
	}

	public void addUnaryProduction(String target, String input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		this.addUnaryProduction(this.getSymbol(target), this.getSymbol(input), isInputBackwards, ignoreFields, ignoreContexts, (short)0);
	}

	public void addUnaryProduction(String target, String input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
		this.addUnaryProduction(this.getSymbol(target), this.getSymbol(input), isInputBackwards, ignoreFields, ignoreContexts, weight);
	}

	public void addBinaryProduction(String target, String firstInput, String secondInput) {
		this.addBinaryProduction(this.getSymbol(target), this.getSymbol(firstInput), this.getSymbol(secondInput), false, false, false, false, (short)0);
	}

	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean ignoreFields) {
		this.addBinaryProduction(this.getSymbol(target), this.getSymbol(firstInput), this.getSymbol(secondInput), false, false, ignoreFields, false, (short)0);
	}

	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards) {
		this.addBinaryProduction(this.getSymbol(target), this.getSymbol(firstInput), this.getSymbol(secondInput), isFirstInputBackwards, isSecondInputBackwards, false, false, (short)0);
	}

	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields) {
		this.addBinaryProduction(this.getSymbol(target), this.getSymbol(firstInput), this.getSymbol(secondInput), isFirstInputBackwards, isSecondInputBackwards, ignoreFields, false, (short)0);
	}

	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		this.addBinaryProduction(this.getSymbol(target), this.getSymbol(firstInput), this.getSymbol(secondInput), isFirstInputBackwards, isSecondInputBackwards, ignoreFields, ignoreContexts, (short)0);
	}

	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
		this.addBinaryProduction(this.getSymbol(target), this.getSymbol(firstInput), this.getSymbol(secondInput), isFirstInputBackwards, isSecondInputBackwards, ignoreFields, ignoreContexts, weight);
	}

	public void addProduction(String target, String[] inputs) {
		this.addProduction(target, inputs, new boolean[inputs.length], false, false, (short)0);
	}

	public void addProduction(String target, String[] inputs, boolean ignoreFields) {
		this.addProduction(target, inputs, new boolean[inputs.length], ignoreFields, false, (short)0);
	}

	public void addProduction(String target, String[] inputs, boolean[] isInputBackwards) {
		this.addProduction(target, inputs, isInputBackwards, false, false, (short)0);
	}

	public void addProduction(String target, String[] inputs, boolean ignoreFields, boolean ignoreContexts) {
		this.addProduction(target, inputs, new boolean[inputs.length], ignoreFields, ignoreContexts, (short)0);
	}

	public void addProduction(String target, String[] inputs, boolean[] isInputBackwards, boolean ignoreFields) {
		this.addProduction(target, inputs, isInputBackwards, ignoreFields, false, (short)0);
	}

	public void addProduction(String target, String[] inputs, boolean[] isInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
		if(inputs.length <= 2 || inputs.length != isInputBackwards.length) {
			throw new RuntimeException("Invalid production");
		}
		String prevInput = inputs[0];
		String curInput = "^" + (isInputBackwards[0] ? "_" : "") + inputs[0] + "^" + (isInputBackwards[1] ? "_" : "") + inputs[1];
		this.addBinaryProduction(curInput, prevInput, inputs[1], isInputBackwards[0], isInputBackwards[1], ignoreFields, ignoreContexts);
		prevInput = curInput;
		for(int i=2; i<inputs.length-1; i++) {
			curInput = prevInput + "^" + (isInputBackwards[i] ? "_" : "") + inputs[i];
			this.addBinaryProduction(curInput, prevInput, inputs[i], false, isInputBackwards[i], ignoreFields, ignoreContexts);
			prevInput = curInput;
		}
		this.addBinaryProduction(target, prevInput, inputs[inputs.length-1], false, isInputBackwards[inputs.length-1], ignoreFields, ignoreContexts);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst) {
		this.addAuxProduction(this.getSymbol(target), this.getSymbol(input), this.getSymbol(auxInput), isAuxInputFirst, false, false, false, false, (short)0);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean ignoreFields) {
		this.addAuxProduction(this.getSymbol(target), this.getSymbol(input), this.getSymbol(auxInput), isAuxInputFirst, false, false, ignoreFields, false, (short)0);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards) {
		this.addAuxProduction(this.getSymbol(target), this.getSymbol(input), this.getSymbol(auxInput), isAuxInputFirst, isInputBackwards, isAuxInputBackwards, false, false, (short)0);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields) {
		this.addAuxProduction(this.getSymbol(target), this.getSymbol(input), this.getSymbol(auxInput), isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, false, (short)0);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		this.addAuxProduction(this.getSymbol(target), this.getSymbol(input), this.getSymbol(auxInput), isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, ignoreContexts, (short)0);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
		this.addAuxProduction(this.getSymbol(target), this.getSymbol(input), this.getSymbol(auxInput), isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, ignoreContexts, weight);
	}

	public void addUnaryProduction(Symbol target, Symbol input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
		if(target.id >= this.symbols.getNumSymbols() || input.id >= this.symbols.getNumSymbols()) {
			throw new RuntimeException("symbol out of range");
		}
		UnaryProduction unaryProduction = new UnaryProduction(target, input, isInputBackwards, ignoreFields, ignoreContexts, weight);
		this.unaryProductionsByInput.get(input.id).add(unaryProduction);
		this.unaryProductionsByTarget.get(target.id).add(unaryProduction);
	}

	public void addBinaryProduction(Symbol target, Symbol firstInput, Symbol secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
		if(target.id >= this.symbols.getNumSymbols() || firstInput.id >= this.symbols.getNumSymbols() || secondInput.id >= this.symbols.getNumSymbols()) {
			throw new RuntimeException("symbol out of range");
		}
		BinaryProduction binaryProduction = new BinaryProduction(target, firstInput, secondInput, isFirstInputBackwards, isSecondInputBackwards, ignoreFields, ignoreContexts, weight);
		this.binaryProductionsByFirstInput.get(firstInput.id).add(binaryProduction);
		this.binaryProductionsBySecondInput.get(secondInput.id).add(binaryProduction);
		this.binaryProductionsByTarget.get(target.id).add(binaryProduction);
	}

	public void addAuxProduction(Symbol target, Symbol input, Symbol auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts, short weight) {
		if(target.id >= this.symbols.getNumSymbols() || input.id >= this.symbols.getNumSymbols() || auxInput.id >= this.symbols.getNumSymbols()) {
			throw new RuntimeException("symbol out of range");
		}
		AuxProduction auxProduction = new AuxProduction(target, input, auxInput, isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, ignoreContexts, weight);
		this.auxProductionsByInput.get(input.id).add(auxProduction);
		this.auxProductionsByAuxInput.get(auxInput.id).add(auxProduction);
		this.auxProductionsByTarget.get(target.id).add(auxProduction);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<this.symbols.getNumSymbols(); i++) {
			for(UnaryProduction unaryProduction : (List<UnaryProduction>)this.unaryProductionsByInput.get(i)) {
				sb.append(unaryProduction.toString()).append("\n");
			}
		}
		for(int i=0; i<this.symbols.getNumSymbols(); i++) {
			for(BinaryProduction binaryProduction : (List<BinaryProduction>)this.binaryProductionsByFirstInput.get(i)) {
				sb.append(binaryProduction.toString()).append("\n");
			}
		}
		for(int i=0; i<this.symbols.getNumSymbols(); i++) {
			for(AuxProduction auxProduction : (List<AuxProduction>)this.auxProductionsByInput.get(i)) {
				sb.append(auxProduction.toString()).append("\n");
			}
		}		
		return sb.toString();
	}
}
