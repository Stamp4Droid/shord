package shord.analyses;

import soot.Type;
import soot.SootMethod;

public class StubAllocNode extends AllocNode
{

	public final Type type;
	public final SootMethod method;

	public StubAllocNode(Type t, SootMethod m)
	{
		this.type= t;
		this.method= m;
	}

	public Type getType()
	{

		return type;
	}

	public SootMethod getMethod()
	{

		return method;
	}


	public String toString()
	{
		return "stubAlloc@" + type + "@" + method;
	}

}
