package shord.analyses;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.AbstractJasminClass;

import java.io.File;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.jimple.internal.VariableBox;
import soot.tagkit.LinkTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import java.util.*;
import stamp.analyses.ImplicitIntentDef;
import stamp.analyses.ReachingDefsAnalysis;
import stamp.analyses.iccg.*;
import stamp.harnessgen.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import shord.program.Program;

import soot.util.NumberedSet;

import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import shord.project.analyses.ProgramDom;
import shord.project.ClassicProject;

import chord.project.Chord;
import stamp.analyses.SootUtils;

import java.util.jar.*;
import java.io.*;

/**
  * Search for implicit intent.
  **/

@Chord(name="base-java-iccg",
       produces={ "MC", "IntentTgtField", "ActionField", "DataTypeField",
                  "abortBroadcast", "RuntimeExec", "sendTextMsg"},
       namesOfTypes = {"M", "T", "I"},
       types = { DomM.class, DomT.class, DomI.class},
       namesOfSigns = { "MC", "IntentTgtField", "ActionField", "DataTypeField", "SpecM",
                        "abortBroadcast", "RuntimeExec", "sendTextMsg"},
       signs = { "M0,COMP0:M0_COMP0", "F0:F0", "F0:F0", "F0:F0",  "M0:M0",
                 "M0:M0", "M0:M0", "M0:M0"}
       )
public class ICCGBuilder extends JavaAnalysis
{

    private	DomM domM;
	private ProgramRel relMC;
	private ProgramRel relSpecM;
    private ProgramRel relAbort;
    private ProgramRel relExec; 
	private ProgramRel relSendMsg;

	private int maxArgs = -1;
	private FastHierarchy fh;
	public static NumberedSet stubMethods;

	public static final boolean ignoreStubs = false;

	private SootClass klass;

	public static Map<String, XmlNode> components = new HashMap<String, XmlNode>();


	/*private static String[] launchArray = {
	"<android.app.Activity: void startActivity(android.content.Intent)>",
	"<android.content.ContextWrapper: void sendBroadcast(android.content.Intent)>",
	//shall we mark bindservice?|| methSig.equals("<android.content.ContextWrapper: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>") 
	"<android.content.ContextWrapper: android.content.ComponentName startService(android.content.Intent)>",
	"<android.content.ContextWrapper: void sendBroadcast(android.content.Intent,java.lang.String)>",
	"<android.content.ContextWrapper: void sendStickyBroadcast(android.content.Intent)>",
	"<android.content.ContextWrapper: void sendOrderedBroadcast(android.content.Intent,java.lang.String)>",
	"<android.content.ContextWrapper: void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>",
	"<android.content.ContextWrapper: void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>",
	"<android.content.Context: void sendBroadcast(android.content.Intent)>",
	"<android.content.Context: android.content.ComponentName startService(android.content.Intent)>",
	"<android.content.Context: void sendBroadcast(android.content.Intent,java.lang.String)>",
	"<android.content.Context: void sendStickyBroadcast(android.content.Intent)>",
	"<android.content.Context: void sendOrderedBroadcast(android.content.Intent,java.lang.String)>",
	"<android.content.Context: void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>",
	"<android.content.Context: void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)>",
	"<android.app.Activity: void startActivities(android.content.Intent[])>",
	"<android.app.Activity: void startIntentSender(android.content.IntentSender,android.content.Intent,int,int,int)>",
	"<android.app.Activity: void startActivityForResult(android.content.Intent,int)>",
	"<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int)>",
	"<android.app.Activity: boolean startNextMatchingActivity(android.content.Intent)>",
	"<android.content.Context: void startActivity(android.content.Intent)>",
	"<android.app.Activity: void startActivityFromChild(android.app.Activity,android.content.Intent,int)>",
	"<android.app.Activity: void startActivityFromFragment(android.app.Fragment,android.content.Intent,int)>",
	"<android.app.Activity: void startIntentSenderForResult(android.content.IntentSender,int,android.content.Intent,int,int,int)>",
	"<android.app.Activity: void startIntentSenderFromChild(android.app.Activity,android.content.IntentSender,int,android.content.Intent,int,int,int)>",
        "<android.widget.TabHost$TabSpec: android.widget.TabHost$TabSpec setContent(android.content.Intent)>"
	};*/


