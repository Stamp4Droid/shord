package chord.project.analyses.provenance.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chord.project.analyses.provenance.LookUpRule;
import chord.project.analyses.provenance.Tuple;

public class Solver {
	private Dictionary dictionary;
	private Tuple query;
	private SimpleDNF F;
	private SimpleDNF D;
	private Set<SimpleClause> B;
	private int k;

	public Solver(Tuple q, int k, List<LookUpRule> rules,boolean subOn, Set<String> paramRs, Set<Tuple> internalTuples) {
		this.query = q;
		this.k = k;
		SimpleDNF.sub_on = subOn;
		SimpleDNF.controllableRs = new HashSet<String>();
		SimpleDNF.parameterRs = paramRs;
		SimpleDNF.internalTuples = internalTuples;
		dictionary = new Dictionary(rules,true);
		F = new SimpleDNF(q);
		D = new SimpleDNF();
		B = new HashSet<SimpleClause>();
	}

	public void run() {
		while(!F.isFixed()){
			System.out.println("F size: "+F.size()+", D size: "+D.size()+", B size: "+B.size());
			System.out.println(F);
			int idx = F.getFirstUnfixedClauseIdx();
			SimpleClause sc = F.remove(idx);
			B.add(sc);
			List<SimpleClause> expanded = sc.expandFirstT(dictionary);
			for(SimpleClause sc1 : expanded){
				if(!B.contains(sc1))
					F.add(sc1);
				else
					System.out.println("Cycle detected");
			}
			if(F.size() > k){
				SimpleDNF dnf = F.trancateTail(k);
				System.out.println("Drop: "+dnf);
				D.join(dnf);
			}
			if(F.size() < k){
				SimpleDNF dnf = D.trancateHead(k-F.size());
				System.out.println("Pull back: "+dnf);
				F.join(dnf);
			}
		}
	}
	
	public SimpleDNF getF(){
		return F;
	}

}
