package stamp.missingmodels.entrypoints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import shord.program.Program;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.util.NumberedString;
import stamp.missingmodels.util.cflsolver.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;

public class PotentialCallbackAugmentsBuilder extends EntryPointAugmentsBuilder {
	@Override
	public Set<SootMethod> getEntryPointAugments() {
		for(SootClass klass : Program.g().getClasses()) {
			this.processClass(klass);
		}
		Set<SootMethod> potentialCallbacks = new HashSet<SootMethod>();
		for(SootMethod superMethod : this.frameworkMethodsToCallbacks.keySet()) {
			System.out.println("Processing potential callbacks for: " + superMethod.toString());
			for(SootMethod potentialCallback : this.frameworkMethodsToCallbacks.get(superMethod)) {
				System.out.println("Adding potential callback: " + potentialCallback.toString());
				potentialCallbacks.add(potentialCallback);
			}
		}
		return potentialCallbacks;
	}

	@Override
	public boolean isGenerated() {
		return IOUtils.graphEdgesFileExists("param", "graph");
	}

	
	private static boolean isInteresting(SootMethod method) {
		return method.isConcrete() && !method.isPrivate() && !method.isStatic() && !method.getName().equals("<init>");
	}
	
	private static boolean canBeOverridden(SootMethod method) {
		return !Modifier.isFinal(method.getModifiers()) && !method.isPrivate() && !method.isStatic() && !method.getName().equals("<init>");
	}

	private MultivalueMap<SootMethod,SootMethod> frameworkMethodsToCallbacks = new MultivalueMap<SootMethod,SootMethod>();
	
	private void findCallbacksHelper(SootClass klass, Map<NumberedString,SootMethod> signaturesToMethods) {
		if(AbstractSourceInfo.isFrameworkClass(klass)) {
			if(!klass.getName().equals("java.lang.Object")) {
				for(SootMethod superMethod : klass.getMethods()) {
					if(canBeOverridden(superMethod)) {
						NumberedString superMethodSignature = superMethod.getNumberedSubSignature();
						SootMethod method = signaturesToMethods.get(superMethodSignature);
						if(method != null) {
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
}
