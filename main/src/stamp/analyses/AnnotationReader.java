package stamp.analyses;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.FastHierarchy;
import soot.util.NumberedString;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.analyses.JavaAnalysis;
import shord.project.ClassicProject;
import shord.program.Program;
import shord.analyses.DomM;

import chord.project.Chord;

/**
 * @author Saswat Anand
**/
@Chord(name = "annot-java",
	   consumes = { "M", "Z" },
	   produces = { "SRC", "SINK", 
					"ArgArgFlow", "ArgRetFlow", 
					"ArgSinkFlow", "RetSinkFlow",
					"SrcArgFlow", "SrcRetFlow" },
	   namesOfTypes = { "SRC", "SINK" },
	   types = { DomSRC.class, DomSINK.class },
	   namesOfSigns = { "ArgArgFlow", "ArgRetFlow", 
						"ArgSinkFlow", "RetSinkFlow",
						"SrcArgFlow", "SrcRetFlow" },
	   signs = { "M0,Z0,Z1:M0_Z0_Z1", "M0,Z0:M0_Z0", 
				 "M0,Z0,SINK0:SINK0_M0_Z0", "M0,SINK0:SINK0_M0",
				 "SRC0,M0,Z0:SRC0_M0_Z0", "SRC0,M0:SRC0_M0" }
	   )
public class AnnotationReader extends JavaAnalysis
{
	private ProgramRel relArgArgFlow;
	private ProgramRel relArgRetFlow;
	private ProgramRel relArgSinkFlow; 
	private ProgramRel relRetSinkFlow;
	private ProgramRel relSrcArgFlow;
	private ProgramRel relSrcRetFlow;

	private HashMap<SootClass,List<SootClass>> classToSubtypes = new HashMap();

