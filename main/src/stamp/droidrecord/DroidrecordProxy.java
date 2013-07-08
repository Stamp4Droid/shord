package stamp.droidrecord;

import edu.stanford.droidrecord.logreader.EventLogStream;
import edu.stanford.droidrecord.logreader.BinLogReader;

public class DroidrecordProxy {
    
    private static DroidrecordProxy singleton = null;
    
    private boolean available;
    private BinLogReader logReader = null;
    private String binLogFile;

    private DroidrecordProxy() {
        String templateLogFile = System.getProperty("stamp.droidrecord.logfile.template");
        binLogFile = System.getProperty("stamp.droidrecord.logfile.bin");
        if(templateLogFile.equals("") || binLogFile.equals("")) {
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
}