package stamp.missingmodels.callgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import shord.program.Program;
import shord.project.analyses.JavaAnalysis;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.util.NumberedString;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.util.cflsolver.util.IOUtils;
import stamp.srcmap.sourceinfo.abstractinfo.AbstractSourceInfo;

/**
 * Copied from stamp.reporting.PotentialCallbacks
 * @author obastani
 *
 */
public class EntryPointAugmentsBuilder extends JavaAnalysis {
	public static Set<SootMethod> getEntryPointAugmentsFromFile() {
		Map<String,SootMethod> methodsBySignature = new HashMap<String,SootMethod>();
		Iterator<SootMethod> methodIter = Program.g().getMethods();
		while(methodIter.hasNext()) {
			SootMethod method = methodIter.next();
			methodsBySignature.put(method.toString(), method);
		}		
		try {
			Set<SootMethod> entryPointAugments = new HashSet<SootMethod>();
			BufferedReader br = new BufferedReader(new FileReader(getEntryPointAugmentsFile()));
			String line;
			while((line = br.readLine()) != null) {
				SootMethod method = methodsBySignature.get(line);
				if(method == null) {
					System.out.println("UNRECOGNIZED METHOD SIGNATURE: " + line);
				}
				entryPointAugments.add(method);
			}
			br.close();
			return entryPointAugments;
		} catch(IOException e) {
			e.printStackTrace();
			return new HashSet<SootMethod>();
		}
	}
	
	public static void writeEntryPointAugmentSignaturesToFile() {
		try {
			PrintWriter pw = new PrintWriter(getEntryPointAugmentsFile());
			for(SootMethod entryPointAugment : getNewEntryPointAugments()) {
				pw.println(entryPointAugment.toString());
			}
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static File getEntryPointAugmentsFile() {
		return new File(IOUtils.getAppOutputDirectory(), "entryPointAugments.txt"); 
	}
	
	public static Set<SootMethod> getEntryPointAugments() {
		if (IOUtils.graphEdgesFileExists("param", "graph")) {
			System.out.println("Param file found, adding potential callbacks");
			return getNewEntryPointAugments();
		} else {
			System.out.println("Param file not yet generated; ignoring potential callbacks!");
			return new HashSet<SootMethod>();
		}
		
	}
	
	public static Set<SootMethod> getNewEntryPointAugments() {
		EntryPointAugmentsBuilder callgraphAugmentsBuilder = new EntryPointAugmentsBuilder();
		for(SootClass klass : Program.g().getClasses()) {
			callgraphAugmentsBuilder.processClass(klass);
		}
		Set<SootMethod> potentialCallbacks = new HashSet<SootMethod>();
		for(SootMethod superMethod : callgraphAugmentsBuilder.frameworkMethodsToCallbacks.keySet()) {
			System.out.println("Processing potential callbacks for: " + superMethod.toString());
			for(SootMethod potentialCallback : callgraphAugmentsBuilder.frameworkMethodsToCallbacks.get(superMethod)) {
				System.out.println("Adding potential callback: " + potentialCallback.toString());
				potentialCallbacks.add(potentialCallback);
			}
		}
		return potentialCallbacks;
		/*
		Set<SootMethod> potentialCallbacks = new HashSet<SootMethod>();
		for(SootClass klass : Program.g().getClasses()) {
			for(SootMethod method : klass.getMethods()) {
				if (!method.isConcrete() && !method.isAbstract())
					potentialCallbacks.add(method);
			}
		}
		return potentialCallbacks;
		*/
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
