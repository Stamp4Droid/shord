package stamp.paths;

import shord.analyses.Ctxt;

public abstract class CtxtStep extends Step {
	public abstract Ctxt getCtxt();

	public CtxtStep(boolean reverse, Point target) {
		super(reverse, target);
	}
}
