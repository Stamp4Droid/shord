package stamp.missingmodels.util.cflsolver.cfg;

import java.util.ArrayList;
import java.util.List;

public final class ContextFreeGrammar {
	public final List[] unaryProductionsByInput; // list of type UnaryProduction
	public final List[] binaryProductionsByFirstInput; // list of type BinaryProduction
	public final List[] binaryProductionsBySecondInput; // list of type BinaryProduction
	
	public ContextFreeGrammar(int numLabels) {
		this.unaryProductionsByInput = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.unaryProductionsByInput[i] = new ArrayList();
		}
		this.binaryProductionsByFirstInput = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.binaryProductionsByFirstInput[i] = new ArrayList();
		}
		this.binaryProductionsBySecondInput = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.binaryProductionsBySecondInput[i] = new ArrayList();
		}
	}
	
	public void addUnaryProduction(int target, int input) {
		int numLabels = this.unaryProductionsByInput.length;
		if(target >= numLabels || input >= numLabels) {
			throw new RuntimeException("label out of range");
		}
		this.unaryProductionsByInput[input].add(new UnaryProduction(target, input));
	}
	
	public void addBinaryProduction(int target, int firstInput, int secondInput) {
		this.addBinaryProduction(target, firstInput, secondInput, false, false);
	}
	
	public void addBinaryProduction(int target, int firstInput, int secondInput, boolean isFirstInputBackwards, boolean isSecondInputBackwards) {
		int numLabels = this.unaryProductionsByInput.length;
		if(target >= numLabels || firstInput >= numLabels || secondInput >= numLabels) {
			throw new RuntimeException("label out of range");
		}
		BinaryProduction binaryProduction = new BinaryProduction(target, firstInput, secondInput, isFirstInputBackwards, isSecondInputBackwards);
		this.binaryProductionsByFirstInput[firstInput].add(binaryProduction);
		this.binaryProductionsBySecondInput[secondInput].add(binaryProduction);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<this.unaryProductionsByInput.length; i++) {
			for(UnaryProduction unaryProduction : (List<UnaryProduction>)this.unaryProductionsByInput[i]) {
				sb.append(unaryProduction.toString()).append("\n");
			}
		}
		for(int i=0; i<this.binaryProductionsByFirstInput.length; i++) {
			for(BinaryProduction binaryProduction : (List<BinaryProduction>)this.binaryProductionsByFirstInput[i]) {
				sb.append(binaryProduction.toString()).append("\n");
			}
		}
		return sb.toString();
	}
}
