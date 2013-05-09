package stamp.analyses;

import java.util.List;
import java.util.LinkedList;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import soot.SootMethod;

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

	private static void readAnnotations() throws IOException
	{
		DomSRC domSRC = (DomSRC) ClassicProject.g().getTrgt("SRC");
		DomSINK domSINK = (DomSINK) ClassicProject.g().getTrgt("SINK");

		relDirectArgArgFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectArgArgFlow");
		relDirectArgRetFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectArgRetFlow");
		relDirectArgSinkFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectArgSinkFlow");
		relDirectRetSinkFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectRetSinkFlow");
		relDirectSrcArgFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectSrcArgFlow");
		relDirectSrcRetFlow = (ProgramRel) ClassicProject.g().getTrgt("DirectSrcRetFlow");

		List worklist = new Linkedist();
		BufferedReader reader = new BufferedReader(new FileReader(new File("stamp_annotations.txt")));
		String line = reader.readLine();
		while(line != null){
			final String[] tokens = line.split(" ");
			String chordMethodSig = tokens[0];
			int atSymbolIndex = chordMethoSig.indexOf('@');
			String className = chordMethodSig.substring(atSymbolIndex+1);
			if(!Scene.v().containsClass(className))
				continue;
			SootClass klass = Scene.v().getSootClass(className);
			String subsig = getSootSubsigFor(chordMethodSig.substring(atSymbolIndex));
			SootMethod meth = klass.getMethod(subsig);
			
			String from = tokens[1];
			String to = tokens[2];

			boolean src = from.startsWith('$');
			boolean sink = to.startsWith('!');
			if(src || sink){
				if(src)
					domSRC.add(from);
				if(sink)
					domSINK.add(to);
				if(src && sink){
					System.out.println("Unsupported annotation type "+line);
				} else {
					worklist.add(meth);
					worklist.add(from);
					worklist.add(to);
				} 
			} else {
				addFlow(meth, src, sink);
			}
			line = reader.readLine();
		}
		reader.close();
		domSRC.save();
		domSINK.save();

		while(!worklist.isEmpty()){
			SootMethod meth = (SootMethod) worklist.remove(0);
			String from = (String) worklist.remove(0);
			String to = (String) worklist.remove(0);
			addFlow(meth, from, to);
		}

		relDirectArgArgFlow.save();	
		relDirectArgRetFlow.save();
		relDirectArgSinkFlow.save();
		relDirectRetSinkFlow.save();
		relDirectSrcArgFlow.save();
		relDirectSrcRetFlow.save();
	}

	private static void addFlow(SootMethod meth, String from, String to) //throws NumberFormatException
	{
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
	
	String getSootSubsigFor(String chordSubsig)
	{
		String name = chordSubsig.substring(chordSubsig.indexOf(':'));
		String retType = chordSubsig.substring(chordSubsig.indexOf(')')+1);
		String paramTypes = chordSubsig.substring(chordSubsig.indexOf('(')+1, chordSubsig.indexOf(')'));
		
	}
}
