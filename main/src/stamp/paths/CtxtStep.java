package stamp.paths;

import shord.analyses.Ctxt;

// This resets the stack altogether, i.e. the solver's view of the stack on
// one end is unrelated to that on the other end (regardless of the direction
// the step is traversed). Crossing such an edge introduces a benign stack
// break.
public abstract class CtxtStep extends Step {
	// CAUTION: This context is set on the destination of the underlying Edge,
	// which is NOT the same as the Step's target, if the Step is in reverse.
	public final Ctxt ctxt;
}
