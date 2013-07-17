package stamp.paths;

import shord.analyses.Ctxt;
import shord.analyses.VarNode;

public class CtxtVarPoint implements Point {
	public final Ctxt ctxt;
	public final VarNode var;

	public CtxtVarPoint(Ctxt ctxt, VarNode var) {
		// TODO: Check that it's a call stack, rather than a contextified
		// object.
		this.ctxt = ctxt;
		// TODO: Check that it's a valid context for this variable.
		this.var = var;
	}

	public String toString() {
		return ctxt.toString() + ":" + var.toString();
	}
}
