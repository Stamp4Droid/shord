package shord.analyses;

import soot.Type;
import soot.RefType;

public class GlobalStringNode extends AllocNode
{
	public GlobalStringNode()
	{

	}

	public Type getType()
	{
        return RefType.v("java.lang.String");
	}

	public String toString()
	{
		return "GStringAlloc$GSTRING";
	}
	
}
