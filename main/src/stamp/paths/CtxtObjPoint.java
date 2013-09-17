package stamp.paths;

import shord.analyses.Ctxt;
import shord.program.Program;

public class CtxtObjPoint implements Point {
	public final Ctxt ctxt;

	public CtxtObjPoint(Ctxt ctxt) {
		// TODO: Check that it's a contextified object, rather than a call
		// stack.
		assert(ctxt.getElems().length > 0);
		this.ctxt = ctxt;
	}

	@Override
	public String toString() {
		return ctxt.toString(true);
	}

	@Override
	public String toShortString() {
		//return Program.unitToString(ctxt.getElems()[0]);
		return ctxt.getElems()[0].toString();
	}
}
