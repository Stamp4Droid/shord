package shord.analyses;

import soot.Type;
import soot.SootMethod;

/*
 * @author Yu Feng
 * @author Saswat Anand
 */
public class StubAllocNode extends AllocNode
{
	private final Type type;
	private final SootMethod method;

	public StubAllocNode(Type t, SootMethod m)
	{
		super(t);
		this.method= m;
	}

	public SootMethod getMethod()
	{
		return method;
	}

	public String toString()
	{
		return "StubAlloc$" + type + "@" + method;
	}
}
