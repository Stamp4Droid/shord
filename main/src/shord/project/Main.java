package shord.project;

import java.io.File;
import java.io.PrintStream;

import shord.project.Program;
import shord.project.Config;
import shord.project.Project;
import shord.util.Timer;
import shord.util.Utils;

/**
 * Entry point of Shord.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 * @author Saswat Anand
 */
public class Main {
	public static long startTime;
    public static void main(String[] args) throws Exception {
    	// setup
    	startTime = System.currentTimeMillis();
    	Utils.mkdirs(Config.v().outDirName);
        Utils.mkdirs(Config.v().bddbddbWorkDirName);
        File outFile;
        {
            String outFileName = Config.v().outFileName;
            if (outFileName == null)
                outFile = null;
            else {
                outFile = new File(outFileName);
                System.out.println("Redirecting stdout to file: " + outFile);
            }
        }
        File errFile;
        {
            String errFileName = Config.v().errFileName;
            if (errFileName == null)
                errFile = null;
            else {
                errFile = new File(errFileName);
                System.out.println("Redirecting stderr to file: " + errFile);
            }
        }
        PrintStream outStream = null;
        PrintStream errStream = null;
        if (outFile != null) {
            outStream = new PrintStream(outFile);
            System.setOut(outStream);
        }
        if (errFile != null) {
            if (outFile != null && errFile.equals(outFile))
                errStream = outStream;
            else
                errStream = new PrintStream(errFile);
            System.setErr(errStream);
        }
        
        // run chord
        Timer timer = new Timer("chord");
        timer.init();
        String initTime = timer.getInitTimeStr();
        if (Config.v().verbose >= 0)
            System.out.println("Chord run initiated at: " + initTime);
        Program.g().build();
        Project project = Project.g();
        String[] analysisNames = Utils.toArray(Config.v().runAnalyses);
        if (analysisNames.length > 0) {
            project.run(analysisNames);
        }
        String[] relNames = Utils.toArray(Config.v().printRels);
        if (relNames.length > 0) {
            project.printRels(relNames);
        }
        timer.done();
        String doneTime = timer.getDoneTimeStr();
        if (Config.v().verbose >= 0) {
            System.out.println("Chord run completed at: " + doneTime);
            System.out.println("Total time: " + timer.getInclusiveTimeStr());
        }
        
        // cleanup
        if (outStream != null)
            outStream.close();
        if (errStream != null && errStream != outStream)
            errStream.close();
    }
}
