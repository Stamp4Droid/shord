package shord.project;

import shord.util.Utils;

/**
 * System properties recognized by Chord.
 *
 * @author Mayur Naik (mhn@cs.stanford.edu)
 */
public class Config {
	private Config() {}
	private static Config config = null;
	public static Config v() {
		if(config == null) {
			config = new Config();
		}
		return config;
	}
	
    // basic properties about program being analyzed (its main class, classpath, command line args, etc.)
    public final String workDirName = System.getProperty("chord.work.dir");
    public final String mainClassName = System.getProperty("chord.main.class");
    
    // path properties
    public final String chordClassPath = System.getProperty("chord.class.path");
    public final String chordModelPath = System.getProperty("chord.model.path");
    
    // project properties
    public final String mainDirName = System.getProperty("chord.main.dir");
    public final String javaAnalysisPathName = System.getProperty("chord.java.analysis.path");
    public final String dlogAnalysisPathName = System.getProperty("chord.dlog.analysis.path");
    
    // properties dictating what gets computed/printed by Chord
    public final String runAnalyses = System.getProperty("chord.run.analyses", "");
    public final String printRels = System.getProperty("chord.print.rels", "");
    
    // properties concerning BDDs
    public final boolean saveDomMaps = Utils.buildBoolProperty("chord.save.maps", true);
    public final int verbose = Integer.getInteger("chord.verbose", 2);
    public final boolean useBuddy = Utils.buildBoolProperty("chord.use.buddy", false);
    public final String bddbddbMaxHeap = System.getProperty("chord.bddbddb.max.heap", "4096m");
    
    // properties specifying names of Chord's output files and directories
    public String outDirName = System.getProperty("chord.out.dir", workRel2Abs("chord_output"));
    public final String outFileName = System.getProperty("chord.out.file", outRel2Abs("log.txt"));
    public final String errFileName = System.getProperty("chord.err.file", outRel2Abs("log.txt"));    
    public final String bddbddbWorkDirName = System.getProperty("chord.bddbddb.work.dir", outRel2Abs("bddbddb"));
    
    public String outRel2Abs(String fileName) {
        return (fileName == null) ? null : Utils.getAbsolutePath(outDirName, fileName);
    }

    public String workRel2Abs(String fileName) {
        return (fileName == null) ? null : Utils.getAbsolutePath(workDirName, fileName);
    }
}
