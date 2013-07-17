package stamp.droidrecord;

import edu.stanford.droidrecord.logreader.BinLogReader;
import edu.stanford.droidrecord.logreader.CoverageReport;
import edu.stanford.droidrecord.logreader.EventLogStream;
import java.io.File;

public class DroidrecordProxy {
    
    private static DroidrecordProxy singleton = null;
    
    private boolean available;
    private BinLogReader logReader = null;
    private String binLogFile;
    
    private CoverageReport catchedCoverage = null;

    private DroidrecordProxy() {
        String templateLogFile = System.getProperty("stamp.droidrecord.logfile.template");
        binLogFile = System.getProperty("stamp.droidrecord.logfile.bin");
        if(templateLogFile.equals("") || binLogFile.equals("") || 
            !(new File(templateLogFile)).exists() ||
            !(new File(binLogFile)).exists()) {
            available = false;
        } else {
            logReader = new BinLogReader(templateLogFile);
            available = true;
        }
    }
    
    public static DroidrecordProxy g() {
        if(singleton == null) {
            singleton = new DroidrecordProxy();
        }
        return singleton;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public StampCallArgumentValueAnalysis getCallArgumentValueAnalysis() {
        if(!isAvailable()) {
            throw new Error("Droidrecord log not available!");
        }
        EventLogStream els = logReader.parseLog(binLogFile);
        return new StampCallArgumentValueAnalysis(els);
    }
    
    public CoverageReport getCoverage() {
        if(!isAvailable()) {
            throw new Error("Droidrecord log not available!");
        } else if(catchedCoverage != null){
            return catchedCoverage;
        }
        logReader.parseLog(binLogFile).readAll();
        return logReader.getCumulativeCoverageReport();
    }
}