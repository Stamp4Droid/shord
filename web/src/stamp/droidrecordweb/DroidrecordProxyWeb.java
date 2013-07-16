package stamp.droidrecordweb;

import edu.stanford.droidrecord.logreader.BinLogReader;
import edu.stanford.droidrecord.logreader.CoverageReport;
import edu.stanford.droidrecord.logreader.EventLogStream;
import edu.stanford.droidrecord.logreader.events.util.ParseUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DroidrecordProxyWeb {
    
    private boolean available;
    private BinLogReader logReader = null;
    private String binLogFile;
    
    private CoverageReport catchedCoverage = null;

    public DroidrecordProxyWeb(String templateLogFile, String binLogFile) {
        this.binLogFile = binLogFile;
        if(templateLogFile == null || binLogFile == null || 
           templateLogFile.equals("") || binLogFile.equals("") || 
            !(new File(templateLogFile)).exists() ||
            !(new File(binLogFile)).exists()) {
            available = false;
        } else {
            logReader = new BinLogReader(templateLogFile);
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
            logReader.parseLog(binLogFile).readAll();
            catchedCoverage = logReader.getCumulativeCoverageReport();;
        }
        return catchedCoverage;
    }
    
    public static String chordToSootMethodSignature(String s) {
        return ParseUtil.chordToSootMethodSignature(s);
    }
}
