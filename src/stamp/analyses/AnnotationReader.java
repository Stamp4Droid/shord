package stamp.analyses;

import java.util.List;
import java.util.LinkedList;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.analyses.JavaAnalysis;
import shord.project.ClassicProject;
import shord.analyses.DomM;

import chord.project.Chord;

/**
 * @author Saswat Anand
**/
@Chord(name = "annot-java",
	   consumes = { "M", "Z" },
	   produces = { "SRC", "SINK", 
					"DirectArgArgFlow", "DirectArgRetFlow", 
					"DirectArgSinkFlow", "DirectRetSinkFlow",
					"DirectSrcArgFlow", "DirectSrcRetFlow" },
	   namesOfTypes = { "SRC", "SINK" },
	   types = { DomSRC.class, DomSINK.class },
	   namesOfSigns = { "DirectArgArgFlow", "DirectArgRetFlow", 
						"DirectArgSinkFlow", "DirectRetSinkFlow",
						"DirectSrcArgFlow", "DirectSrcRetFlow" },
	   signs = { "M0,Z0,Z1:M0_Z0_Z1", "M0,Z0:M0_Z0", 
				 "M0,Z0,SINK0:SINK0_M0_Z0", "M0,SINK0:SINK0_M0",
				 "SRC0,M0,Z0:SRC0_M0_Z0", "SRC0,M0:SRC0_M0" }
	   )
public class AnnotationReader extends JavaAnalysis
{
	private ProgramRel relDirectArgArgFlow;
	private ProgramRel relDirectArgRetFlow;
	private ProgramRel relDirectArgSinkFlow; 
	private ProgramRel relDirectRetSinkFlow;
	private ProgramRel relDirectSrcArgFlow;
	private ProgramRel relDirectSrcRetFlow;

	public void run()
	{
		DomSRC domSRC = (DomSRC) ClassicProject.g().getTrgt("SRC");
		DomSINK domSINK = (DomSINK) ClassicProject.g().getTrgt("SINK");
		DomM domM = (DomM) ClassicProject.g().getTrgt("M");

		relDirectArgArgFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectArgArgFlow");
		relDirectArgRetFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectArgRetFlow");

		relDirectArgArgFlow.zero();
		relDirectArgRetFlow.zero();

		List worklist = new LinkedList();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File("stamp_annotations.txt")));
			String line = reader.readLine();
			while(line != null){
				final String[] tokens = line.split(" ");
				String chordMethodSig = tokens[0];
				int atSymbolIndex = chordMethodSig.indexOf('@');
				String className = chordMethodSig.substring(atSymbolIndex+1);
				if(Scene.v().containsClass(className)){
					SootClass klass = Scene.v().getSootClass(className);
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

		relDirectArgArgFlow.save();	
		relDirectArgRetFlow.save();

		relDirectArgSinkFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectArgSinkFlow");
		relDirectRetSinkFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectRetSinkFlow");
		relDirectSrcArgFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectSrcArgFlow");
		relDirectSrcRetFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectSrcRetFlow");

		relDirectArgSinkFlow.zero();
		relDirectRetSinkFlow.zero();
		relDirectSrcArgFlow.zero();
		relDirectSrcRetFlow.zero();
		
		while(!worklist.isEmpty()){
			SootMethod meth = (SootMethod) worklist.remove(0);
			String from = (String) worklist.remove(0);
			String to = (String) worklist.remove(0);
			addFlow(meth, from, to);
		}

		relDirectArgSinkFlow.save();
		relDirectRetSinkFlow.save();
		relDirectSrcArgFlow.save();
		relDirectSrcRetFlow.save();
	}

	private void addFlow(SootMethod meth, String from, String to) //throws NumberFormatException
	{
		System.out.println("+++ " + meth + " " + from + " " + to);
		if(from.charAt(0) == '$') {
			if(to.equals("-1"))
				relDirectSrcRetFlow.add(from, meth);
			else
				relDirectSrcArgFlow.add(from, meth, Integer.valueOf(to));
		} else {
			assert !(from.charAt(0) == '!');
			Integer fromArgIndex = Integer.valueOf(from);
			if(to.charAt(0) == '!'){
				if(from.equals("-1"))
					relDirectRetSinkFlow.add(meth, to);
				else
					relDirectArgSinkFlow.add(meth, fromArgIndex, to);
			} else if(to.equals("-1")){
				relDirectArgRetFlow.add(meth, fromArgIndex);
			} else {
				Integer toArgIndex = Integer.valueOf(to);
				relDirectArgArgFlow.add(meth, fromArgIndex, toArgIndex);
			}
		}
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
			} else if ( c == 'D') {
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
