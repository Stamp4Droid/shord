package stamp.paths;

import shord.analyses.Ctxt;

public class CtxtLabelPoint implements Point {
	public final Ctxt ctxt;
	public final String label;

	public CtxtLabelPoint(Ctxt ctxt, String label) {
		this.ctxt = ctxt;
		this.label = label;
	}

	@Override
	public String toString() {
		return ctxt.toString() + ":" + label;
	}

	@Override
	public String toShortString() {
		return label;
	}
}
