package stamp.reporting;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import shord.analyses.Ctxt;
import shord.analyses.VarNode;

import chord.util.tuple.object.Trio;
import chord.util.tuple.object.Pair;

/*
 * @author Saswat Anand
**/
public class FlowCluster
{
	private static final Map<Pair<String,Ctxt>,Set<Pair<VarNode,Ctxt>>> labelToBucket = new HashMap();
	private static final Map<Pair<Pair<String,Ctxt>,Pair<String,Ctxt>>,Set<Pair<VarNode,Ctxt>>> flowToBucket = new HashMap();

	static void cluster()
	{
		System.out.println("Begin clustering");
		fillLabelBuckets();
		fillFlowBuckets();
		
		Object[] flows = flowToBucket.keySet().toArray();
		int numFlows = flows.length;
		for(int i = 0; i < numFlows; i++){
			Pair<Pair<String,Ctxt>,Pair<String,Ctxt>> f1 = (Pair<Pair<String,Ctxt>,Pair<String,Ctxt>>) flows[i];
			for(int j = i+1; j < numFlows; j++){
				Pair<Pair<String,Ctxt>,Pair<String,Ctxt>> f2 = (Pair<Pair<String,Ctxt>,Pair<String,Ctxt>>) flows[j];
				double ochiaiIndex = ochiai(flowToBucket.get(f1), flowToBucket.get(f2));
				System.out.println("ochiai("+i+", "+j+") = "+ ochiaiIndex);
			}
		}
		System.out.println("End clustering");
	}

	static double ochiai(Set<Pair<VarNode,Ctxt>> set1, Set<Pair<VarNode,Ctxt>> set2)
	{
		Set<Pair<VarNode,Ctxt>> intersection = new HashSet<Pair<VarNode,Ctxt>>(set1);
		intersection.retainAll(set2);
		
		double index = intersection.size() / Math.sqrt(set1.size()*set2.size());
		return index;
	}

	static void fillFlowBuckets()
	{
		final ProgramRel relCtxtFlows = (ProgramRel)ClassicProject.g().getTrgt("flow");
		relCtxtFlows.load();

		Iterable<Pair<Pair<String,Ctxt>,Pair<String,Ctxt>>> res = relCtxtFlows.getAry2ValTuples();
		int count = 0;
		for(Pair<Pair<String,Ctxt>,Pair<String,Ctxt>> pair : res) {
			Pair<String,Ctxt> srcLabel = pair.val0;
			Pair<String,Ctxt> sinkLabel = pair.val1;
			
			Pair<Pair<String,Ctxt>,Pair<String,Ctxt>> flow = new Pair<Pair<String,Ctxt>,Pair<String,Ctxt>>(srcLabel, sinkLabel);
			
			Set<Pair<VarNode,Ctxt>> bucket = flowToBucket.get(flow);
			if(bucket == null){
				bucket = new HashSet();
				flowToBucket.put(flow, bucket);
			}
			
			for(Pair<VarNode,Ctxt> v : getBucketForLabel(srcLabel)){
				bucket.add(v);
			}
			for(Pair<VarNode,Ctxt> v : getBucketForLabel(sinkLabel)){
				bucket.add(v);
			}			
		}
	}

	static void fillLabelBuckets()
	{
		final ProgramRel relRef = (ProgramRel) ClassicProject.g().getTrgt("labelRef");
		relRef.load();
		Iterable<Trio<Ctxt,VarNode,Pair<String,Ctxt>>> res1 = relRef.getAry3ValTuples();
		fillBuckets(res1);
		relRef.close();

		final ProgramRel relPrim = (ProgramRel) ClassicProject.g().getTrgt("labelPrim");
		relPrim.load();
		Iterable<Trio<Ctxt,VarNode,Pair<String,Ctxt>>> res2 = relPrim.getAry3ValTuples();
		fillBuckets(res2);
		relPrim.close();
	}

	private static void fillBuckets(Iterable<Trio<Ctxt,VarNode,Pair<String,Ctxt>>> iter)
	{
		for(Trio<Ctxt,VarNode,Pair<String,Ctxt>> trio : iter) {
			Ctxt varCtxt = trio.val0;
			VarNode var = trio.val1;
			Pair<String,Ctxt> label = trio.val2;
			
			Set<Pair<VarNode,Ctxt>> bucket = getBucketForLabel(label);
			bucket.add(new Pair<VarNode,Ctxt>(var, varCtxt));
		}
	}
	
	private static Set<Pair<VarNode,Ctxt>> getBucketForLabel(Pair<String,Ctxt> label)
	{
		Set<Pair<VarNode,Ctxt>> bucket = labelToBucket.get(label);
		if(bucket == null){
			bucket = new HashSet();
			labelToBucket.put(label, bucket);
		}
		return bucket;
	}
		
		
}