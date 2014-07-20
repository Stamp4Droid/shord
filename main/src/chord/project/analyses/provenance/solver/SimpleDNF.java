package chord.project.analyses.provenance.solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import chord.project.analyses.provenance.Tuple;
import chord.util.ArraySet;

/**
 * A simple DNF with underapproximation
 * 
 * @author xin
 * 
 */
public class SimpleDNF {
	private List<SimpleClause> dnf;
	public static boolean sub_on = true;
	public static Comparator<SimpleClause> cmp = new ClauseLHGCMP();
	public static Set<String> controllableRs;
	public static Set<String> parameterRs;
	public static Set<Tuple> internalTuples;

	public SimpleDNF() {
		dnf = new ArrayList<SimpleClause>();
	}

	public SimpleDNF(Tuple t){
		SimpleClause sc = new SimpleClause(t);
		dnf = new ArrayList<SimpleClause>();
		dnf.add(sc);
	}
	
	public SimpleDNF(List<SimpleClause> dnf){
		this.dnf = dnf;
	}
	
	public SimpleClause get(int i) {
		return dnf.get(i);
	}

	public SimpleClause remove(int i) {
		return dnf.remove(i);
	}

	public boolean isFixed(){
		for(SimpleClause sc : dnf)
			if(!sc.isFixed())
				return false;
		return true;
	}
	
	public void join(SimpleDNF other){
		for(SimpleClause sc : other.dnf)
			this.add(sc);
	}
	
	public int getFirstUnfixedClauseIdx(){
		for(int i = 0;i < dnf.size(); i++){
			if(!dnf.get(i).isFixed()){
				return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * Break current DNF into 2 DNFs. Keep the first k disjuncts, and return the rest.
	 * @param k
	 * @return
	 */
	public SimpleDNF trancateTail(int k){
		if(k > dnf.size())
			return new SimpleDNF();
		List<SimpleClause> dnf1 = dnf.subList(k, dnf.size());
		dnf = dnf.subList(0, k);
		return new SimpleDNF(dnf1);
	}
	
	/**
	 * Break current DNF into 2 DNFs. Keep the last size()-k disjuncts, and return the first k.
	 * @param k
	 * @return
	 */
	public SimpleDNF trancateHead(int k){
		if(k > dnf.size())
			k = dnf.size();
		List<SimpleClause> dnf1 = dnf.subList(0,k);
		dnf = dnf.subList(k, dnf.size());
		return new SimpleDNF(dnf1);		
	}
	
	public int indexOf(SimpleClause sc) {
		return dnf.indexOf(sc);
	}

	public boolean add(SimpleClause sc) {
		if (sub_on) {
			ArraySet<Integer> scToRm = new ArraySet<Integer>();
			for (int i = 0; i < dnf.size(); i++) {
				SimpleClause sc1 = dnf.get(i);
				if (sc1.contains(sc)){
					System.out.println("Contain check work!");
					return false;
					}
				if (sc.contains(sc1)){
					System.out.println("Contain check work!");
					scToRm.add(i);
				}
			}
			for(int i = scToRm.size()-1; i >= 0; i--)
				this.remove(scToRm.get(i));
		}
		int j = 0;
		for(; j < dnf.size(); j++){
			SimpleClause sc1 = dnf.get(j);
			if(cmp.compare(sc1, sc) >= 0){
				if(!sub_on)
					if(sc.equals(sc1))
						return false;
				break;
			}
		}
		dnf.add(j, sc);
		return true;
	}

	public SimpleClause checkContain(SimpleClause sc){
		for (int i = 0; i < dnf.size(); i++) {
			SimpleClause sc1 = dnf.get(i);
			if (sc1.contains(sc)){
				System.out.println("Contain check work!");
				return null;
				}
		}
		return null;
	}
	
	public int size() {
		return dnf.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dnf == null) ? 0 : dnf.hashCode());
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
		SimpleDNF other = (SimpleDNF) obj;
		if (dnf == null) {
			if (other.dnf != null)
				return false;
		} else if (other.dnf == null)
			return false;
		return dnf.containsAll(other.dnf) && other.dnf.containsAll(dnf);
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		if(dnf.size() > 0)
			sb.append(dnf.get(0));
		for(int i = 1; i < dnf.size(); i++)
			sb.append("!!!!!\\/!!!!"+dnf.get(i).toString());
		sb.append("]");
		return sb.toString();
	}
	
}