	private static String[] subLaunchArray = {
	"void startActivity(android.content.Intent)",
	"void sendBroadcast(android.content.Intent)",
	"boolean bindService(android.content.Intent,android.content.ServiceConnection,int)",
	"android.content.ComponentName startService(android.content.Intent)",
	"void sendBroadcast(android.content.Intent,java.lang.String)",
	"void sendStickyBroadcast(android.content.Intent)",
	"void sendOrderedBroadcast(android.content.Intent,java.lang.String)",
	"void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
	"void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
	"void sendBroadcast(android.content.Intent)",
	"android.content.ComponentName startService(android.content.Intent)",
	"void sendBroadcast(android.content.Intent,java.lang.String)",
	"void sendStickyBroadcast(android.content.Intent)",
	"void sendOrderedBroadcast(android.content.Intent,java.lang.String)",
	"void sendOrderedBroadcast(android.content.Intent,java.lang.String,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
	"void sendStickyOrderedBroadcast(android.content.Intent,android.content.BroadcastReceiver,android.os.Handler,int,java.lang.String,android.os.Bundle)",
	"void startActivities(android.content.Intent[])",
	"void startIntentSender(android.content.IntentSender,android.content.Intent,int,int,int)",
	"void startActivityForResult(android.content.Intent,int)",
	"boolean startActivityIfNeeded(android.content.Intent,int)",
	"boolean startNextMatchingActivity(android.content.Intent)",
	"void startActivity(android.content.Intent)",
    "void setResult(int,android.content.Intent)",
	"void startActivityFromChild(android.app.Activity,android.content.Intent,int)",
	"void startActivityFromFragment(android.app.Fragment,android.content.Intent,int)",
	"void startIntentSenderForResult(android.content.IntentSender,int,android.content.Intent,int,int,int)",
	"void startIntentSenderFromChild(android.app.Activity,android.content.IntentSender,int,android.content.Intent,int,int,int)",
    "android.widget.TabHost$TabSpec setContent(android.content.Intent)"
	};


    public static List launchList = new ArrayList<String>(Arrays.asList(subLaunchArray));

    public static String pkgName = ""; 

    void openRels()
    {
        relMC = (ProgramRel) ClassicProject.g().getTrgt("MC");
        relMC.zero();
        relSpecM = (ProgramRel) ClassicProject.g().getTrgt("SpecM");
        relSpecM.zero();

		relAbort = (ProgramRel) ClassicProject.g().getTrgt("abortBroadcast");
		relExec = (ProgramRel) ClassicProject.g().getTrgt("RuntimeExec");
		relSendMsg = (ProgramRel) ClassicProject.g().getTrgt("sendTextMsg");
	    relAbort.zero();
	    relExec.zero();
	    relSendMsg.zero();
    }

    void saveRels() 
    {
        relMC.save();
        relSpecM.save();
		relAbort.save();
		relExec.save();
		relSendMsg.save();
    }

    public ICCGBuilder()
    {
        String stampOutDir = System.getProperty("stamp.out.dir");
        //parse manifest.xml
        String manifestDir = stampOutDir + "/apktool-out";
        File manifestFile = new File(manifestDir, "AndroidManifest.xml");
        ParseManifest pmf = new ParseManifest();
        pmf.extractComponents(manifestFile, components);
	    pkgName = pmf.getPkgName();
        System.out.println("Current components: " + components);
    }


	protected void visit(SootClass klass)
	{
		this.klass = klass;
		Collection<SootMethod> methodsCopy = new ArrayList(klass.getMethods());
		for(SootMethod method : methodsCopy)
		    visitMethod(method);
	}

