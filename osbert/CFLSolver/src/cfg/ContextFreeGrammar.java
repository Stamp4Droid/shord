package cfg;

import java.util.LinkedList;
import java.util.List;

public final class ContextFreeGrammar {
	public final List[] unaryProductionsByInput; // list of type UnaryProduction
	public final List[] binaryProductionsByFirstInput; // list of type BinaryProduction
	public final List[] binaryProductionsBySecondInput; // list of type BinaryProduction
	
	public ContextFreeGrammar(UnaryProduction[] unaryProductions, BinaryProduction[] binaryProductions, int numLabels) {
		this.unaryProductionsByInput = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.unaryProductionsByInput[i] = new LinkedList();
		}
		this.binaryProductionsByFirstInput = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.binaryProductionsByFirstInput[i] = new LinkedList();
		}
		this.binaryProductionsBySecondInput = new List[numLabels];
		for(int i=0; i<numLabels; i++) {
			this.binaryProductionsBySecondInput[i] = new LinkedList();
		}
		
		for(int i=0; i<unaryProductions.length; i++) {
			this.unaryProductionsByInput[unaryProductions[i].input].add(unaryProductions[i]);
		}
		for(int i=0; i<binaryProductions.length; i++) {
			this.binaryProductionsByFirstInput[binaryProductions[i].firstInput].add(binaryProductions[i]);
			this.binaryProductionsBySecondInput[binaryProductions[i].secondInput].add(binaryProductions[i]);
		}
	}
}
