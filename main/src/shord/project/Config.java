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
    
    // project properties
    public final String mainDirName = System.getProperty("chord.main.dir");
    public final String javaAnalysisPathName = System.getProperty("chord.java.analysis.path");
    public final String dlogAnalysisPathName = System.getProperty("chord.dlog.analysis.path");
    
    // properties dictating what gets computed/printed by Chord
    public final String runAnalyses = System.getProperty("chord.run.analyses", "");
    public final String printRels = System.getProperty("chord.print.rels", "");
    public final boolean saveDomMaps = Utils.buildBoolProperty("chord.save.maps", true);
    // Determines verbosity level of Chord:
    // 0 => silent
    // 1 => print task/process enter/leave/time messages and sizes of computed doms/rels
    //      bddbddb: print sizes of relations output by solver
    // 2 => all other messages in Chord
    //      bddbddb: print bdd node resizing messages, gc messages, and solver stats (e.g. how long each iteration took)
    // 3 => bddbddb: noisy=yes for solver
    // 4 => bddbddb: tracesolve=yes for solver
    // 5 => bddbddb: fulltravesolve=yes for solver
    public final int verbose = Integer.getInteger("chord.verbose", 2);

    // properties concerning BDDs
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
