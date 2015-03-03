package stamp.missingmodels.util.cflsolver.relation;

import shord.analyses.DomU;
import shord.analyses.DomV;
import shord.project.ClassicProject;
import stamp.missingmodels.util.cflsolver.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.relation.TaintRelationManager.TaintPointsToRelationManager;

public class CallbackRelationManager extends TaintPointsToRelationManager {
	private void setNewWeights() {
		this.clearRelationsByName("param");
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param", null, (short)1));
		
		this.clearRelationsByName("paramPrim");
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim", null, (short)1));
	}
	
	public CallbackRelationManager(final MultivalueMap<String,String> trueParam, final MultivalueMap<String,String> trueParamPrim) {
		this.setNewWeights();
		
		final DomV domV = (DomV)ClassicProject.g().getTrgt("V");
		final DomU domU = (DomU)ClassicProject.g().getTrgt("U");
		
		this.add(new IndexRelation("param", "V", 1, "V", 0, "param") {
			@Override
			public boolean filter(int[] tuple) {
				String source = domV.get(Integer.parseInt(this.getSource(tuple).substring(1))).toString();
				String sink = domV.get(Integer.parseInt(this.getSink(tuple).substring(1))).toString();
				return trueParam.get(source).contains(sink);
			}
		});
		this.add(new IndexRelation("paramPrim", "U", 1, "U", 0, "paramPrim") {
			@Override
			public boolean filter(int[] tuple) {
				String source = domU.get(Integer.parseInt(this.getSource(tuple).substring(1))).toString();
				String sink = domU.get(Integer.parseInt(this.getSink(tuple).substring(1))).toString();
				return trueParamPrim.get(source).contains(sink);
			}
		});
		
	}
}
