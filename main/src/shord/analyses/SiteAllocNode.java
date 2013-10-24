package shord.analyses;

import soot.Unit;
import soot.Type;
import soot.jimple.AssignStmt;
import shord.program.Program;

public class SiteAllocNode extends AllocNode
{
    public Unit unit;

    public SiteAllocNode(Unit u)
    {
        this.unit = u;
    }

    public Unit getUnit()
    {
        return unit;

    }

    public String toString()
    {
        return "SiteAlloc$" + Program.unitToString(unit);
    }
    
    public Type getType()
    {
        return ((AssignStmt) unit).getRightOp().getType();
    }
}
