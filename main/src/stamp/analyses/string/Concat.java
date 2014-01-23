package stamp.analyses.string;

import soot.Local;
import soot.Immediate;

public class Concat implements Statement
{
	final Immediate right1;
	final Immediate right2;
	final Immediate left;

	public Concat(Immediate right1, Immediate right2, Local left)
	{
		this.left = left;
		this.right1 = right1;
		this.right2 = right2;
	}
}