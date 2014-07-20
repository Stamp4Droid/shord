package chord.project.analyses.provenance.solver;

import java.util.Comparator;

public class ClauseLHGCMP  implements Comparator<SimpleClause>{
	@Override
	public int compare(SimpleClause o1, SimpleClause o2) {
		if(o1.isFixed() && !o2.isFixed())
			return -1;
		if(!o1.isFixed() && o2.isFixed())
			return 1;
		return 1;
	}
}
