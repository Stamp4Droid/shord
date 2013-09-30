package shord.analyses;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.Type;
import soot.SootMethod;
import soot.jimple.StringConstant;
import soot.Value;

public class StringConstNode extends AllocNode
{
	public final String value;
	public final Unit unit;

	public StringConstNode(Unit u)
	{
		Value rightOp = ((AssignStmt) u).getRightOp();
		assert rightOp instanceof StringConstant;
		this.value = ((StringConstant)rightOp).value;
		this.unit = u;
	}

	public String getValue()
	{
		return value;
	}

	public String toString()
	{
		return "StringConst$" + value; 
	}

	public Type getType()
	{
		return ((AssignStmt) unit).getRightOp().getType();
	}

}
