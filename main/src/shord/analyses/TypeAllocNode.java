package shord.analyses;

import soot.Type;

public class TypeAllocNode extends AllocNode
{

	public final Type type;

	public TypeAllocNode(Type t)
	{
		this.type= t;
	}

	public Type getType()
	{

		return type;
	}

	public String toString()
	{
		return "typeAlloc@" + type;
	}

}
