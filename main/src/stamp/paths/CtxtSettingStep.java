package stamp.paths;

import shord.analyses.Ctxt;

// This resets the stack altogether, i.e. the solver's view of the stack on
// one end is unrelated to that on the other end (regardless of the direction
// the step is traversed). Crossing such an edge introduces a benign stack
// break.
public class CtxtSettingStep extends CtxtStep {

	public CtxtSettingStep(boolean reverse, Point target, Ctxt ctxt) {
		super(reverse, target);
		this.ctxt = ctxt;
	}
}
