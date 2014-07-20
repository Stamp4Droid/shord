package chord.project.analyses.provenance.solver;

import java.util.ArrayList;
import java.util.List;

import chord.project.analyses.provenance.ConstraintItem;
import chord.project.analyses.provenance.Tuple;
import chord.util.ArraySet;

/**
 * A simple disjunct clause class
 * 
 * @author xin
 * 
 */
public class SimpleClause {
	private ArraySet<Tuple> disjunct = new ArraySet<Tuple>();;
	private boolean isFixed = true;// this is fixed when disjunct only contains
									// input tuples
	public final static boolean CHECK_NON_PARAM = true;

	public SimpleClause() {
	}

	public SimpleClause(Tuple t) {
		this.add(t);
	}

	private boolean add(Tuple t) {
		String rName = t.getRelName();
		if (!SimpleDNF.internalTuples.contains(t) && SimpleDNF.controllableRs.contains(rName))
			isFixed = false;
		if (CHECK_NON_PARAM) {
			if (!SimpleDNF.internalTuples.contains(t))
				if (SimpleDNF.controllableRs.contains(rName) || SimpleDNF.parameterRs.contains(rName))
					return disjunct.add(t);
		} else
			return this.disjunct.add(t);
		return false;
	}

	public SimpleClause(List<Tuple> dis) {
		for (Tuple t : dis) {
			this.add(t);
		}
	}

	public boolean isFixed() {
		return isFixed;
	}

	public int size() {
		return disjunct.size();
	}

	public ArraySet<Tuple> getTuples() {
		return disjunct;
	}

	public SimpleClause intersect(SimpleClause other) {
		return this.intersect(other.getTuples());
	}

	public SimpleClause intersect(List<Tuple> other) {
		SimpleClause ret = new SimpleClause(this.disjunct);
		for (Tuple t : other)
			ret.add(t);
		return ret;
	}

	public List<SimpleClause> expandFirstT(Dictionary dic) {
		Tuple t = null;
		int i = 0;
		for (; i < disjunct.size(); i++) {
			Tuple t1 = disjunct.get(i);
			if (SimpleDNF.controllableRs.contains(t1.getRelName()) && !SimpleDNF.internalTuples.contains(t1)) {
				t = t1;
				break;
			}
		}
		ArraySet<Tuple> retPart = (ArraySet<Tuple>) disjunct.clone();
		retPart.remove(i);
		List<SimpleClause> ret = new ArrayList<SimpleClause>();
		List<ConstraintItem> rules = dic.getConstraintItems(t);
		for (ConstraintItem ci : rules) {
			System.out.println("Constraint applied: " + ci);
			SimpleClause sc = new SimpleClause(retPart);
			sc = sc.intersect(ci.subTuples);
			ret.add(sc);
		}
		if (ret.size() == 0)
			throw new RuntimeException();
		return ret;
	}

	public SimpleClause intersect(Tuple t) {
		ArraySet<Tuple> nDis = new ArraySet<Tuple>(disjunct);
		nDis.add(t);
		return new SimpleClause(nDis);
	}

	public boolean contains(SimpleClause other) {
		return other.disjunct.containsAll(disjunct);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((disjunct == null) ? 0 : disjunct.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleClause other = (SimpleClause) obj;
		if (disjunct == null) {
			if (other.disjunct != null)
				return false;
		} else if (!disjunct.equals(other.disjunct))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		if (disjunct.size() > 0)
			sb.append(disjunct.get(0));
		for (int i = 1; i < disjunct.size(); i++)
			sb.append(" /\\ " + disjunct.get(i));
		sb.append(")");
		return sb.toString();
	}

}
