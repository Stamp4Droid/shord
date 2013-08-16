package stamp.paths;

import shord.analyses.Ctxt;
import shord.program.Program;

public class CtxtObjPoint extends CtxtPoint {

	public CtxtObjPoint(Ctxt ctxt) {
		// TODO: Check that it's a contextified object, rather than a call
		// stack.
		super(ctxt);
	}

	@Override
	public String toString() {
		return ctxt.toString();
	}

	@Override
	public String toShortString() {
		return Program.unitToString(ctxt.getElems()[0]);
	}
}
