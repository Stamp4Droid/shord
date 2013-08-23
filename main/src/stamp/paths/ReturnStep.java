package stamp.paths;

import soot.Unit;

// When traversed in forward, this step moves from the return statement of a
// method to the variable receiving the method's result at the invocation site.
// The context is changed by popping the top-most invocation from the stack,
// which should be equal to 'invk' (otherwise we have a stack break).
// When traversed in reverse, i.e. when moving from the result receiver back to
// the return statement, 'invk' is pushed on the stack instead (and there is no
// danger of breaking the stack).
public class ReturnStep extends Step {
	public final Unit invk;

	public ReturnStep(boolean reverse, Point target, Unit invk) {
		super(reverse, target);
		this.invk = invk;
	}
}
