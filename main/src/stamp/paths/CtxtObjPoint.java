package stamp.paths;

import shord.analyses.Ctxt;

public class CtxtObjPoint extends CtxtPoint {

	public CtxtObjPoint(Ctxt ctxt) {
		// TODO: Check that it's a contextified object, rather than a call
		// stack.
		super(ctxt);
	}

	public String toString() {
		return ctxt.toString();
	}
}
