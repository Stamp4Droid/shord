package stamp.paths;

import java.util.Arrays;
import shord.analyses.Ctxt;
import shord.program.Program;
import soot.Unit;

public class CtxtObjPoint implements CtxtPoint {
	private final Unit[] elems;
	public final Unit alloc;

	public CtxtObjPoint(Ctxt ctxtAndAlloc) {
		// Separate the first statement, which should correspond to the
		// abstract object's allocation statement (TODO: Verify that this is
		// the case).
		Unit[] elemsObj = ctxtAndAlloc.getElems();
		this.elems = Arrays.copyOfRange(elemsObj, 1, elemsObj.length);
		this.alloc = elemsObj[0];
	}

	@Override
	public Unit[] getElems() {
		return elems;
	}

	@Override
	public String toString() {
		return Ctxt.toString(false, elems) + ":" + Program.unitToString(alloc);
	}

	@Override
	public String toShortString() {
		return Program.unitToString(alloc);
	}
}
