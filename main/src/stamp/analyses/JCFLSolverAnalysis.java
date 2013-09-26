package stamp.analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.NullOutputStream;

import shord.project.analyses.JavaAnalysis;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import stamp.missingmodels.analysis.Experiment;
import stamp.missingmodels.analysis.JCFLSolverRunner;
import stamp.missingmodels.analysis.JCFLSolverRunner.JCFLSolverSingle;
import stamp.missingmodels.analysis.JCFLSolverRunner.JCFLSolverStubs;
import stamp.missingmodels.analysis.JCFLSolverRunner.RelationAdder;
import stamp.missingmodels.grammars.E12;
import stamp.missingmodels.jimplesrcmapper.ChordJimpleAdapter;
import stamp.missingmodels.jimplesrcmapper.CodeStructureInfo;
import stamp.missingmodels.jimplesrcmapper.JavaToJimpleStructureConverter;
import stamp.missingmodels.jimplesrcmapper.JimpleStructureExtractor;
import stamp.missingmodels.jimplesrcmapper.Printer;
import stamp.missingmodels.util.ConversionUtils.ChordRelationAdder;
import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampOutputFile;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.StubModelSet.ModelType;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationInputFile;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationOutputFile;
import stamp.missingmodels.util.xml.XMLObject;
import stamp.missingmodels.util.xml.XMLObject.XMLContainerObject;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.AllStubInputsFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetInputFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetOutputFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetWithDataOutputFile;
import stamp.srcmap.SourceInfoSingleton;
import stamp.srcmap.sourceinfo.javasource.JavaSourceInfo;
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
	
	public static class FileRelationAdder implements RelationAdder {
		private final FileManager manager;
		
		public FileRelationAdder(FileManager manager) {
			this.manager = manager;
		}

		@Override
		public Collection<String> addEdges(Graph g, StubLookup s, StubModelSet m) {
			Set<String> relationsNotFound = new HashSet<String>();
			for(int k=0; k<g.numKinds(); k++) {
				String symbol = g.kindToSymbol(k);
				try {
					if(!g.isTerminal(k)) {
						continue;						
					}
					Set<EdgeData> edges = this.manager.read(new JCFLRelationInputFile(FileType.SCRATCH, symbol, g.kindToWeight(k)));
					for(EdgeData edge : edges) {
						if(!edge.hasLabel()) {
							g.addWeightedInputEdge(edge.from, edge.to, k, edge.weight);
						} else {
							try {
								g.addWeightedInputEdge(edge.from, edge.to, k, Integer.parseInt(edge.label), edge.weight);
							} catch(NumberFormatException e) {
								e.printStackTrace();
								throw new RuntimeException("Error printing edge: " + edge.toString());
							}
						}
					}
				} catch(IOException e) {
					if(g.isTerminal(k)) {
						relationsNotFound.add(symbol);
					}
				}
			}
			return relationsNotFound;
		}
	}
	
	public static void run(FileManager manager, RelationAdder relationAdder) {		
		// STEP 1: Set up the graph and load the stub model set if applicable.
		StubModelSet m;
		try {
			m = manager.read(new StubModelSetInputFile("StubModelSet.txt", FileType.PERMANENT));
		} catch (IOException e) {
			e.printStackTrace();
			m = new StubModelSet();
		}
		
		//j = new JCFLSolverSingle(new E12(), m);
		//j.run(E12.class, m);
		Experiment experiment = new Experiment(JCFLSolverSingle.class, E12.class);
		experiment.run(m, new StubModelSet(), relationAdder, ModelType.FALSE);
		j = experiment.j();

		// STEP 3: Output some results
		printRelCounts(j.g());
		
		Set<StampOutputFile> files = new HashSet<StampOutputFile>();
		for(int i=0; i<j.g().numKinds(); i++) {
			if(j.g().isTerminal(i)) {
				files.add(new JCFLRelationOutputFile(FileType.SCRATCH, j.g(), j.g().kindToSymbol(i), false));
			}
		}
		files.add(new JCFLRelationOutputFile(FileType.OUTPUT, j.g(), "Src2Sink", true));
		files.add(new AllStubInputsFile(j.g(), j.s()));
		//files.add(new StubInputsFile(j.g(), j.s()));
		files.add(new StubModelSetWithDataOutputFile<Pair<ModelType,Integer>>(experiment.getAllProposedModels()));
		//files.addAll(FlowWriter.viz(j.g, j.s));
		m.putAll(experiment.getAllProposedModels());
		files.add(new StubModelSetOutputFile(m, "StubModelSet.txt", FileType.PERMANENT));
		files.add(experiment);
		try {
			for(StampOutputFile file : files) {
				manager.write(file);
			}
		} catch(IOException e) { e.printStackTrace(); }
	}
	
	public static FileManager getFileManager(String stampDirectory) {
		// STEP 0: Set up the file manager.
		File permanentDir = new File(stampDirectory + "/../../osbert/permanent/");
		File outputDir = new File(stampDirectory + File.separator + "cfl");
		File scratchDir = new File(stampDirectory + File.separator + "/../../osbert/scratch/" + outputDir.getParentFile().getName());

		FileManager manager = null;;
		try {
			System.out.println("STAMP OUTPUT DIRECTORY: " + new File(stampDirectory).getCanonicalPath());
			System.out.println("Using permanent directory: " + permanentDir.getCanonicalPath());
			System.out.println("Using output directory: " + outputDir.getCanonicalPath());
			System.out.println("Using scratch directory: " + scratchDir.getCanonicalPath());
			manager = new FileManager(permanentDir, outputDir, scratchDir, true);
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up the file manager!");
		}
		return manager;
	}
	
	@Override public void run() {
		run(getFileManager(System.getProperty("stamp.out.dir")), new ChordRelationAdder());
		
		try {
			// SET UP SCRATCH DIRECTORY
			String stampDirectory = System.getProperty("stamp.out.dir");
			File outputDir = new File(stampDirectory + File.separator + "cfl");
			File scratchDir = new File(stampDirectory + File.separator + "/../../osbert/scratch/" + outputDir.getParentFile().getName());
			String outputPath = scratchDir.getCanonicalPath() + "/jimple/";
			
			// PRINT JIMPLE
			JimpleStructureExtractor jse = new JimpleStructureExtractor();
			new Printer(jse).printAll(outputPath);
			
			// GET STRUCTURE AND PRINT
			CodeStructureInfo codeInfo = jse.getCodeStructureInfo();
			System.out.println("PRINTING CLASS INFO:");
			for(SootClass cl : codeInfo.getClasses()) {
				System.out.println(cl.toString() + ": " + codeInfo.getClassInfo(cl).toString());
			}
			System.out.println("PRINTING METHOD INFO:");
			for(SootMethod m : codeInfo.getMethods()) {
				System.out.println(m.toString() + ": " + codeInfo.getMethodInfo(m).toString());
			}
			
			// CONVERT STRUCTURE TO XML OBJECT
			JavaSourceInfo sourceInfo = SourceInfoSingleton.getJavaSourceInfo();
			JavaToJimpleStructureConverter jtj = new JavaToJimpleStructureConverter(sourceInfo, codeInfo);

			for(SootClass cl : Scene.v().getClasses()) {
				System.out.println("READING: " + cl.getName());
				
				// GET THE XML OBJECT FILE PATH
				File objectFile;
				try {
					objectFile = new File(sourceInfo.srcMapFile(sourceInfo.filePath(cl)).getCanonicalPath().replace(".xml", ".obj"));
				} catch(NullPointerException e) {
					System.out.println("FAILED TO READ: " + cl.getName());
					e.printStackTrace();
					continue;
				}
				
				// READ IN THE OBJECT
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectFile));
				XMLContainerObject object;
				try {
					object = (XMLContainerObject)ois.readObject();
				} catch(ClassNotFoundException e) {
					System.out.println("FAILED TO READ: " + cl.getName());
					e.printStackTrace();
					continue;
				} finally {
					ois.close();
				}
				
				// GET THE OUTPUT FILE PATH
				StringBuffer b = new StringBuffer();
				//b.append(outputPath);
				b.append(stampDirectory + "/jimple/"); 
				b.append(cl.getName());
				b.append(".xml");
				String xmlOutputPath = b.toString();
				
				// CONVERT THE OBJECT
				//jtj.convert(object);
				Collection<XMLObject> objects = new HashSet<XMLObject>(); objects.add(object);
				ChordJimpleAdapter cja = new ChordJimpleAdapter(sourceInfo, objects);
				Printer printer = new Printer(cja.toJimpleVisitor(codeInfo));
				printer.printTo(cl, new NullOutputStream());
				object = cja.getResults().get(cl);
								
				// WRITE THE XML OBJECT
				File objectOutputFile = new File(xmlOutputPath);
				objectOutputFile.getParentFile().mkdirs();
				System.out.println("PRINTING TO: " + objectOutputFile.getCanonicalPath());
				PrintWriter pw = new PrintWriter(new FileOutputStream(objectOutputFile));
				pw.println(object.toString());
				pw.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String appDirectory = "../stamp_output/_home_obastani_Documents_projects_stamp_stamptest_SymcApks_24feff7f70fc1f4369069d64a9998d43.apk/";
		FileManager manager = getFileManager(appDirectory);
		run(manager, new FileRelationAdder(manager));
	}
}
