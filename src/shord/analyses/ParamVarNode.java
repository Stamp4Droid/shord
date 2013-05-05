package shord.analyses;

import soot.SootMethod;

public class ParamVarNode extends VarNode
{
	private final SootMethod method;
	private final int index;

	public ParamVarNode(SootMethod m, int i)
	{
		this.method = m;
		this.index = i;
	}
}