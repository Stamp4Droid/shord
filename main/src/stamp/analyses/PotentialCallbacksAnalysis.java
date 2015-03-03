package stamp.analyses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import shord.analyses.DomI;
import shord.analyses.DomM;
import shord.program.Program;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;
import stamp.missingmodels.entrypoints.EntryPointAugmenter.MockUnit;
import stamp.missingmodels.entrypoints.EntryPointAugmentsBuilder;
import stamp.missingmodels.util.cflsolver.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import chord.project.Chord;

/**
 * 
 * Copied from stamp.reporting.PotentialCallbacks
 * @author obastani
 *
 */
@Chord(name = "potential-callbacks",
consumes = { "M", "I" },
produces = { "potentialCallback", "potentialCallbackIM" },
namesOfTypes = {},
types = {},
namesOfSigns = { "potentialCallback", "potentialCallbackIM" },
signs = { "M0:M0", "I0,M0:M0_I0" })
public class PotentialCallbacksAnalysis extends JavaAnalysis {
	private Set<String> invocationTargetSignatures = new HashSet<String>();
	
	private MultivalueMap<SootMethod,SootMethod> findCallgraph() {
		MultivalueMap<SootMethod,SootMethod> callgraph = new MultivalueMap<SootMethod,SootMethod>();
		Iterator<Edge> edgeIter = Program.g().scene().getCallGraph().listener();
		while(edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			if(!edge.isExplicit() && !edge.isThreadRunCall()) {
				continue;
			}
			SootMethod source = (SootMethod)edge.src();
			SootMethod target = (SootMethod)edge.tgt();

			if(target.isAbstract()) {
				assert false : "tgt = "+target +" "+target.isAbstract();
			}
			if(target.isPhantom()) {
				continue;
			}
			callgraph.add(source, target);
		}
		return callgraph;
	}
	
	
	@Override
	public void run() {
		MultivalueMap<SootMethod,SootMethod> callgraph = this.findCallgraph();
		for(SootMethod source : callgraph.keySet()) {
			for(SootMethod target : callgraph.get(source)) {
				this.invocationTargetSignatures.add(target.toString());
			}
		}
		
		Set<SootMethod> potentialCallbacks = new HashSet<SootMethod>();
		for(SootMethod potentialCallback : EntryPointAugmentsBuilder.getAllEntryPointAugments()) {
			if(!this.invocationTargetSignatures.contains(potentialCallback.toString())) {
				potentialCallbacks.add(potentialCallback);
			}
		}
		
		DomI domI = (DomI)ClassicProject.g().getTrgt("I");
		Map<SootMethod,Unit> unitsByMethod = new HashMap<SootMethod,Unit>();
		for(SootMethod potentialCallback : potentialCallbacks) {
			Unit mockUnit = new MockUnit(potentialCallback);
			unitsByMethod.put(potentialCallback, mockUnit);
			domI.add(mockUnit);
		}
		domI.save();
		
		DomM domM = (DomM)ClassicProject.g().getTrgt("M");
		SootMethod stampHarness = domM.get(0);
		
		ProgramRel relPotentialCallback = (ProgramRel)ClassicProject.g().getTrgt("potentialCallback");
		ProgramRel relPotentialCallbackIM = (ProgramRel)ClassicProject.g().getTrgt("potentialCallbackIM");
		ProgramRel relChaIM = (ProgramRel)ClassicProject.g().getTrgt("chaIM");
		ProgramRel relStatIM = (ProgramRel)ClassicProject.g().getTrgt("StatIM");
		ProgramRel relMI = (ProgramRel)ClassicProject.g().getTrgt("MI");
		relPotentialCallback.zero();
		relPotentialCallbackIM.zero();
		relChaIM.load();
		relStatIM.load();
		relMI.load();
		for(SootMethod potentialCallback : potentialCallbacks) {
			relPotentialCallback.add(potentialCallback);
			Unit potentialCallbackI = unitsByMethod.get(potentialCallback);
			relPotentialCallbackIM.add(potentialCallbackI, potentialCallback);
			relChaIM.add(potentialCallbackI, potentialCallback);
			relStatIM.add(potentialCallbackI, potentialCallback);
			relMI.add(stampHarness, potentialCallbackI);
		}
		relPotentialCallback.save();
		relPotentialCallbackIM.save();
		relChaIM.save();
		relStatIM.save();
		relMI.save();
		
		IOUtils.printRelation("potentialCallback");
		IOUtils.printRelation("potentialCallbackIM");
	}
}
