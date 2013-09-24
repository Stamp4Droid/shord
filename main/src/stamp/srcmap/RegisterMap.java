package stamp.srcmap;

import java.util.Map;
import java.util.Set;

import soot.Local;

/**
 * @author Saswat Anand 
 */
public interface RegisterMap {
	public Set<Expr> srcLocsFor(Local var);
	public Map<Local,Set<Expr>> allSrcLocs();
}
