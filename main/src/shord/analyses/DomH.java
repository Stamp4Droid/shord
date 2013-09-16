package shord.analyses;

import soot.Unit;
import shord.program.Program;
import shord.project.analyses.ProgramDom;

/**
 * Domain of new stmts
 * 
 * @author Saswat Anand
 */
//public class DomH extends ProgramDom<Unit> {
public class DomH extends ProgramDom<AllocNode> {
    /*@Override
    public String toUniqueString(AllocNode u) {
	return u.toString();
    }*/
}
