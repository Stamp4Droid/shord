package stamp.missingmodels.util.cflsolver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.core.Util.Pair;

public class CallgraphUtils {
	public static class Callgraph {
		private final List<Pair<String,String>> callgraph = new ArrayList<Pair<String,String>>();
		
		public Set<String> getMethodSet() {
			throw new RuntimeException("Not implemented!");
		}
		
		// ordered by callee
		// each method occurs once
		public List<String> getUniqueMethodList() {
			throw new RuntimeException("Not implemented!");
		}
		
		public MultivalueMap<String,String> getCallgraph() {
			throw new RuntimeException("Not implemented!");
		}
		
		public List<Pair<String,String>> getOrderedCallgraph() {
			throw new RuntimeException("Not implemented!");
		}
		
		public void add(String caller, String callee) {
			this.callgraph.add(new Pair<String,String>(caller,callee));
		}
	}
}
