package chord.project.analyses.provenance.solver;

import java.util.Comparator;

/**
 * The comparator based on SimpleClause size. Note if compare return 0, it doesn't necessarily mean o1 == o2 here
 * @author xin
 *
 */
public class ClauseSizeCMP implements Comparator<SimpleClause> {

	@Override
	public int compare(SimpleClause o1, SimpleClause o2) {
		if(o1.isFixed() && !o2.isFixed())
			return -1;
		if(!o1.isFixed() && o2.isFixed())
			return 1;
		return o1.size() - o2.size();
	}

}
