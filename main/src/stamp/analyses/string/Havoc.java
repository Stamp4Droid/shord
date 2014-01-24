package stamp.analyses.string;

import soot.Local;

public class Havoc implements Statement
{
	final Local local;

	public Havoc(Local local)
	{
		this.local = local;
	}
}