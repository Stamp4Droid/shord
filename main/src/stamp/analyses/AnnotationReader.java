package stamp.analyses;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
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
	   produces = { "L",
					"ArgArgTransfer", "ArgRetTransfer", 
					"ArgArgFlow",
					"SrcLabel", "SinkLabel",
					"InLabelArg", "InLabelRet",
					"OutLabelArg", "OutLabelRet" },
	   namesOfTypes = { "L" },
	   types = { DomL.class },
	   namesOfSigns = { "ArgArgTransfer", "ArgRetTransfer", 
						"ArgArgFlow",
						"SrcLabel", "SinkLabel",
						"InLabelArg", "InLabelRet",
						"OutLabelArg", "OutLabelRet" },
	   signs = { "M0,Z0,Z1:M0_Z0_Z1", "M0,Z0:M0_Z0", 
				 "M0,Z0,Z1:M0_Z0_Z1",
				 "L0:L0", "L0:L0",
				 "L0,M0,Z0:L0_M0_Z0", "L0,M0:L0_M0",
				 "L0,M0,Z0:L0_M0_Z0", "L0,M0:L0_M0" }
	   )
public class AnnotationReader extends JavaAnalysis
{
	private ProgramRel relArgArgTransfer;
	private ProgramRel relArgRetTransfer;
	private ProgramRel relArgArgFlow;

	private ProgramRel relInLabelArg; 
	private ProgramRel relInLabelRet;
	private ProgramRel relOutLabelArg; 
	private ProgramRel relOutLabelRet;

	public void run()
	{		
		List<String> srcLabels = new ArrayList();
		List<String> sinkLabels = new ArrayList();
		List worklist = new LinkedList();		
		//fill ArgArgTransfer, ArgRetTransfer, ArgArgFlow
		process(srcLabels, sinkLabels, worklist);

		//fill DomL
		DomL domL = (DomL) ClassicProject.g().getTrgt("L");
		for(String l : srcLabels)
			domL.add(l);
		for(String l : sinkLabels)
			domL.add(l);
		domL.save();

		//fille SrcLabel
		ProgramRel relSrcLabel = (ProgramRel) ClassicProject.g().getTrgt("SrcLabel");
		relSrcLabel.zero();
		for(String l : srcLabels)
			relSrcLabel.add(l);
		relSrcLabel.save();

		//fill SinkLabel
		ProgramRel relSinkLabel = (ProgramRel) ClassicProject.g().getTrgt("SinkLabel");
		relSinkLabel.zero();
		for(String l : sinkLabels)
			relSinkLabel.add(l);
		relSinkLabel.save();

		//fill LabelArg and LabelRet
		relInLabelArg = (ProgramRel) ClassicProject.g().getTrgt("InLabelArg");
		relInLabelRet = (ProgramRel) ClassicProject.g().getTrgt("InLabelRet");
		relOutLabelArg = (ProgramRel) ClassicProject.g().getTrgt("OutLabelArg");
		relOutLabelRet = (ProgramRel) ClassicProject.g().getTrgt("OutLabelRet");
		relInLabelArg.zero();
		relInLabelRet.zero();		
		relOutLabelArg.zero();
		relOutLabelRet.zero();		
		while(!worklist.isEmpty()){
			SootMethod meth = (SootMethod) worklist.remove(0);
			String from = (String) worklist.remove(0);
			String to = (String) worklist.remove(0);
			addFlow(meth, from, to);
		}
		relInLabelArg.save();
		relInLabelRet.save();
		relOutLabelArg.save();
		relOutLabelRet.save();
	}

	private void process(List<String> srcLabels, List<String> sinkLabels, List worklist)
	{
		relArgArgTransfer = (ProgramRel) ClassicProject.g().getTrgt("ArgArgTransfer");
		relArgRetTransfer = (ProgramRel) ClassicProject.g().getTrgt("ArgRetTransfer");
		relArgArgFlow = (ProgramRel) ClassicProject.g().getTrgt("ArgArgFlow");

		relArgArgTransfer.zero();
		relArgRetTransfer.zero();
		relArgArgFlow.zero();

		DomM domM = (DomM) ClassicProject.g().getTrgt("M");
		Scene scene = Program.g().scene();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File(System.getProperty("stamp.out.dir"), "stamp_annotations.txt")));
			String line = reader.readLine();
			while(line != null){
				final String[] tokens = line.split(" ");
				String chordMethodSig = tokens[0];
				int atSymbolIndex = chordMethodSig.indexOf('@');
				String className = chordMethodSig.substring(atSymbolIndex+1);
				if(scene.containsClass(className)){
					SootClass klass = scene.getSootClass(className);
					String subsig = SootUtils.getSootSubsigFor(chordMethodSig.substring(0,atSymbolIndex));
					SootMethod meth = klass.getMethod(subsig);
					
					if(domM.indexOf(meth) >= 0){
						String from = tokens[1];
						String to = tokens[2];
			
						boolean b1 = addLabel(from, srcLabels, sinkLabels);
						boolean b2 = addLabel(to, srcLabels, sinkLabels);

						char c = from.charAt(0);
						boolean src = (c == '$' || c == '!');
						boolean sink = to.charAt(0) == '!';
						if(b1 && b2){
							System.out.println("Unsupported annotation type "+line);
						} else if(b1 || b2){							
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

		relArgArgTransfer.save();	
		relArgRetTransfer.save();
		relArgArgFlow.save();	
	}

	private boolean addLabel(String label, List<String> srcLabels, List<String> sinkLabels)
	{
		char c = label.charAt(0);
		if(c == '$'){
			srcLabels.add(label);
			return true;
		} 
		if(c == '!'){
			sinkLabels.add(label);
			return true;
		}
		return false;
	}

	private void addFlow(SootMethod meth, String from, String to) //throws NumberFormatException
	{
		//System.out.println("+++ " + meth + " " + from + " " + to);
		List<SootMethod> meths = SootUtils.overridingMethodsFor(meth);
		char from0 = from.charAt(0);
		if(from0 == '$' || from0 == '!') {
			if(to.equals("-1")){
				for(SootMethod m : meths)
					relInLabelRet.add(from, m);
			}
			else{
				for(SootMethod m : meths)
					relInLabelArg.add(from, m, Integer.valueOf(to));
			}
		} else {
			Integer fromArgIndex = Integer.valueOf(from);
			char to0 = to.charAt(0);
			if(to0 == '!'){
				if(from.equals("-1")){
					for(SootMethod m : meths)
						relOutLabelRet.add(to, m);
				} else{
					for(SootMethod m : meths)
						relOutLabelArg.add(to, m, fromArgIndex);
				}
			} else if(to0 == '?'){
				Integer toArgIndex = Integer.valueOf(to.substring(1));
				for(SootMethod m : meths)
					relArgArgFlow.add(m, fromArgIndex, toArgIndex);
			} else if(to.equals("-1")){
				for(SootMethod m : meths)
					relArgRetTransfer.add(m, fromArgIndex);
			} else {
				Integer toArgIndex = Integer.valueOf(to);
				for(SootMethod m : meths)
					relArgArgTransfer.add(m, fromArgIndex, toArgIndex);
			}
		}
	}
}
