package shord.analyses;

import soot.Local;

public class LocalVarNode extends VarNode
{
	public final Local local;

	public LocalVarNode(Local l)
	{
		this.local = l;
	}
}