package stamp.analyses;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import shord.project.analyses.ProgramRel;
import soot.SootMethod;
import chord.project.Chord;

/**
 * @author obastani
 */
@Chord(name = "suspicious-call-java")
public class SuspiciousCallAnalysis extends JavaAnalysis {
	private static String[] signatureArray = {
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
        "<javax.crypto.Cipher: int update(java.nio.ByteBuffer,java.nio.ByteBuffer)>",
        "<android.content.IntentFilter: void setPriority(int)>",
        "<android.content.Context: android.content.Intent registerReceiver(android.content.BroadcastReceiver,android.content.IntentFilter)>",
        "<android.telephony.gsm.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>"
    };
	
	@Override
	public void run() {
		ProgramRel relReachableM = (ProgramRel)ClassicProject.g().getTrgt("ci_reachableM");
		relReachableM.load();
		Set<String> signatures = new HashSet<String>(Arrays.asList(signatureArray));
		int count = 0;
		for(Object obj : relReachableM.getAry1ValTuples()) {
			SootMethod m = (SootMethod)obj;
			String signature = m.getSignature();
			if(signatures.contains(signature)) {
				System.out.println("SUSPICIOUS: " + signature);
				count++;
			}
		}
		relReachableM.close();
		System.out.println("SUSPICIOUS CALL COUNT: " + count);
	}
}
