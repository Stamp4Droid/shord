package stamp.paths;

public class LabelPoint implements Point {
	public final String label;

	public LabelPoint(String label) {
		this.label = label;
	}

	public String toString() {
		return label;
	}
}
