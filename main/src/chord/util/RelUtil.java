package chord.util;

import shord.analyses.DomH;
import shord.analyses.DomI;
import shord.analyses.DomK;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;

/** Use by saying {@code import static chord.util.RelUtil.*;}. */
public final class RelUtil {
    private RelUtil() { /* no instance */ }
	public static ProgramRel pRel(String name) { return (ProgramRel) ClassicProject.g().getTrgt(name); }
	public static DomI domI() { return (DomI) ClassicProject.g().getTrgt("I"); }
	public static DomK domK() { return (DomK) ClassicProject.g().getTrgt("K"); }
	public static DomH domH() { return (DomH) ClassicProject.g().getTrgt("H"); }
}
