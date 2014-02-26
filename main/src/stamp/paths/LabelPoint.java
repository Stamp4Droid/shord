package stamp.paths;

public class LabelPoint implements Point {
	public final String label;

	public LabelPoint(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public String toShortString() {
		return label;
	}
}
