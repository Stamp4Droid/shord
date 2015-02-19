package stamp.missingmodels.util.cflsolver.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextFreeGrammar {
	public final class BinaryProduction {
		public final int target;
		public final int firstInput;
		public final int secondInput;
		public final boolean isFirstInputBackwards;
		public final boolean isSecondInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		
		public BinaryProduction(int target, int firstInput, int secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
			this.target = target;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
			this.isFirstInputBackwards = isFirstInputBackwards;
			this.isSecondInputBackwards = isSecondInputBackwards;
			this.ignoreFields = ignoreFields;
			this.ignoreContexts = ignoreContexts;
		}
		
		public BinaryProduction(int target, int firstInput, int secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards) {
			this(target, firstInput, secondInput, isFirstInputBackwards, isSecondInputBackwards, false, false);
		}
		
		public BinaryProduction(int target, int firstInput, int secondInput) {
			this(target, firstInput, secondInput, false, false, false, false);
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
		
		public AuxProduction(int target, int input, int auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
			this.target = target;
			this.input = input;
			this.auxInput = auxInput;
			this.isAuxInputFirst = isAuxInputFirst;
			this.isInputBackwards = isInputBackwards;
			this.isAuxInputBackwards = isAuxInputBackwards;
			this.ignoreFields = ignoreFields;
			this.ignoreContexts = ignoreContexts;
		}
		
		public AuxProduction(int target, int input, int auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards) {
			this(target, input, auxInput, isAuxInputFirst, isInputBackwards, isAuxInputBackwards, false, false);
		}
		
		public AuxProduction(int target, int input, int auxInput, boolean isAuxInputFirst) {
			this(target, input, auxInput, isAuxInputFirst, false, false, false, false);
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
	
	public final class UnaryProduction {
		public final int target;
		public final int input;
		public final boolean isInputBackwards;
		public final boolean ignoreFields;
		public final boolean ignoreContexts;
		
		public UnaryProduction(int target, int input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
			this.target = target;
			this.input = input;
			this.isInputBackwards = isInputBackwards;
			this.ignoreFields = ignoreFields;
			this.ignoreContexts = ignoreContexts;
		}

		public UnaryProduction(int target, int input, boolean isInputBackwards, boolean ignoreFields) {
			this(target, input, isInputBackwards, ignoreFields, false);
		}
		
		public UnaryProduction(int target, int input, boolean isInputBackwards) {
			this(target, input, isInputBackwards, false, false);
		}
		
		public UnaryProduction(int target, int input) {
			this(target, input, false, false, false);
		}
		
		@Override
		public String toString() {
			return getSymbol(this.target) + " :- " + (this.isInputBackwards ? "_" : "") + getSymbol(this.input) + ".";
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
	
	
	
	private final Map<String,Integer> symbolInts = new HashMap<String,Integer>();
	private final Map<Integer,String> symbols = new HashMap<Integer,String>();
	private int curLabel = 0;
	
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
	
	public int getNumLabels() {
		return this.curLabel;
	}
	
	public int getSymbolInt(String symbol) {
		Integer symbolInt = this.symbolInts.get(symbol);
		if(symbolInt == null) {
			this.unaryProductionsByInput.add(new ArrayList<UnaryProduction>());
			this.unaryProductionsByTarget.add(new ArrayList<UnaryProduction>());
			this.binaryProductionsByFirstInput.add(new ArrayList<BinaryProduction>());
			this.binaryProductionsBySecondInput.add(new ArrayList<BinaryProduction>());
			this.binaryProductionsByTarget.add(new ArrayList<BinaryProduction>());
			this.auxProductionsByInput.add(new ArrayList<AuxProduction>());
			this.auxProductionsByAuxInput.add(new ArrayList<AuxProduction>());
			this.auxProductionsByTarget.add(new ArrayList<AuxProduction>());
			symbolInt = this.curLabel++;
			this.symbolInts.put(symbol, symbolInt);
			this.symbols.put(symbolInt, symbol);
		}
		return symbolInt;
	}
	
	public String getSymbol(int label) {
		return this.symbols.get(label);
	}
	
	public void addUnaryProduction(String target, String input) {
		this.addUnaryProduction(this.getSymbolInt(target), this.getSymbolInt(input), false, false, false);
	}
	
	public void addUnaryProduction(String target, String input, boolean isInputBackwards) {
		this.addUnaryProduction(this.getSymbolInt(target), this.getSymbolInt(input), isInputBackwards, false, false);
	}
	
	public void addUnaryProduction(String target, String input, boolean isInputBackwards, boolean ignoreFields) {
		this.addUnaryProduction(this.getSymbolInt(target), this.getSymbolInt(input), isInputBackwards, ignoreFields, false);
	}
	
	public void addUnaryProduction(String target, String input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		this.addUnaryProduction(this.getSymbolInt(target), this.getSymbolInt(input), isInputBackwards, ignoreFields, ignoreContexts);
	}
	
	public void addBinaryProduction(String target, String firstInput, String secondInput) {
		this.addBinaryProduction(this.getSymbolInt(target), this.getSymbolInt(firstInput), this.getSymbolInt(secondInput), false, false, false, false);
	}
	
	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean ignoreFields) {
		this.addBinaryProduction(this.getSymbolInt(target), this.getSymbolInt(firstInput), this.getSymbolInt(secondInput), false, false, ignoreFields, false);
	}
	
	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards) {
		this.addBinaryProduction(this.getSymbolInt(target), this.getSymbolInt(firstInput), this.getSymbolInt(secondInput), isFirstInputBackwards, isSecondInputBackwards, false, false);
	}
	
	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields) {
		this.addBinaryProduction(this.getSymbolInt(target), this.getSymbolInt(firstInput), this.getSymbolInt(secondInput), isFirstInputBackwards, isSecondInputBackwards, ignoreFields, false);
	}

	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		this.addBinaryProduction(this.getSymbolInt(target), this.getSymbolInt(firstInput), this.getSymbolInt(secondInput), isFirstInputBackwards, isSecondInputBackwards, ignoreFields, ignoreContexts);
	}

	public void addProduction(String target, String[] inputs) {
		this.addProduction(target, inputs, new boolean[inputs.length], false, false);
	}

	public void addProduction(String target, String[] inputs, boolean ignoreFields) {
		this.addProduction(target, inputs, new boolean[inputs.length], ignoreFields, false);
	}
	
	public void addProduction(String target, String[] inputs, boolean[] isInputBackwards) {
		this.addProduction(target, inputs, isInputBackwards, false, false);
	}

	public void addProduction(String target, String[] inputs, boolean ignoreFields, boolean ignoreContexts) {
		this.addProduction(target, inputs, new boolean[inputs.length], ignoreFields, ignoreContexts);
	}

	public void addProduction(String target, String[] inputs, boolean[] isInputBackwards, boolean ignoreFields) {
		this.addProduction(target, inputs, isInputBackwards, ignoreFields, false);
	}
	
	public void addProduction(String target, String[] inputs, boolean[] isInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
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
		this.addAuxProduction(this.getSymbolInt(target), this.getSymbolInt(input), this.getSymbolInt(auxInput), isAuxInputFirst, false, false, false, false);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean ignoreFields) {
		this.addAuxProduction(this.getSymbolInt(target), this.getSymbolInt(input), this.getSymbolInt(auxInput), isAuxInputFirst, false, false, ignoreFields, false);
	}
	
	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards) {
		this.addAuxProduction(this.getSymbolInt(target), this.getSymbolInt(input), this.getSymbolInt(auxInput), isAuxInputFirst, isInputBackwards, isAuxInputBackwards, false, false);
	}
	
	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields) {
		this.addAuxProduction(this.getSymbolInt(target), this.getSymbolInt(input), this.getSymbolInt(auxInput), isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, false);
	}

	public void addAuxProduction(String target, String input, String auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		this.addAuxProduction(this.getSymbolInt(target), this.getSymbolInt(input), this.getSymbolInt(auxInput), isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, ignoreContexts);
	}
	
	public void addUnaryProduction(int target, int input, boolean isInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		if(target >= this.curLabel || input >= this.curLabel) {
			throw new RuntimeException("label out of range");
		}
		UnaryProduction unaryProduction = new UnaryProduction(target, input, isInputBackwards, ignoreFields);
		this.unaryProductionsByInput.get(input).add(unaryProduction);
		this.unaryProductionsByTarget.get(target).add(unaryProduction);
	}
	
	public void addBinaryProduction(int target, int firstInput, int secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		if(target >= this.curLabel || firstInput >= this.curLabel || secondInput >= this.curLabel) {
			throw new RuntimeException("label out of range");
		}
		BinaryProduction binaryProduction = new BinaryProduction(target, firstInput, secondInput, isFirstInputBackwards, isSecondInputBackwards, ignoreFields, ignoreContexts);
		this.binaryProductionsByFirstInput.get(firstInput).add(binaryProduction);
		this.binaryProductionsBySecondInput.get(secondInput).add(binaryProduction);
		this.binaryProductionsByTarget.get(target).add(binaryProduction);
	}
	
	public void addAuxProduction(int target, int input, int auxInput, boolean isAuxInputFirst, boolean isInputBackwards, boolean isAuxInputBackwards, boolean ignoreFields, boolean ignoreContexts) {
		if(target >= this.curLabel || input >= this.curLabel || auxInput >= this.curLabel) {
			throw new RuntimeException("label out of range");
		}
		AuxProduction auxProduction = new AuxProduction(target, input, auxInput, isAuxInputFirst, isInputBackwards, isAuxInputBackwards, ignoreFields, ignoreContexts);
		this.auxProductionsByInput.get(input).add(auxProduction);
		this.auxProductionsByAuxInput.get(auxInput).add(auxProduction);
		this.auxProductionsByTarget.get(target).add(auxProduction);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<this.curLabel; i++) {
			for(UnaryProduction unaryProduction : (List<UnaryProduction>)this.unaryProductionsByInput.get(i)) {
				sb.append(unaryProduction.toString()).append("\n");
			}
		}
		for(int i=0; i<this.curLabel; i++) {
			for(BinaryProduction binaryProduction : (List<BinaryProduction>)this.binaryProductionsByFirstInput.get(i)) {
				sb.append(binaryProduction.toString()).append("\n");
			}
		}
		for(int i=0; i<this.curLabel; i++) {
			for(AuxProduction auxProduction : (List<AuxProduction>)this.auxProductionsByInput.get(i)) {
				sb.append(auxProduction.toString()).append("\n");
			}
		}		
		return sb.toString();
	}
}
