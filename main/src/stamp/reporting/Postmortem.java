package stamp.reporting;

import chord.project.Chord;
import shord.project.ClassicProject;
import shord.project.analyses.JavaAnalysis;
import java.io.*;

/**
 * @author Saswat Anand
 * @author Osbert Bastani
 */
@Chord(name = "post-java")
public class Postmortem extends JavaAnalysis
{
	public static final String reportsTxtFilePath = System.getProperty("stamp.out.dir")+"/reports.txt";
	public static final String resultsDir = System.getProperty("stamp.out.dir")+"/results";

    public void run()
	{
		new File(resultsDir).mkdirs();

		Class[] srcReports = new Class[]{
			SrcFlow.class
			,ArgSinkFlow.class
			,SrcSinkFlow.class
			////,ReachableStub.class,
			,TaintedStub.class,
			////,InvkNone.class,
			,TaintedVar.class
			,IM.class
			,PotentialCallbacks.class
			,AllReachable.class
			,FileNames.class
		};

		Class[] apkReports = new Class[]{
			SrcSinkFlow.class
		};

		Class[] dontShowReports = new Class[]{
			IM.class
			,AllReachable.class
			,FileNames.class
		};

		try{
			PrintWriter reportsTxtWriter = new PrintWriter(new FileWriter(new File(reportsTxtFilePath)));
			Class[] reports = System.getProperty("stamp.input.type", "src").equals("src") ? srcReports : apkReports;
			for(Class reportClass : reports){
				XMLReport report = report = (XMLReport) reportClass.newInstance();
				
				boolean show = true;
				for(int i = 0; show && i < dontShowReports.length; i++){
					if(reportClass.equals(dontShowReports[i]))
						show = false;
				}
				if(show)
					reportsTxtWriter.println(report.getTitle() + " " + report.getCanonicalReportFilePath());
				
				report.write();
			}
			
			reportsTxtWriter.close();
		}catch(Exception e){
			throw new Error(e);
		}
    }
}
