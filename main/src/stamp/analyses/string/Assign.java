package stamp.analyses.string;

import soot.Local;
import soot.Immediate;

public class Assign implements Statement
{
	final Immediate right;
	final Local left;

	public Assign(Immediate right, Local left)
	{
		this.left = left;
		this.right = right;
	}	
}