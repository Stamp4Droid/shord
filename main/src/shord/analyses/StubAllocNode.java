package shord.analyses;

import soot.Type;
import soot.SootMethod;

/*
 * @author Yu Feng
 * @author Saswat Anand
 */
public class StubAllocNode extends GlobalAllocNode
{
	public StubAllocNode(Type t)
	{
		super(t);
	}

	public String toString()
	{
		return "StubAlloc$" + type;
	}
}
