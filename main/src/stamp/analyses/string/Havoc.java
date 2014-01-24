package stamp.analyses.string;

import soot.Local;

public class Havoc implements Statement
{
	final Local local;

	public Havoc(Local local)
	{
		this.local = local;
	}

	public boolean equals(Object other)
	{
		if(!(other instanceof Havoc))
			return false;
		return local.equals(((Havoc) other).local);
	}
	
	public int hashCode()
	{
		return local.hashCode();
	}
}