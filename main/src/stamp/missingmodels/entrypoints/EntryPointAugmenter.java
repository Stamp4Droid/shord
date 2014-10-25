package stamp.missingmodels.entrypoints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shord.analyses.DomI;
import shord.analyses.DomM;
import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.util.Switch;

/**
 * 
 * Copied from stamp.reporting.PotentialCallbacks
 * @author obastani
 *
 */
public class EntryPointAugmenter {	
	private final Map<SootMethod,Unit> unitsByMethod = new HashMap<SootMethod,Unit>();

	public EntryPointAugmenter() {		
		for(SootMethod entryPointAugment : EntryPointAugmentsBuilder.getAllEntryPointAugmentsIfGenerated()) {
			Unit mockUnit = new MockUnit(entryPointAugment);
			this.unitsByMethod.put(entryPointAugment, mockUnit);
		}
	}

	public void addDomI(DomI domI) {
		for(Unit unit : this.unitsByMethod.values()) {
			domI.add(unit);
		}
	}
	
	public void addRelM(ProgramRel relM) {
		for(SootMethod entryPointAugment : this.unitsByMethod.keySet()) {
			relM.add(entryPointAugment);
		}
	}

	public void addRelIM(ProgramRel relIM) {
		for(Map.Entry<SootMethod,Unit> entry : this.unitsByMethod.entrySet()) {
			System.out.println("ADDING CALL EDGE: " + entry.getValue().toString() + " -> " + entry.getKey().toString());
			relIM.add(entry.getValue(), entry.getKey());
		}
	}

	public void addRelMI(ProgramRel relMI) {
		DomM domM = (DomM)ClassicProject.g().getTrgt("M");
		SootMethod stampHarness = Scene.v().getMainMethod();
		for(Unit unit : this.unitsByMethod.values()) {
			relMI.add(stampHarness, unit);
		}		
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
		
		@Override
		public MockUnit clone() {
			return new MockUnit(this.target);
		}

		private static final long serialVersionUID = 5751889841030513201L;
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
