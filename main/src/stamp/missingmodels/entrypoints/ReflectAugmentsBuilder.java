package stamp.missingmodels.entrypoints;

import java.util.HashSet;
import java.util.Set;

import shord.program.Program;
import soot.SootClass;
import soot.SootMethod;

public class ReflectAugmentsBuilder extends EntryPointAugmentsBuilder {
	@Override
	public Set<SootMethod> getEntryPointAugments() {
		Set<SootMethod> reflectCalls = new HashSet<SootMethod>();
		for(SootClass klass : Program.g().getClasses()) {
			for(SootMethod method : klass.getMethods()) {
				if (!method.isConcrete() && !method.isAbstract())
					reflectCalls.add(method);
			}
		}
		return reflectCalls;
	}

	@Override
	public boolean isGenerated() {
		return true;
	}
}