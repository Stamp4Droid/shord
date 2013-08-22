package stamp.paths;

import soot.jimple.spark.pag.SparkField;

public class LoadStep extends Step {
	public final SparkField field;

	public LoadStep(boolean reverse, Point target, SparkField field) {
		super(reverse, target);
		this.field = field;
	}
}
