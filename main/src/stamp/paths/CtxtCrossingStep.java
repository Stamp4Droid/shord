package stamp.paths;

import shord.analyses.Ctxt;

// This step changes the context, but must keep a consistent view of the stack.
public class CtxtCrossingStep extends Step {
	// CAUTION: This context is set on the destination of the underlying Edge,
	// which is NOT the same as the Step's target, if the Step is in reverse.
	public final Ctxt ctxt;

	public CtxtCrossingStep(boolean reverse, Point target, Ctxt ctxt) {
		super(reverse, target);
		this.ctxt = ctxt;
	}
}
