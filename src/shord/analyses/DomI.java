package shord.analyses;

import soot.Unit;
import shord.project.Program;
import shord.project.analyses.ProgramDom;

/**
 * Domain of method invocation stmts
 * 
 * @author Saswat Anand
 */
public class DomI extends ProgramDom<Unit> {
    @Override
    public String toUniqueString(Unit u) {
		return Program.unitToString(u);
    }
}
