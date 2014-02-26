package stamp.paths;

import shord.program.Program;
import soot.Unit;

public class ObjPoint implements Point {
	public final Unit alloc;

	public ObjPoint(Unit alloc) {
		this.alloc = alloc;
	}

	@Override
	public String toString() {
		return Program.unitToString(alloc);
	}

	@Override
	public String toShortString() {
		return toString();
	}
}
