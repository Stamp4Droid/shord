package stamp.paths;

import soot.Unit;

public class CallStep extends Step {
	public final Unit invk;

	public CallStep(boolean reverse, Point target, Unit invk) {
		super(reverse, target);
		this.invk = invk;
	}
}
