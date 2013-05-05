package shord.analyses;

import soot.SootMethod;

public class ThisVarNode extends VarNode
{
	private final SootMethod method;

	public ThisVarNode(SootMethod m)
	{
		this.method = m;
	}
}