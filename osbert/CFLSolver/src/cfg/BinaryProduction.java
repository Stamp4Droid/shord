package cfg;

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
		this.target = target;
		this.firstInput = firstInput;
		this.secondInput = secondInput;
		this.isFirstInputBackwards = false;
		this.isSecondInputBackwards = false;
	}
}
