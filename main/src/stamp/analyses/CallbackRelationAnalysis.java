package stamp.analyses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shord.analyses.DomI;
import shord.program.Program;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.util.NumberedString;
import soot.util.Switch;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.PrintingUtils;
import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;
import chord.project.Chord;

/**
 * Copied from stamp.reporting.PotentialCallbacks
 * @author obastani
 *
 */
@Chord(name = "potential-callback-java",
consumes = { "M", "I" },
produces = { "potentialCallback", "potentialCallbackIM" },
namesOfTypes = {},
types = {},
namesOfSigns = { "potentialCallback", "potentialCallbackIM" },
signs = { "M0:M0", "I0,M0:M0_I0" })
public class CallbackRelationAnalysis extends JavaAnalysis {
	private MultivalueMap<SootMethod,SootMethod> frameworkMethodsToCallbacks = new MultivalueMap<SootMethod,SootMethod>();
	private Set<String> invocationTargetSignatures = new HashSet<String>();
	
	private static boolean isInteresting(SootMethod method) {
		return method.isConcrete() && !method.isPrivate() && !method.isStatic() && !method.getName().equals("<init>");
	}
	
	private static boolean canBeOverridden(SootMethod method) {
		return !Modifier.isFinal(method.getModifiers()) && !method.isPrivate() && !method.isStatic() && !method.getName().equals("<init>");
	}
	
	private void findCallbacksHelper(SootClass klass, Map<NumberedString,SootMethod> signaturesToMethods) {
		if(AbstractSourceInfo.isFrameworkClass(klass)) {
			if(!klass.getName().equals("java.lang.Object")) {
				for(SootMethod superMethod : klass.getMethods()) {
					if(canBeOverridden(superMethod)) {
						NumberedString superMethodSignature = superMethod.getNumberedSubSignature();
						SootMethod method = signaturesToMethods.get(superMethodSignature);
						if(method != null && !this.invocationTargetSignatures.contains(method.toString())) {
							this.frameworkMethodsToCallbacks.add(superMethod, method);
						}
					}
				}
			}
		}
		this.findCallbacks(klass, signaturesToMethods);
	}
	
	private void findCallbacks(SootClass klass, Map<NumberedString,SootMethod> signaturesToMethods) {
		if(klass.hasSuperclass()) {		
			this.findCallbacksHelper(klass.getSuperclass(), signaturesToMethods);
		}
		for(SootClass iface : klass.getInterfaces()) {
			this.findCallbacksHelper(iface, signaturesToMethods);
		}
	}
	
	private void processClass(SootClass klass) {
		if(AbstractSourceInfo.isFrameworkClass(klass)) {
			return;
		}
		
		Map<NumberedString,SootMethod> signaturesToMethods = new HashMap<NumberedString,SootMethod>();
		for(SootMethod method : klass.getMethods()) {
			if(isInteresting(method)) {
				NumberedString signature = method.getNumberedSubSignature();
				signaturesToMethods.put(signature, method);
			}
		}
		
		findCallbacks(klass, signaturesToMethods);
	}
	
	/*
	 * Copied from code to produce chaIM
	 */
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
		
		for(SootClass klass : Program.g().getClasses()) {
			processClass(klass);
		}
		
		DomI domI = (DomI)ClassicProject.g().getTrgt("I");
		Map<SootMethod,Unit> unitsByMethod = new HashMap<SootMethod,Unit>();
		for(SootMethod superMethod : this.frameworkMethodsToCallbacks.keySet()) {
			for(SootMethod method : this.frameworkMethodsToCallbacks.get(superMethod)) {
				Unit mockUnit = new MockUnit(method);
				unitsByMethod.put(method, mockUnit);
				domI.add(mockUnit);
			}
		}
		domI.save();
		
		ProgramRel relPotentialCallback = (ProgramRel)ClassicProject.g().getTrgt("potentialCallback");
		ProgramRel relPotentialCallbackIM = (ProgramRel)ClassicProject.g().getTrgt("potentialCallbackIM");
		relPotentialCallback.zero();
		relPotentialCallbackIM.zero();
		for(SootMethod superMethod : this.frameworkMethodsToCallbacks.keySet()) {
			for(SootMethod method : this.frameworkMethodsToCallbacks.get(superMethod)) {
				relPotentialCallback.add(method);
				relPotentialCallbackIM.add(unitsByMethod.get(method), method);
			}
		}
		relPotentialCallback.save();
		relPotentialCallbackIM.save();
		
		PrintingUtils.printRelation("potentialCallback");
		PrintingUtils.printRelation("potentialCallbackIM");
	}
	
	public static class MockUnit implements Unit {
		public final SootMethod target;
		public MockUnit(SootMethod target) {
			this.target = target;
		}
		@Override
		public String toString() {
			return this.target.toString();
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
