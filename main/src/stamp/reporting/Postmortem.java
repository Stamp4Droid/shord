package stamp.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import shord.program.Program;
import shord.project.analyses.JavaAnalysis;
import stamp.srcmap.SourceInfoSingleton;
import stamp.util.PropertyHelper;
import chord.project.Chord;

/**
 * @author Saswat Anand
 * @author Osbert Bastani
 */
@Chord(name = "post-java")
public class Postmortem extends JavaAnalysis {
	public static final String reportsTxtFilePath = System.getProperty("stamp.out.dir")+"/reports.txt";
	public static final String resultsDir = System.getProperty("stamp.out.dir")+"/results";
	public static final boolean processingSrc = System.getProperty("stamp.input.type", "src").equals("src");

    public void run() {
		new File(resultsDir).mkdirs();

		Class[] srcReports = new Class[]{
			SrcFlow.class
			,ArgSinkFlow.class
			,SrcSinkFlow.class
			,SrcSinkFlowViz.class
			//,ReachableStub.class,
			,TaintedStub.class
			//,InvkNone.class,
			,TaintedVar.class
			,IM.class
			,PotentialCallbacks.class
			,AllReachable.class
			,FileNames.class
			,MissingModels.class
			,AllMissingModels.class
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
			Class[] reports = processingSrc ? srcReports : apkReports;

			for(int j=0; j<2; j++) {
				boolean jimple = (j == 0);
				for(Class reportClass : reports) {
					XMLReport report = (XMLReport) reportClass.newInstance();
					if(jimple) {
						report.setSourceInfo(SourceInfoSingleton.getJimpleSourceInfo());
					}
					
					boolean show = true;
					for(int i = 0; show && i < dontShowReports.length; i++){
						if(reportClass.equals(dontShowReports[i]))
							show = false;
					}
					if(show) {
						reportsTxtWriter.println(report.getTitle() + " " + report.getCanonicalReportFilePath(jimple));
					}
				
					report.write(jimple);
				}
			}
			
			reportsTxtWriter.close();
		} catch(Exception e){
			throw new Error(e);
		}
		
		boolean printClasses =
			PropertyHelper.getBoolProp("stamp.print.allclasses");
		System.out.println("stamp.print.allclasses = "+printClasses);
		if(printClasses)
			Program.g().printAllClasses();
    }
}
