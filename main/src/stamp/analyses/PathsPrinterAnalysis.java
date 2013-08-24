package stamp.analyses;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import stamp.paths.CallStep;
import stamp.paths.CtxtCrossingStep;
import stamp.paths.CtxtLabelPoint;
import stamp.paths.CtxtObjPoint;
import stamp.paths.CtxtPoint;
import stamp.paths.CtxtSettingStep;
import stamp.paths.Path;
import stamp.paths.PathsAdapter;
import stamp.paths.Point;
import stamp.paths.ReturnStep;
import stamp.paths.StatFldPoint;
import stamp.paths.Step;
import stamp.paths.VarPoint;
import stamp.util.PropertyHelper;

import chord.project.Chord;
import shord.analyses.Ctxt;
import shord.project.analyses.JavaAnalysis;
import soot.Unit;

@Chord(
	name = "paths-printer-java"
)
public class PathsPrinterAnalysis extends JavaAnalysis {
	// the partial stack, bottom is on the left
	List<Unit> stack = new ArrayList<Unit>();

	@Override
	public void run() {
		String schemaFile = PropertyHelper.getProperty("stamp.paths.schema");
		String rawPathsFile = PropertyHelper.getProperty("stamp.paths.raw");
		String normalPathsFile =
			PropertyHelper.getProperty("stamp.paths.normal");
		String flatPathsFile = PropertyHelper.getProperty("stamp.paths.flat");

		try {
			PathsAdapter adapter = new PathsAdapter(schemaFile);
			adapter.normalizeRawPaths(rawPathsFile, normalPathsFile);

			List<Path> paths = adapter.getFlatPaths(rawPathsFile);
			PrintWriter pw = new PrintWriter(flatPathsFile);
			pw.println("PATHS: " + paths.size());
			pw.println();

			for (Path p : paths) {
				int breaks = 0;
				initStack(getElems(p.start));
				pw.println(p.start + " --> " + p.end);
				for (Step s : p.steps) {
					if (!recordStep(s)) {
						pw.println(">>> BROKEN: <<<");
						breaks++;
					}
					if (!recordTarget(s.target)) {
						pw.println(">>> BROKEN: <<<");
						breaks++;
					}
					pw.println((s.reverse ? "<-- " : "--> ") + s.target);
				}
				if (breaks > 0) {
					pw.println("INVALID");
				}
				pw.println();
			}

			pw.close();
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private void initStack(Unit[] elems) {
		stack.clear();
		boolean validMatch = matchStack(elems, 0);
		assert(validMatch);
	}

	// TODO:
	// - record more information when the stack breaks unexpectedly
	// - this functionality should be broken off into other classes
	//   e.g. in PartialStack, Ctxt, and *Point classes
	//   Ctxt, in particular, should contain the information of whether it's a
	//   just a call stack or a contextified abstract object (as long as we're
	//   sticking to kCFA-sensitivity)

	private boolean recordStep(Step step) {
		boolean matched = true;
		Unit[] elemsToSet = null;

		if (step instanceof CallStep) {
			Unit invk = ((CallStep) step).invk;
			if (step.reverse) {
				// Handle as a return (stack pop): Verify that the invocation
				// we're following is actually the one at the top of the stack,
				// then remove it.
				matched = matchStack(new Unit[]{invk}, 0);
				if (matched && stack.size() > 0) {
					stack.remove(0);
				}
			} else {
				// Handle as a call (stack push): Simply add the followed
				// invocation to the top of the stack.
				stack.add(0, invk);
			}
		} else if (step instanceof ReturnStep) {
			// Exactly opposite to above case.
			Unit invk = ((ReturnStep) step).invk;
			if (step.reverse) {
				stack.add(0, invk);
			} else {
				matched = matchStack(new Unit[]{invk}, 0);
				if (matched && stack.size() > 0) {
					stack.remove(0);
				}
			}
		} else if (step instanceof CtxtCrossingStep) {
			// When this step was followed, it altered the depth of the stack
			// that the solver could see, but that shouldn't create a stack
			// break, i.e. the context it carries should be compatible with the
			// current view of the stack.
			Unit[] elems = ((CtxtCrossingStep) step).ctxt.getElems();
			matched = matchStack(elems, 0);
		} else if (step instanceof CtxtSettingStep) {
			// When the solver followed the underlying edge for this step, it
			// completely switched contexts.
			Unit[] elems = ((CtxtSettingStep) step).ctxt.getElems();
			if (step.reverse) {
				// This step set the context at its target node, which
				// corresponds to the point we're currently on (since this step
				// is traversed in reverse). Therefore, the context on the step
				// should be compatible with the one on the current point.
				matched = matchStack(elems, 0);
				// The context from which this edge originated could be
				// arbitrary, so we need to reset our view of the stack.
				elemsToSet = new Unit[0];
			} else {
				// We're moving to a new context, entirely dictated by the
				// context on this step.
				elemsToSet = elems;
			}
		} else {
			// All other steps do not affect the context.
			return true;
		}

		if (!matched && elemsToSet == null) {
			elemsToSet = new Unit[0];
		}
		if (elemsToSet != null) {
			initStack(elemsToSet);
		}
		return matched;
	}

	private Unit[] getElems(Point p) {
		if (p instanceof CtxtPoint) {
			return ((CtxtPoint) p).getElems();
		} else {
			// The other kinds of points carry no context information.
			return new Unit[0];
		}
	}

	private boolean recordTarget(Point target) {
		Unit[] elems = getElems(target);
		if (!matchStack(elems, 0)) {
			initStack(elems);
			return false;
		}
		return true;
	}

	private boolean matchStack(Unit[] elems, int base) {
		int elemsIdx = 0;
		int stackIdx = base;

		while (elemsIdx < elems.length && stackIdx < stack.size()) {
			// Neither the partial stack or the target's context is exhausted,
			// cross-check the next statement.
			if (stackIdx >= 0 &&
				!stack.get(stackIdx).equals(elems[elemsIdx])) {
				// Failed to match up the partial stack with the context.
				return false;
			}
			elemsIdx++;
			stackIdx++;
		}

		// Exhausted either the context or the partial stack without detecting
		// any conflict. Update the stack and return successfully.

		// Extend the partial stack upwards if needed.
		for (; elemsIdx < elems.length; elemsIdx++) {
			stack.add(elems[elemsIdx]);
		}
		// Extend the partial stack downwards if needed.
		for (int i = -(base + 1); i >= 0; i--) {
			stack.add(0, elems[i]);
		}
		// Pop off frames after a return.
		for (int i = base; i > 0; i--) {
			stack.remove(0);
		}

		return true;
	}
}
