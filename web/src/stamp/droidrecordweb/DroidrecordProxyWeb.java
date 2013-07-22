package stamp.droidrecordweb;

import edu.stanford.droidrecord.logreader.BinLogReader;
import edu.stanford.droidrecord.logreader.CoverageReport;
import edu.stanford.droidrecord.logreader.EventLogStream;
import edu.stanford.droidrecord.logreader.analysis.CallArgumentValueAnalysis;
import edu.stanford.droidrecord.logreader.events.util.ParseUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DroidrecordProxyWeb {
    
    private boolean available;
    private BinLogReader logReader = null;
    private List<String> binLogFiles;
    
    private CoverageReport catchedCoverage = null;
    private CallArgumentValueAnalysis catchedCAVAnalysis = null;
    
    private void getBinLogFiles(String binLogFile) {
        // Go from dir/droidrecord.log.bin to gathering all 
        // dir/droidrecord.log.bin.runX.threadY files
        try {
            binLogFiles = new ArrayList<String>();
            int dirEnd = binLogFile.lastIndexOf('/');
            String dirPath = binLogFile.substring(0,dirEnd);
            File folder = new File(dirPath);
            String canonicalBinLogFile = 
                folder.getCanonicalPath() + binLogFile.substring(dirEnd);
            Pattern traceFilePattern = 
                Pattern.compile("^" + canonicalBinLogFile + "\\.run\\d*\\.thread\\d*$");
            System.out.println("Droidrecord bin log pattern: " + traceFilePattern.toString());
            File[] files = folder.listFiles();
            for(File file : files) {
                if(!file.exists()) continue;
                String filename = file.getCanonicalPath();
                System.out.println("Inspecting file: " + filename);
                Matcher m = traceFilePattern.matcher(filename);
                if(m.matches()) {
                    binLogFiles.add(filename);
                    System.out.println("Loaded binary droidrecord log: " + filename);
                }
            }
        } catch(IOException e) {
            throw new Error(e);
        }
    }

    public DroidrecordProxyWeb(String templateLogFile, String binLogFile) {
        if(templateLogFile == null || binLogFile == null || 
           templateLogFile.equals("") || binLogFile.equals("") || 
            !(new File(templateLogFile)).exists()) {
            available = false;
        } else {
            logReader = new BinLogReader(templateLogFile);
            getBinLogFiles(binLogFile);
            available = true;
        }
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public CoverageReport getCoverage() {
        if(!isAvailable()) {
            throw new Error("Droidrecord log not available!");
        } else if(catchedCoverage == null){
            logReader.parseLogs(binLogFiles).readAll();
            catchedCoverage = logReader.getCumulativeCoverageReport();;
        }
        return catchedCoverage;
    }
    
    public CallArgumentValueAnalysis getCallArgumentValueAnalysis() {
        if(!isAvailable()) {
            throw new Error("Droidrecord log not available!");
        } else if(catchedCAVAnalysis == null){
            catchedCAVAnalysis = new CallArgumentValueAnalysis(
                logReader.parseLogs(binLogFiles));
            catchedCAVAnalysis.run();
        }
        return catchedCAVAnalysis;
    }
    
    public static String chordToSootMethodSignature(String s) {
        return ParseUtil.chordToSootMethodSignature(s);
    }
}
