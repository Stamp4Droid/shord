package stamp.paths;

import soot.Unit;

// When traversed in forward, this step moves from the actual parameter of a
// call to the corresponding formal parameter. The context is changed by
// pushing 'invk' on the stack. When traversed in reverse, i.e. when moving
// from the formal back to the actual parameter, the stack is popped, and the
// removed invocation should be equal to 'invk', otherwise we get a stack
// break.
public class CallStep extends Step {
	public final Unit invk;

	public CallStep(boolean reverse, Point target, Unit invk) {
		super(reverse, target);
		this.invk = invk;
	}
}
