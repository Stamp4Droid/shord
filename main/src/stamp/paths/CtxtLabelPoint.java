package stamp.paths;

import shord.analyses.Ctxt;
import soot.Unit;

public class CtxtLabelPoint implements CtxtPoint {
	private final Unit[] elems;
	public final String label;

	public CtxtLabelPoint(Ctxt ctxt, String label) {
		this.elems = ctxt.getElems();
		this.label = label;
	}

	@Override
	public Unit[] getElems() {
		return elems;
	}

	@Override
	public String toString() {
		return Ctxt.toString(false, elems) + ":" + label;
	}

	@Override
	public String toShortString() {
		return label;
	}
}
