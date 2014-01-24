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
	
	public boolean equals(Object other)
	{
		if(!(other instanceof Assign))
			return false;
		Assign as = (Assign) other;
		return right.equals(as.right) && left.equals(as.left);
	}
	
	public int hashCode()
	{
		return right.hashCode()+left.hashCode();
	}
}