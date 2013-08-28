package stamp.paths;

import shord.analyses.Ctxt;

// This step sets a new context on its destination, but must keep a consistent
// view of the stack. Either way we traverse it, it should not break the stack.
public class CtxtCrossingStep extends CtxtStep {

	// CAUTION: This context is set on the destination of the underlying Edge,
	// which is NOT the same as the Step's target, if the Step is in reverse.
	public final Ctxt ctxt;


	public CtxtCrossingStep(boolean reverse, Point target, Ctxt ctxt) {
		super(reverse, target);
		this.ctxt = ctxt;
	}

	public Ctxt getCtxt() {
		return ctxt;
	}
}
