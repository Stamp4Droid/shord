package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.grammars.E12;
import stamp.missingmodels.util.ConversionUtils;
import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampOutputFile;
import stamp.missingmodels.util.Relation;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubLookup.StubModelSet;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.AllStubInputsFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubInputsFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetInputFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetOutputFile;
import chord.project.Chord;

/*
 * An analysis that runs the JCFLSolver to do the taint analysis.
 */

@Chord(name = "jcflsolver")
public class JCFLSolverAnalysis extends JavaAnalysis {
	/*
	 * The following code is for running the JCFLSolver analysis.
	 */
	private void fillTerminalEdges(Graph g, StubLookup stubLookup, StubModelSet stubModelSet) {
		for(int k=0; k<g.numKinds(); k++) {
			if(g.isTerminal(k)) {
				if(ConversionUtils.getChordRelationsFor(g.kindToSymbol(k)).isEmpty()) {
					System.out.println("No edges found for relation " + g.kindToSymbol(k) + "...");
				}
				for(Relation rel : ConversionUtils.getChordRelationsFor(g.kindToSymbol(k))) {
					rel.addEdges(g.kindToSymbol(k), g, stubLookup, stubModelSet);
				}
			}
		}
	}
	
	private static Graph g = new E12();
	private static StubLookup s = new StubLookup();;;
	private static StubModelSet m;
	
	public static Graph g() {
	    return g;
	}
	
	public static StubLookup s() {
	    return s;
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
		
		// STEP 1: Set up the graph, stub lookup, and load the stub model set if applicable.
		try {
			m = manager.read(new StubModelSetInputFile());
		} catch (IOException e) {
			e.printStackTrace();
			m = new StubModelSet();
		}
		
		// STEP 2: Fill the edges into the graph and run the algorithm.
		fillTerminalEdges(g, s, m);
		g.algo.process();

		// STEP 3: Output some results
		printRelCounts(g);
		
		Set<StampOutputFile> files = new HashSet<StampOutputFile>();
		for(int i=0; i<g.numKinds(); i++) {
			if(g.isTerminal(i)) {
				files.add(new JCFLRelationFile(FileType.OUTPUT, g, g.kindToSymbol(i), false));
			}
		}
		files.add(new JCFLRelationFile(FileType.OUTPUT, g, "Src2Sink", true));
		files.add(new AllStubInputsFile(g, s));
		files.add(new StubInputsFile(g, s));
		files.add(new StubModelSetOutputFile(new StubModelSet(g, s)));
		//files.addAll(FlowWriter.viz(g, s));
		try {
			for(StampOutputFile file : files) {
				manager.write(file);
			}
		} catch(IOException e) { e.printStackTrace(); }
	}
}
