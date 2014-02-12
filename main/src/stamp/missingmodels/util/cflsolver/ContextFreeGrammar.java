package stamp.missingmodels.util.cflsolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContextFreeGrammar {
	public final class BinaryProduction {
		public final int target;
		public final int firstInput;
		public final int secondInput;
		public final boolean isFirstInputBackwards;
		public final boolean isSecondInputBackwards;
		
		public BinaryProduction(int target, int firstInput, int secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards) {
			this.target = target;
			this.firstInput = firstInput;
			this.secondInput = secondInput;
			this.isFirstInputBackwards = isFirstInputBackwards;
			this.isSecondInputBackwards = isSecondInputBackwards;
		}
		
		public BinaryProduction(int target, int firstInput, int secondInput) {
			this(target, firstInput, secondInput, false, false);
		}
		
		@Override
		public String toString() {
			return getLabelString(this.target) + " :- " + (this.isFirstInputBackwards ? "_" : "") + getLabelString(this.firstInput) + ", " + (this.isSecondInputBackwards ? "_" : "") + getLabelString(this.secondInput) + ".";
		}
	}
	
	public final class UnaryProduction {
		public final int target;
		public final int input;
		
		public UnaryProduction(int target, int input) {
			this.target = target;
			this.input = input;
		}
		
		@Override
		public String toString() {
			return getLabelString(this.target) + " :- " + getLabelString(this.input) + ".";
		}
	}
	
	public final List<List<UnaryProduction>> unaryProductionsByInput; // list of type UnaryProduction
	public final List<List<BinaryProduction>> binaryProductionsByFirstInput; // list of type BinaryProduction
	public final List<List<BinaryProduction>> binaryProductionsBySecondInput; // list of type BinaryProduction
	
	private final Map<String,Integer> labels = new HashMap<String,Integer>();
	private final Map<Integer,String> labelStrings = new HashMap<Integer,String>();
	private int curLabel = 0;
	
	public ContextFreeGrammar() {
		this.unaryProductionsByInput = new ArrayList<List<UnaryProduction>>();
		this.binaryProductionsByFirstInput = new ArrayList<List<BinaryProduction>>();
		this.binaryProductionsBySecondInput = new ArrayList<List<BinaryProduction>>();
	}
	
	public int numLabels() {
		return this.curLabel;
	}
	
	public int getLabel(String label) {
		Integer intLabel = this.labels.get(label);
		if(intLabel == null) {
			this.unaryProductionsByInput.add(new ArrayList<UnaryProduction>());
			this.binaryProductionsByFirstInput.add(new ArrayList<BinaryProduction>());
			this.binaryProductionsBySecondInput.add(new ArrayList<BinaryProduction>());
			intLabel = this.curLabel++;
			this.labels.put(label, intLabel);
			this.labelStrings.put(intLabel, label);
		}
		return intLabel;
	}
	
	public String getLabelString(int label) {
		return this.labelStrings.get(label);
	}
	
	public void addUnaryProduction(String target, String input) {
		this.addUnaryProduction(this.getLabel(target), this.getLabel(input));
	}
	
	public void addBinaryProduction(String target, String firstInput, String secondInput) {
		this.addBinaryProduction(this.getLabel(target), this.getLabel(firstInput), this.getLabel(secondInput));
	}
	
	public void addBinaryProduction(String target, String firstInput, String secondInput, boolean isFirstinputBackwards, boolean isSecondInputBackwards) {
		this.addBinaryProduction(this.getLabel(target), this.getLabel(firstInput), this.getLabel(secondInput), isFirstinputBackwards, isSecondInputBackwards);
	}

	public void addProduction(String target, String[] inputs) {
		this.addProduction(target, inputs, new boolean[inputs.length]);
	}
	
	public void addProduction(String target, String[] inputs, boolean[] isInputBackwards) {
		if(inputs.length <= 2 || inputs.length != isInputBackwards.length) {
			throw new RuntimeException("Invalid production");
		}
		String prevInput = inputs[0];
		String curInput = "^" + (isInputBackwards[0] ? "_" : "") + inputs[0] + "^" + (isInputBackwards[1] ? "_" : "") + inputs[1];
		this.addBinaryProduction(curInput, prevInput, inputs[1], isInputBackwards[0], isInputBackwards[1]);
		prevInput = curInput;
		for(int i=2; i<inputs.length-1; i++) {
			curInput = prevInput + "^" + (isInputBackwards[i] ? "_" : "") + inputs[i];
			this.addBinaryProduction(curInput, prevInput, inputs[i], false, isInputBackwards[i]);
			prevInput = curInput;
		}
		this.addBinaryProduction(target, prevInput, inputs[inputs.length-1], false, isInputBackwards[inputs.length-1]);
	}
	
	public void addUnaryProduction(int target, int input) {
		if(target >= this.curLabel || input >= this.curLabel) {
			throw new RuntimeException("label out of range");
		}
		this.unaryProductionsByInput.get(input).add(new UnaryProduction(target, input));
	}
	
	public void addBinaryProduction(int target, int firstInput, int secondInput) {
		this.addBinaryProduction(target, firstInput, secondInput, false, false);
	}
	
	public void addBinaryProduction(int target, int firstInput, int secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards) {
		if(target >= this.curLabel || firstInput >= this.curLabel || secondInput >= this.curLabel) {
			throw new RuntimeException("label out of range");
		}
		BinaryProduction binaryProduction = new BinaryProduction(target, firstInput, secondInput, isFirstInputBackwards, isSecondInputBackwards);
		this.binaryProductionsByFirstInput.get(firstInput).add(binaryProduction);
		this.binaryProductionsBySecondInput.get(secondInput).add(binaryProduction);
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
		return sb.toString();
	}
}
