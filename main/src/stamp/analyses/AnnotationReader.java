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
 * @author Yu Feng
**/
@Chord(name = "annot-java",
	   consumes = { "M", "Z" },
	   produces = { "L",
					"ArgArgTransfer", "ArgRetTransfer", 
					"ArgArgFlow",
					"SrcLabel", "SinkLabel",
					"InLabelArg", "InLabelRet",
					"OutLabelArg", "OutLabelRet", "DeviceId", "SubId", "Internet", "EncSrc", "EncSink",
                    "MODEL", "BRAND", "SDK", "Manufact", "Product", "LineNumber", "SmsContent", 
                    "SimSerial", "FileSrc", "FileSink", "WebView", "Exec", "InstallPackage"},
	   namesOfTypes = { "L" },
	   types = { DomL.class },
	   namesOfSigns = { "ArgArgTransfer", "ArgRetTransfer", 
						"ArgArgFlow",
						"SrcLabel", "SinkLabel",
						"InLabelArg", "InLabelRet",
						"OutLabelArg", "OutLabelRet", "DeviceId" , "SubId", "Internet", "EncSrc", "EncSink",
                        "MODEL", "BRAND", "SDK", "Manufact", "Product", "LineNumber", "SmsContent", 
                        "SimSerial", "FileSrc", "FileSink", "WebView", "Exec", "InstallPackage"},
	   signs = { "M0,Z0,Z1:M0_Z0_Z1", "M0,Z0:M0_Z0", 
				 "M0,Z0,Z1:M0_Z0_Z1",
				 "L0:L0", "L0:L0",
				 "L0,M0,Z0:L0_M0_Z0", "L0,M0:L0_M0",
				 "L0,M0,Z0:L0_M0_Z0", "L0,M0:L0_M0", "L0:L0", "L0:L0","L0:L0","L0:L0","L0:L0",
                 "L0:L0","L0:L0","L0:L0","L0:L0","L0:L0","L0:L0","L0:L0", 
                 "L0:L0","L0:L0","L0:L0","L0:L0","L0:L0", "L0:L0"}
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
        //by yufeng.
        sigLabels(srcLabels, sinkLabels);

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

    private void sigLabels(List<String> srcLabels, List<String> sinkLabels) {
		ProgramRel relDeviceId = (ProgramRel) ClassicProject.g().getTrgt("DeviceId");
		ProgramRel relSubId = (ProgramRel) ClassicProject.g().getTrgt("SubId");
		ProgramRel relInternet = (ProgramRel) ClassicProject.g().getTrgt("Internet");
		ProgramRel relEncSrc = (ProgramRel) ClassicProject.g().getTrgt("EncSrc");
		ProgramRel relEncSink = (ProgramRel) ClassicProject.g().getTrgt("EncSink");
		ProgramRel relModel = (ProgramRel) ClassicProject.g().getTrgt("MODEL");
		ProgramRel relBrand = (ProgramRel) ClassicProject.g().getTrgt("BRAND");
		ProgramRel relSdk = (ProgramRel) ClassicProject.g().getTrgt("SDK");
		ProgramRel relManufact = (ProgramRel) ClassicProject.g().getTrgt("Manufact");
		ProgramRel relProduct = (ProgramRel) ClassicProject.g().getTrgt("Product");
		ProgramRel relLineNumber = (ProgramRel) ClassicProject.g().getTrgt("LineNumber");
		ProgramRel relSmsContent = (ProgramRel) ClassicProject.g().getTrgt("SmsContent");
		ProgramRel relSimSerial = (ProgramRel) ClassicProject.g().getTrgt("SimSerial");
		ProgramRel relFileSrc = (ProgramRel) ClassicProject.g().getTrgt("FileSrc");
		ProgramRel relFileSink = (ProgramRel) ClassicProject.g().getTrgt("FileSink");
		ProgramRel relWebView = (ProgramRel) ClassicProject.g().getTrgt("WebView");
		ProgramRel relExec = (ProgramRel) ClassicProject.g().getTrgt("Exec");
		ProgramRel relInstallPkg = (ProgramRel) ClassicProject.g().getTrgt("InstallPackage");

		relDeviceId.zero();
		relSubId.zero();
		relInternet.zero();
		relEncSrc.zero();
		relEncSink.zero();
        relModel.zero();
        relBrand.zero();
        relSdk.zero();
        relManufact.zero();
        relProduct.zero();
        relLineNumber.zero();
        relSmsContent.zero();
        relSimSerial.zero();
        relFileSrc.zero();
        relFileSink.zero();
        relWebView.zero();
        relExec.zero();
        relInstallPkg.zero();

		for(String l : srcLabels) {
            if(l.equals("$getDeviceId"))
			    relDeviceId.add(l);
            if(l.equals("$getSubscriberId"))
			    relSubId.add(l);
            if(l.equals("$ENC/DEC"))
			    relEncSrc.add(l);
            if(l.equals("$MODEL"))
			    relModel.add(l);
            if(l.equals("$BRAND"))
			    relBrand.add(l);
            if(l.equals("$SDK"))
                relSdk.add(l);
            if(l.equals("$MANUFACTURER"))
                relManufact.add(l);
            if(l.equals("$PRODUCT"))
                relProduct.add(l);
            if(l.equals("$getLine1Number"))
                relLineNumber.add(l);
            if(l.equals("$content://sms"))
                relSmsContent.add(l);
            if(l.equals("$getSimSerialNumber"))
                relSimSerial.add(l);
            if(l.equals("$File"))
                relFileSrc.add(l);
            if(l.equals("$InstalledPackages"))
                relInstallPkg.add(l);
        }
		for(String l : sinkLabels) {
            if(l.equals("!INTERNET"))
                relInternet.add(l);
            if(l.equals("!ENC/DEC"))
                relEncSink.add(l);
            if(l.equals("!FILE"))
                relFileSink.add(l);
            if(l.equals("!WebView"))
                relWebView.add(l);
            if(l.equals("!PROCESS.OutputStream"))
                relExec.add(l);

        }

		relDeviceId.save();
		relSubId.save();
		relInternet.save();
		relEncSrc.save();
		relEncSink.save();
        relModel.save();
        relBrand.save();
        relSdk.save();
        relProduct.save();
        relManufact.save();
        relLineNumber.save();
        relSmsContent.save();
        relSimSerial.save();
        relFileSrc.save();
        relFileSink.save();
        relWebView.save();
        relExec.save();
        relInstallPkg.save();
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
        Program prog = Program.g();
		Scene scene = prog.scene();

		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File("stamp_annotations.txt")));
			String line = reader.readLine();
			while(line != null){
				final String[] tokens = line.split(" ");
				String chordMethodSig = tokens[0];
				int atSymbolIndex = chordMethodSig.indexOf('@');
				String className = chordMethodSig.substring(atSymbolIndex+1);
				if(scene.containsClass(className)) {
					SootClass klass = scene.getSootClass(className);
					String subsig = SootUtils.getSootSubsigFor(chordMethodSig.substring(0,atSymbolIndex));
					SootMethod meth = klass.getMethod(subsig);
					
					if((domM.indexOf(meth) >= 0) && !prog.exclude(meth)){
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
		System.out.println("+++ " + meth + " " + from + " " + to);
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
