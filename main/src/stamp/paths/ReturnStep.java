package stamp.paths;

import soot.Unit;

public class ReturnStep extends Step {
	public final Unit invk;

	public ReturnStep(boolean reverse, Point target, Unit invk) {
		super(reverse, target);
		this.invk = invk;
	}
}
