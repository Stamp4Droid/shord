package stamp.paths;

import shord.analyses.Ctxt;

// This resets the stack altogether.
public class CtxtSettingStep extends Step {
	// CAUTION: This context is set on the destination of the underlying Edge,
	// which is NOT the same as the Step's target, if the Step is in reverse.
	public final Ctxt ctxt;

	public CtxtSettingStep(boolean reverse, Point target, Ctxt ctxt) {
		super(reverse, target);
		this.ctxt = ctxt;
	}
}
