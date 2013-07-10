package shord.analyses;

import soot.Unit;
import shord.project.analyses.ProgramDom;
import stamp.srcmap.SourceInfo;

/**
 * Domain of new stmts
 * 
 * @author Saswat Anand
 */
public class DomH extends ProgramDom<Unit> {
    @Override
    public String toUniqueString(Unit u) {
		return SourceInfo.unitToString(u);
    }
}
