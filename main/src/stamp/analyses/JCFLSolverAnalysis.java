package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.analysis.Experiment;
import stamp.missingmodels.analysis.JCFLSolverRunner;
import stamp.missingmodels.analysis.JCFLSolverRunner.JCFLSolverSingle;
import stamp.missingmodels.analysis.JCFLSolverRunner.JCFLSolverStubs;
import stamp.missingmodels.grammars.E12;
import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampOutputFile;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.AllStubInputsFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubInputsFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetInputFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetWithDataOutputFile;
import chord.project.Chord;

/*
 * An analysis that runs the JCFLSolver to do the taint analysis.
 */

@Chord(name = "jcflsolver")
public class JCFLSolverAnalysis extends JavaAnalysis {	
	private static JCFLSolverRunner j = new JCFLSolverStubs();
	
	public static Graph g() {
		if(j == null) return null;
	    return j.g();
	}
	
	public static StubLookup s() {
		if(j == null) return null;
	    return j.s();
	}

	public static void printRelCounts(Graph g) {
		System.out.println("Printing final relation counts...");
		for(int k=0; k<g.numKinds(); k++) {
			System.out.println(g.kindToSymbol(k) + ": " + g.getEdges(k).size());
		}
	}
	
	@Override public void run() {
		// STEP 0: Set up the file manager.
		System.out.println("STAMP OUTPUT DIRECTORY: " + System.getProperty("stamp.out.dir"));
		File permanentDir = new File(System.getProperty("stamp.out.dir") + "/../../temp/");
		File outputDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");
		File scratchDir = new File(System.getProperty("stamp.out.dir") + File.separator + "cfl");

		FileManager manager = null;;
		try {
			manager = new FileManager(permanentDir, outputDir, scratchDir, true);
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up the file manager!");
		}
		
		// STEP 1: Set up the graph and load the stub model set if applicable.
		StubModelSet m;
		try {
			//m = manager.read(new StubModelSetWithDataInputFile<Pair<Integer,Integer>>("StubModelSet.txt", ProposedStubModelSet.class));
			//m = manager.read(new StubModelSetInputFile("StubModelSet012.txt"));
			//m = manager.read(new StubModelSetInputFile("StubModelSet018.txt"));
			//m = manager.read(new StubModelSetInputFile("StubModelSet0ac.txt"));
			//m = manager.read(new StubModelSetInputFile("StubModelSet101.txt"));
			//m = manager.read(new StubModelSetInputFile("StubModelSet197.txt"));
			//m = manager.read(new StubModelSetInputFile("StubModelSet1c2.txt"));
			//m = manager.read(new StubModelSetInputFile("StubModelSet24f.txt"));
			//m = manager.read(new StubModelSetInputFile("StubModelSet2ef.txt"));
			m = manager.read(new StubModelSetInputFile("StubModelSet.txt"));
		} catch (IOException e) {
			e.printStackTrace();
			m = new StubModelSet();
		}
		
		//j = new JCFLSolverSingle(new E12(), m);
		//j.run(E12.class, m);
		Experiment experiment = new Experiment(JCFLSolverSingle.class, E12.class);
		experiment.run(m, m, 2);
		j = experiment.j();

		// STEP 3: Output some results
		printRelCounts(j.g());
		
		Set<StampOutputFile> files = new HashSet<StampOutputFile>();
		for(int i=0; i<j.g().numKinds(); i++) {
			if(j.g().isTerminal(i)) {
				files.add(new JCFLRelationFile(FileType.OUTPUT, j.g(), j.g().kindToSymbol(i), false));
			}
		}
		files.add(new JCFLRelationFile(FileType.OUTPUT, j.g(), "Src2Sink", true));
		files.add(new AllStubInputsFile(j.g(), j.s()));
		files.add(new StubInputsFile(j.g(), j.s()));
		files.add(new StubModelSetWithDataOutputFile<Pair<Integer,Integer>>(experiment.getAllProposedModels()));
		//files.addAll(FlowWriter.viz(j.g, j.s));
		try {
			for(StampOutputFile file : files) {
				manager.write(file);
			}
		} catch(IOException e) { e.printStackTrace(); }
	}
}
