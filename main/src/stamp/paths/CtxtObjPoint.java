package stamp.paths;

import shord.analyses.Ctxt;

public class CtxtObjPoint implements Point {
	public final Ctxt ctxt;

	public CtxtObjPoint(Ctxt ctxt) {
		// TODO: Check that it's a contextified object, rather than a call
		// stack.
		this.ctxt = ctxt;
	}

	public String toString() {
		return ctxt.toString();
	}
}
