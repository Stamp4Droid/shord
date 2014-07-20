package chord.project.analyses.provenance.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import chord.project.analyses.provenance.ConstraintItem;
import chord.project.analyses.provenance.LookUpRule;
import chord.project.analyses.provenance.Tuple;

public class Dictionary {
	private Map<String, List<LookUpRule>> dic = new HashMap<String, List<LookUpRule>>();
	private Map<Tuple, List<ConstraintItem>> index;

	public Dictionary(List<LookUpRule> rules, boolean buildIndex) {
		for (LookUpRule r : rules) {
			String headName = r.getHeadRelName();
			SimpleDNF.controllableRs.add(headName);
			List<LookUpRule> rList = dic.get(headName);
			if (rList == null) {
				rList = new ArrayList<LookUpRule>();
				dic.put(headName, rList);
			}
			rList.add(r);
		}
		if (buildIndex) {
			index = new HashMap<Tuple,List<ConstraintItem>>();
			for(LookUpRule r: rules){
				Iterator<ConstraintItem> iter = r.getAllConstrIterator();
				while(iter.hasNext()){
					ConstraintItem ci = iter.next();
					Tuple ht = ci.headTuple;
					List<ConstraintItem> ciList = index.get(ht);
					if(ciList == null){
						ciList = new ArrayList<ConstraintItem>();
						index.put(ht, ciList);
					}
					ciList.add(ci);
				}
			}
		}
	}
	
	public List<ConstraintItem> getConstraintItems(Tuple t){
		if(index != null)
			return index.get(t);
		List<ConstraintItem> ret = new ArrayList<ConstraintItem>();
		List<LookUpRule> rules = dic.get(t.getRelName());
		for (LookUpRule r : rules) {
			Iterator<ConstraintItem> iter = r.getConstrIterForTuple(t);
			while (iter.hasNext()) {
				ConstraintItem ci = iter.next();
				ret.add(ci);
			}
		}
		return ret;
	}

}
