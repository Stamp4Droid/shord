package stamp.harnessgen;

import java.io.*;
import java.util.*;

import stamp.app.App;
import stamp.app.Component;
import stamp.app.Layout;

import soot.Scene;
import soot.CompilationDeathException;
import soot.options.Options;


/*
* @author Saswat Anand
*/
public class Main
{
	private static App app;

	public static void main(String[] args) throws Exception
	{
		String apktoolOutDir = System.getProperty("stamp.apktool.out.dir");
		String apkPath = System.getProperty("stamp.apk.path");
		String driverDirName = System.getProperty("stamp.driver.dir");
		String androidJar = System.getProperty("stamp.android.jar");
		String harnessListFile = System.getProperty("stamp.harnesslist.file");
		int numCompsPerHarness = Integer.parseInt(System.getProperty("stamp.max.harness.size"));

		initSoot(apkPath, androidJar, comps);

		app = App.readApp(apkPath, apktoolOutDir);
		List<Component> comps = app.components();
		
		File driverDir = new File(driverDirName, "stamp/harness");
		driverDir.mkdirs();
		
		PrintWriter writer = new PrintWriter(new FileWriter(new File(harnessListFile)));

		int numComps = comps.size();
		System.out.println("number of components = "+numComps);
		int harnessCount = 0;
		int i = 0;
		while(i < numComps){
			harnessCount++;
			String harnessClassName = "stamp.harness.Main"+harnessCount;
			writer.println(harnessClassName);
			Harness h = new Harness(harnessClassName, comps);
			for(int j = 0; j < numCompsPerHarness && i < numComps; j++, i++){
				Component comp = comps.get(i);
				h.addComponent(comp);
			}

			File harnessClassFile = new File(driverDirName, harnessClassName.replace('.','/').concat(".class"));
			h.writeTo(harnessClassFile);
		}
		writer.close();
	}

	private static void initSoot(String apkPath, String androidJar, List<Component> comps)
	{
        try {
			StringBuilder options = new StringBuilder();
			options.append("-allow-phantom-refs");
			options.append(" -src-prec apk");
			options.append(" -p jb.tr use-older-type-assigner:true"); 
			//options.append(" -p cg implicit-entry:false");
			options.append(" -force-android-jar "+System.getProperty("user.dir"));
			options.append(" -soot-classpath "+androidJar+File.pathSeparator+apkPath);
			//options.append(" -f jimple");
			options.append(" -f none");

			if (!Options.v().parse(options.toString().split(" ")))
				throw new CompilationDeathException(
													CompilationDeathException.COMPILATION_ABORTED,
													"Option parse error");
            Scene.v().loadBasicClasses();

			Scene.v().loadClassAndSupport("edu.stanford.stamp.harness.ApplicationDriver");
			for(Component c : comps){
				Scene.v().loadClassAndSupport(c.name);
			}

			Scene.v().loadDynamicClasses();
        } catch (Exception e) {
			throw new Error(e);
        }
	}
}
