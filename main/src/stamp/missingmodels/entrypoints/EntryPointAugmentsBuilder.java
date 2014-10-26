package stamp.missingmodels.entrypoints;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

public abstract class EntryPointAugmentsBuilder extends JavaAnalysis {
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
			for(SootMethod entryPointAugment : getAllEntryPointAugments()) {
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
	
	private static List<Class<? extends EntryPointAugmentsBuilder>> builders = new ArrayList<Class<? extends EntryPointAugmentsBuilder>>();
	static {
		//builders.add(PotentialCallbackAugmentsBuilder.class);
		builders.add(ReflectAugmentsBuilder.class);
	}
	
	private static Set<SootMethod> getAllEntryPointAugmentsPrivate(boolean useGenerated) {
		Set<SootMethod> allEntryPointAugments = new HashSet<SootMethod>();
		for(Class<? extends EntryPointAugmentsBuilder> builder : builders) {
			try {
				EntryPointAugmentsBuilder builderInstance = builder.newInstance();
				if(builderInstance.isGenerated()) {
					System.out.println("Adding augments for class: " + builder.toString());
					Set<SootMethod> entryPointAugments = builderInstance.getEntryPointAugments();
					for(SootMethod entryPointAugment : entryPointAugments) {
						System.out.println(entryPointAugment.toString());
					}
					allEntryPointAugments.addAll(entryPointAugments);
				} else {
					System.out.println("Skipping augments for class: " + builder.toString());
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return allEntryPointAugments;
	}
	
	public static Set<SootMethod> getAllEntryPointAugmentsIfGenerated() {
		return getAllEntryPointAugmentsPrivate(true);
	}
	
	public static Set<SootMethod> getAllEntryPointAugments() {
		return getAllEntryPointAugmentsPrivate(false);
	}
	
	public abstract Set<SootMethod> getEntryPointAugments();
	public abstract boolean isGenerated();
}
