package shord.analyses;

import soot.Unit;
import soot.Type;
import soot.jimple.AssignStmt;

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
	
	public Type getType()
	{
		return ((AssignStmt) unit).getRightOp().getType();
	}
}