    private void visitMethod(SootMethod method)
    {
        if(!method.isConcrete())
            return;

        String[] callerComp = {
            "<android.content.BroadcastReceiver: void abortBroadcast()>",
            "<java.lang.Runtime: java.lang.Process exec(java.lang.String)>",
            "<java.lang.System: void loadLibrary(java.lang.String)>",
            "<java.lang.System: void load(java.lang.String)>",
            "<android.telephony.SmsManager: void sendMultipartTextMessage(java.lang.String,java.lang.String,java.util.ArrayList,java.util.ArrayList,java.util.ArrayList)>",
            "<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
            "<android.content.Context: java.lang.String getPackageName()>",
            "<android.content.Context: android.content.pm.PackageManager getPackageManager()>",
            "<dalvik.system.DexClassLoader: void <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.ClassLoader)>",
            "<java.lang.ClassLoader: java.lang.Class loadClass(java.lang.String)>",

            "<android.app.WallpaperManager: void setBitmap(android.graphics.Bitmap)>",
            "<android.app.WallpaperManager: void setResource(int)>",
            "<android.app.WallpaperManager: void setStream(java.io.InputStream)>",
            "<android.content.Context: void setWallpaper(java.io.InputStream)>",
            "<android.content.Context: void setWallpaper(android.graphics.Bitmap)>",

            "<java.lang.Runtime: void load(java.lang.String)>",
            "<java.lang.Runtime: void loadLibrary(java.lang.String)>",
            "<java.lang.Runtime: java.lang.Process exec(java.lang.String)>",
            "<java.lang.Runtime: java.lang.Process exec(java.lang.String[])>",
            "<java.lang.Runtime: java.lang.Process exec(java.lang.String[],java.lang.String[])>",
            "<java.lang.Runtime: java.lang.Process exec(java.lang.String[],java.lang.String[],java.io.File)>",
            "<java.lang.Runtime: java.lang.Process exec(java.lang.String,java.lang.String[])>",
            "<java.lang.Runtime: java.lang.Process exec(java.lang.String,java.lang.String[],java.io.File)>",
            "<java.lang.ProcessBuilder: java.lang.Process start()>",

            "<javax.crypto.Cipher: byte[] doFinal()>",
            "<javax.crypto.Cipher: byte[] doFinal(byte[])>",
            "<javax.crypto.Cipher: int doFinal(byte[],int)>",
            "<javax.crypto.Cipher: byte[] doFinal(byte[],int,int)>",
            "<javax.crypto.Cipher: int doFinal(byte[],int,int,byte[])>",
            "<javax.crypto.Cipher: byte[] doFinal(byte[])>",

            "<javax.crypto.Cipher: byte[] update(byte[])>",
            "<javax.crypto.Cipher: byte[] update(byte[],int,int)>",
            "<javax.crypto.Cipher: int update(byte[],int,int,byte[])>",
            "<javax.crypto.Cipher: int update(byte[],int,int,byte[],int)>",
            "<javax.crypto.Cipher: int update(java.nio.ByteBuffer,java.nio.ByteBuffer)>"

        };
        String abortSig = "<android.content.BroadcastReceiver: void abortBroadcast()>";
        String execSig = "<java.lang.Runtime: java.lang.Process exec(java.lang.String)>";
        String sendMsgSig = "<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>";
        List<String> cList = Arrays.asList(callerComp);
        //record special invocation.
        if(cList.contains(method.getSignature()) )
            relSpecM.add(method);

        ///////////
        if(abortSig.equals(method.getSignature()))
            relAbort.add(method);
        if(execSig.equals(method.getSignature()))
            relExec.add(method);
        if(sendMsgSig.equals(method.getSignature()))
            relSendMsg.add(method);
        ////////////

        if(components.get(this.klass.getName()) != null) {
            String[] str = { "void onPostResume()",
                    "java.lang.CharSequence onCreateDescription()",
                    "void onRestoreInstanceState(android.os.Bundle)",
                    "void onPostCreate(android.os.Bundle)",
                    "void onStart()",
                    "void onCreate(android.os.Bundle)",
                    "void onUserLeaveHint()",
                    "void onResume()",
                    "void onStop()",
                    "void onPause()",
                    "void onRestart()",
                    "boolean onCreateThumbnail(android.graphics.Bitmap,android.graphics.Canvas)",
                    "void onNewIntent(android.content.Intent)",
                    "void onDestroy()",
                    "void onSaveInstanceState(android.os.Bundle)"};
            List<String> circleList = Arrays.asList(str);
            String compKey = getCompKey(klass.getName());

            //if(circleList.contains(method.getSubSignature()))
            assert(compKey != null);
            if(domM.contains(method)) 
                relMC.add(method, compKey);
        }

    }

    //find the actual string object in domComp.
    public static String getCompKey(String val)
    {
        String compKey = null;
        for(Object node : components.keySet()) {
            if(val.equals(node)){
                compKey = (String)node;
                break;
            }
        }

        return compKey;
    }

	//target name and action in intent object.
	private void populateIntentActionTgt() 
	{
		ProgramRel relIntentTgtField = (ProgramRel) ClassicProject.g().getTrgt("IntentTgtField");
        relIntentTgtField.zero();
		ProgramRel relActionField = (ProgramRel) ClassicProject.g().getTrgt("ActionField");
        relActionField.zero();
		ProgramRel relDataTypeField = (ProgramRel) ClassicProject.g().getTrgt("DataTypeField");
        relDataTypeField.zero();

		SootClass klass = Program.g().scene().getSootClass("android.content.Intent");
		SootField nameField = klass.getFieldByName("name");
		SootField actionField = klass.getFieldByName("action");
		SootField dataTypeField = klass.getFieldByName("type");

		relIntentTgtField.add(nameField);
		relIntentTgtField.save();

		relActionField.add(actionField);
		relActionField.save();

        relDataTypeField.add(dataTypeField);
        relDataTypeField.save();

	}

    public void run()
    {
        openRels();
        populateIntentActionTgt();
        domM = (DomM) ClassicProject.g().getTrgt("M");

        for(SootClass klass: Program.g().getClasses()){
            this.visit(klass);
        }
        saveRels();
    }


}
