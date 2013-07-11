package shord.analyses;

import soot.Unit;
import shord.program.Program;
import shord.project.analyses.ProgramDom;

/**
 * Domain of new stmts
 * 
 * @author Saswat Anand
 */
public class DomH extends ProgramDom<Unit> {
    @Override
    public String toUniqueString(Unit u) {
		return Program.unitToString(u);
    }
}
