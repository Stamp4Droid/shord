package stamp.srcmap.sourceinfo.abstractinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.srcmap.sourceinfo.ClassInfo;

/**
 * @author Saswat Anand 
 */
public abstract class AbstractClassInfo implements ClassInfo {
	private Map<String, BasicMethodInfo> methodInfos = new HashMap<String, BasicMethodInfo>();

	public static class BasicMethodInfo {
		private int lineNum = -1;
		private List<String> aliasChordSigs;
		
		public BasicMethodInfo(int lineNum) {
			this.lineNum = lineNum;
		}
		
		public int lineNum() {
			return this.lineNum;
		}
		
		public List<String> aliasChordSigs() {
			return this.aliasChordSigs();
		}
		
		public void addAliasChordSig(String chordSig) {
			if(aliasChordSigs == null)
				aliasChordSigs = new ArrayList<String>();
			aliasChordSigs.add(chordSig);
		}
	}
	
	public void addMethodInfo(String chordSig, BasicMethodInfo methodInfo) {
		this.methodInfos.put(chordSig, methodInfo);
	}
	
	public boolean hasMethodInfoFor(String chordSig) {
		return this.methodInfos.get(chordSig) != null;
	}

	@Override
	public int lineNum(String chordMethSig) { 
		BasicMethodInfo bmi = this.methodInfos.get(chordMethSig);
		return bmi == null ? -1 : bmi.lineNum;
	}
	
	@Override
	public List<String> aliasSigs(String chordMethSig) {
		List<String> ret = this.methodInfos.get(chordMethSig).aliasChordSigs;
		return ret == null ? Collections.EMPTY_LIST : ret;
	}

	@Override
	public Map<String,List<String>> allAliasSigs() {
		Map<String,List<String>> ret = new HashMap();
		for(Map.Entry<String,BasicMethodInfo> bmiEntry : this.methodInfos.entrySet()) {
			String chordSig = bmiEntry.getKey();
			List<String> aliases = bmiEntry.getValue().aliasChordSigs;
			if(aliases != null){
				if(ret == null)
					ret = new HashMap();
				ret.put(chordSig, aliases);
			}
		}
		return ret == null ? Collections.EMPTY_MAP : ret;
	}
}