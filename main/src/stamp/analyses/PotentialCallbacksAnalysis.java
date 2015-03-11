package stamp.analyses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import soot.UnitBox;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.util.Switch;
import stamp.missingmodels.callgraph.PotentialCallbacksBuilder;
import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
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
		for(SootMethod potentialCallback : PotentialCallbacksBuilder.getPotentialCallbacks()) {
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
	
	public static class MockUnit implements Unit {
		public final SootMethod target;
		public MockUnit(SootMethod target) {
			this.target = target;
		}
		@Override
		public String toString() {
			return "MockCall(" + this.target.toString() + ")";
		}
		
		private static final long serialVersionUID = 1L;
		@Override
		public Unit clone() {
			return null;
		}
		@Override
		public void apply(Switch arg0) {}
		@Override
		public void addAllTagsOf(Host arg0) {}
		@Override
		public void addTag(Tag arg0) {}
		@Override
		public int getJavaSourceStartColumnNumber() {
			return 0;
		}
		@Override
		public int getJavaSourceStartLineNumber() {
			return 0;
		}
		@Override
		public Tag getTag(String arg0) {
			return null;
		}
		@Override
		public List<Tag> getTags() {
			return null;
		}
		@Override
		public boolean hasTag(String arg0) {
			return false;
		}
		@Override
		public void removeAllTags() {}
		@Override
		public void removeTag(String arg0) {}
		@Override
		public void addBoxPointingToThis(UnitBox arg0) {}
		@Override
		public boolean branches() {
			return false;
		}
		@Override
		public void clearUnitBoxes() {}
		@Override
		public boolean fallsThrough() {
			return false;
		}
		@Override
		public List<UnitBox> getBoxesPointingToThis() {
			return null;
		}
		@Override
		public List<ValueBox> getDefBoxes() {
			return null;
		}
		@Override
		public List<UnitBox> getUnitBoxes() {
			return null;
		}
		@Override
		public List<ValueBox> getUseAndDefBoxes() {
			return null;
		}
		@Override
		public List<ValueBox> getUseBoxes() {
			return null;
		}
		@Override
		public void redirectJumpsToThisTo(Unit arg0) {}
		@Override
		public void removeBoxPointingToThis(UnitBox arg0) {}
		@Override
		public void toString(UnitPrinter arg0) {}
	}
}
