package shord.analyses;

import soot.Unit;

//public class SiteAllocNode extends AllocNode
public class SiteAllocNode extends AllocNode
{
	///public final Unit unit;
	public Unit unit;

	public SiteAllocNode(Unit u)
	{
		this.unit = u;
	}

	public Unit getUnit()
	{
		return unit;

	}

	public String toString()
	{
		return "siteAlloc@" + unit;
	}

}
