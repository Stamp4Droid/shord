package shord.analyses;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.Type;
import soot.SootMethod;
import soot.jimple.StringConstant;
import soot.Value;

/*
 * @author Yu Feng
 */

public class StringConstNode extends SiteAllocNode
{
	public final String value;

	public StringConstNode(Unit u)
	{
		super(u);
		Value rightOp = ((AssignStmt) u).getRightOp();
		assert rightOp instanceof StringConstant;
		this.value = ((StringConstant)rightOp).value;
	}

	public String getValue()
	{
		return value;
	}

	public String toString()
	{
		return "StringConst$" + value; 
	}
}
