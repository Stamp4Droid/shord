package shord.analyses;

import soot.RefType;

public class GlobalStringConstantNode extends GlobalAllocNode
{
	public GlobalStringConstantNode()
	{
		super(RefType.v("java.lang.String"));
	}

	public String toString()
	{
		return "GStringAlloc$GSTRING";
	}
	
}