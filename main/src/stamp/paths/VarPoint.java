package stamp.paths;

import shord.analyses.VarNode;

public class VarPoint implements Point {
	public final VarNode var;

	public VarPoint(VarNode var) {
		this.var = var;
	}

	@Override
	public String toString() {
		return var.toString();
	}

	@Override
	public String toShortString() {
		return toString();
	}
}
