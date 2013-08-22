package stamp.paths;

import soot.jimple.spark.pag.SparkField;

public class StoreStep extends Step {
	public final SparkField field;

	public StoreStep(boolean reverse, Point target, SparkField field) {
		super(reverse, target);
		this.field = field;
	}
}
