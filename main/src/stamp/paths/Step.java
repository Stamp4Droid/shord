package stamp.paths;

public abstract class Step {
	public final boolean reverse;
	public final Point target;

	public Step(boolean reverse, Point target) {
		// TODO: Missing the actual symbol, instead the subtype of Step is used
		// to infer the nature of the Step.
		this.reverse = reverse;
		this.target = target;
	}
}
