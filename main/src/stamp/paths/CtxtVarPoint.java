package stamp.paths;

import shord.analyses.Ctxt;
import shord.analyses.VarNode;

public class CtxtVarPoint extends CtxtObjPoint {
	public final VarNode var;

	public CtxtVarPoint(Ctxt ctxt, VarNode var) {
		// TODO: Check that it's a call stack, rather than a contextified
		// object.
		super(ctxt);
		// TODO: Check that it's a valid context for this variable.
		this.var = var;
	}

	@Override
	public String toString() {
		return ctxt.toString() + ":" + var.toString();
	}
}
