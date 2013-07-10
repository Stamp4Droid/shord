package shord.analyses;

import soot.Unit;
import shord.project.analyses.ProgramDom;
import stamp.srcmap.SourceInfo;

/**
 * Domain of method invocation stmts
 * 
 * @author Saswat Anand
 */
public class DomI extends ProgramDom<Unit> {
    @Override
    public String toUniqueString(Unit u) {
		return SourceInfo.unitToString(u);
    }
}