	public void run()
	{
		DomSRC domSRC = (DomSRC) ClassicProject.g().getTrgt("SRC");
		DomSINK domSINK = (DomSINK) ClassicProject.g().getTrgt("SINK");
		DomM domM = (DomM) ClassicProject.g().getTrgt("M");

		relArgArgFlow = (ProgramRel) ClassicProject.g().getTrgt("ArgArgFlow");
		relArgRetFlow = (ProgramRel) ClassicProject.g().getTrgt("ArgRetFlow");

		relArgArgFlow.zero();
		relArgRetFlow.zero();
		
		Scene scene = Program.g().scene();
		List worklist = new LinkedList();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File("stamp_annotations.txt")));
			String line = reader.readLine();
			while(line != null){
				final String[] tokens = line.split(" ");
				String chordMethodSig = tokens[0];
				int atSymbolIndex = chordMethodSig.indexOf('@');
				String className = chordMethodSig.substring(atSymbolIndex+1);
				if(scene.containsClass(className)){
					SootClass klass = scene.getSootClass(className);
					String subsig = getSootSubsigFor(chordMethodSig.substring(0,atSymbolIndex));
					SootMethod meth = klass.getMethod(subsig);
					
					if(domM.indexOf(meth) >= 0){
						String from = tokens[1];
						String to = tokens[2];
						
						boolean src = from.charAt(0) == '$';
						boolean sink = to.charAt(0) == '!';
						if(src && sink){
							System.out.println("Unsupported annotation type "+line);
						} else if(src || sink){
							if(src)
								domSRC.add(from);
							if(sink)
								domSINK.add(to);
							
							worklist.add(meth);
							worklist.add(from);
							worklist.add(to);
						} else {
							addFlow(meth, from, to);
						}
					}
				}
				line = reader.readLine();
			}
			reader.close();
		}catch(IOException e){
			throw new Error(e);
		}
		domSRC.save();
		domSINK.save();

		relArgArgFlow.save();	
		relArgRetFlow.save();

		relArgSinkFlow = (ProgramRel) ClassicProject.g().getTrgt("ArgSinkFlow");
		relRetSinkFlow = (ProgramRel) ClassicProject.g().getTrgt("RetSinkFlow");
		relSrcArgFlow = (ProgramRel) ClassicProject.g().getTrgt("SrcArgFlow");
		relSrcRetFlow = (ProgramRel) ClassicProject.g().getTrgt("SrcRetFlow");

		relArgSinkFlow.zero();
		relRetSinkFlow.zero();
		relSrcArgFlow.zero();
		relSrcRetFlow.zero();
		
		while(!worklist.isEmpty()){
			SootMethod meth = (SootMethod) worklist.remove(0);
			String from = (String) worklist.remove(0);
			String to = (String) worklist.remove(0);
			addFlow(meth, from, to);
		}

		relArgSinkFlow.save();
		relRetSinkFlow.save();
		relSrcArgFlow.save();
		relSrcRetFlow.save();
	}

	private void addFlow(SootMethod meth, String from, String to) //throws NumberFormatException
	{
		System.out.println("+++ " + meth + " " + from + " " + to);
		List<SootMethod> meths = overridingMethodsFor(meth);
		if(from.charAt(0) == '$') {
			if(to.equals("-1")){
				for(SootMethod m : meths)
					relSrcRetFlow.add(from, m);
			}
			else{
				for(SootMethod m : meths)
					relSrcArgFlow.add(from, m, Integer.valueOf(to));
			}
		} else {
			assert !(from.charAt(0) == '!');
			Integer fromArgIndex = Integer.valueOf(from);
			if(to.charAt(0) == '!'){
				if(from.equals("-1")){
					for(SootMethod m : meths)
						relRetSinkFlow.add(m, to);
				} else{
					for(SootMethod m : meths)
						relArgSinkFlow.add(m, fromArgIndex, to);
				}
			} else if(to.equals("-1")){
				for(SootMethod m : meths)
					relArgRetFlow.add(m, fromArgIndex);
			} else {
				Integer toArgIndex = Integer.valueOf(to);
				for(SootMethod m : meths)
					relArgArgFlow.add(m, fromArgIndex, toArgIndex);
			}
		}
	}

	List<SootMethod> overridingMethodsFor(SootMethod originalMethod)
	{
		List<SootClass> subTypes = subTypesOf(originalMethod.getDeclaringClass());
		List<SootMethod> overridingMeths = new ArrayList();
		NumberedString subsig = originalMethod.getNumberedSubSignature();
		for(SootClass st : subTypes){
			if(st.declaresMethod(subsig)){
				overridingMeths.add(st.getMethod(subsig));
			}
		}
		return overridingMeths;
	}

	List<SootClass> subTypesOf(SootClass cl)
	{
		List<SootClass> subTypes = classToSubtypes.get(cl);
		if(subTypes != null) 
			return subTypes;
		
		classToSubtypes.put(cl, subTypes = new ArrayList());

		subTypes.add(cl);

		LinkedList<SootClass> worklist = new LinkedList<SootClass>();
		HashSet<SootClass> workset = new HashSet<SootClass>();
		FastHierarchy fh = Program.g().scene().getOrMakeFastHierarchy();

		if(workset.add(cl)) worklist.add(cl);
		while(!worklist.isEmpty()) {
			cl = worklist.removeFirst();
			if(cl.isInterface()) {
				for(Iterator cIt = fh.getAllImplementersOfInterface(cl).iterator(); cIt.hasNext();) {
					final SootClass c = (SootClass) cIt.next();
					if(workset.add(c)) worklist.add(c);
				}
			} else {
				if(cl.isConcrete()) {
					subTypes.add(cl);
				}
				for(Iterator cIt = fh.getSubclassesOf(cl).iterator(); cIt.hasNext();) {
					final SootClass c = (SootClass) cIt.next();
					if(workset.add(c)) worklist.add(c);
				}
			}
		}
		return subTypes;
	}
	
	private String getSootSubsigFor(String chordSubsig)
	{
		String name = chordSubsig.substring(0, chordSubsig.indexOf(':'));
		String retType = chordSubsig.substring(chordSubsig.indexOf(')')+1);
		String paramTypes = chordSubsig.substring(chordSubsig.indexOf('(')+1, chordSubsig.indexOf(')'));
		return parseDesc(retType) + " " + name + "(" + parseDesc(paramTypes) + ")";
	}

	static String parseDesc(String desc) 
	{
		StringBuilder params = new StringBuilder();
		String param = null;
		char c;
		int arraylevel=0;
		boolean didone = false;

		int len = desc.length();
		for (int i=0; i < len; i++) {
			c = desc.charAt(i);
			if (c =='B') {
				param = "byte";
			} else if (c =='C') {
				param = "char";
			} else if (c == 'D') {
				param = "double";
			} else if (c == 'F') {
				param = "float";
			} else if (c == 'I') {
				param = "int";
			} else if (c == 'J') {
				param = "long";
			} else if (c == 'S') {
				param = "short";
			} else if (c == 'Z') {
				param = "boolean";
			} else if (c == 'V') {
				param = "void";
			} else if (c == '[') {
				arraylevel++;
				continue;
			} else if (c == 'L') {
				int j;
				j = desc.indexOf(';',i+1);
				param = desc.substring(i+1,j);
				// replace '/'s with '.'s
				param = param.replace('/','.');
				i = j;
			} else
				assert false;

			if (didone) params.append(',');
			params.append(param);
			while (arraylevel>0) {
				params.append("[]");
				arraylevel--;
			}
			didone = true;
		}
		return params.toString();
	}
}
