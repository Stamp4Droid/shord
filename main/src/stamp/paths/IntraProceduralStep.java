package stamp.paths;

// Crossing this step does not affect the solver's view of the stack. This
// usually corresponds to an operation between variables residing in the same
// method.
public class IntraProceduralStep extends Step {
	public IntraProceduralStep(boolean reverse, Point target) {
		super(reverse, target);
	}
}
