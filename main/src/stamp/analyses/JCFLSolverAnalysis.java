package stamp.analyses;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import shord.project.analyses.JavaAnalysis;
import stamp.missingmodels.analysis.Experiment;
import stamp.missingmodels.analysis.JCFLSolverRunner;
import stamp.missingmodels.analysis.JCFLSolverRunner.JCFLSolverSingle;
import stamp.missingmodels.analysis.JCFLSolverRunner.JCFLSolverStubs;
import stamp.missingmodels.analysis.JCFLSolverRunner.RelationAdder;
import stamp.missingmodels.analysis.ModelClassifier.ModelInfo;
import stamp.missingmodels.grammars.G;
import stamp.missingmodels.ml.Classifier;
import stamp.missingmodels.util.ConversionUtils.ChordRelationAdder;
import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.FileManager.StampOutputFile;
import stamp.missingmodels.util.StubLookup;
import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.StubModelSet.ModelType;
import stamp.missingmodels.util.StubModelSet.StubModel;
import stamp.missingmodels.util.Util.Pair;
import stamp.missingmodels.util.jcflsolver.Edge;
import stamp.missingmodels.util.jcflsolver.EdgeData;
import stamp.missingmodels.util.jcflsolver.Graph;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationInputFile;
import stamp.missingmodels.util.viz.jcflsolver.JCFLRelationOutputFile;
import stamp.missingmodels.viz.flow.JCFLRelationOutputFileNew;
import stamp.missingmodels.viz.flow.JCFLRelationOutputFileNew.TupleConverter;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.AllStubInputsFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetInputFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetOutputFile;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetWithDataOutputFile;
import chord.project.Chord;

/*
 * An analysis that runs the JCFLSolver to do the taint analysis.
 */
@Chord(name = "jcflsolver")
public class JCFLSolverAnalysis extends JavaAnalysis {
	private static JCFLSolverRunner j = new JCFLSolverStubs();
	
	public static void runStubModelClassifier(StubModelSet m) {
		Random random = new Random();
		int length = 5;
		
		// STEP 1: Build the training and test sets.
		Map<StubModel,Boolean> trainingSet = new HashMap<StubModel,Boolean>();
		Map<StubModel,Boolean> testSet = new HashMap<StubModel,Boolean>();
		for(StubModel model : m.keySet()) {
			boolean b = random.nextDouble() < 0.7;
			if(m.get(model) != null) {
				System.out.println(model.toString() + ": " + m.get(model));
			}
			try {
				ModelInfo info = new ModelInfo(model);
			} catch(Exception e) {
				e.printStackTrace();
				continue;
			}
			if(m.get(model) == ModelType.FALSE) {
				if(b) {
					trainingSet.put(model, false);
				} else {
					testSet.put(model, false);
				}
			} else if(m.get(model) == ModelType.TRUE) {
				if(b) {
					trainingSet.put(model, true);
				} else {
					testSet.put(model, true);
				}
			}
		}
		
		// STEP 2: Construct the features.
		double[][] xTraining = new double[trainingSet.keySet().size()][length];
		double[] yTraining = new double[trainingSet.keySet().size()];
		int counter=0;
		for(Map.Entry<StubModel,Boolean> entry : trainingSet.entrySet()) {
			xTraining[counter] = new ModelInfo(entry.getKey()).featurize();
			yTraining[counter] = entry.getValue() ? 1.0 : 0.0;
			counter++;
		}
		double[][] xTest = new double[testSet.keySet().size()][length];
		double[] yTest = new double[testSet.keySet().size()];
		counter=0;
		for(Map.Entry<StubModel,Boolean> entry : testSet.entrySet()) {
			xTest[counter] = new ModelInfo(entry.getKey()).featurize();
			yTest[counter] = entry.getValue() ? 1.0 : 0.0;
			counter++;
		}
		
		// STEP 3: Predict and get accuracy
		double[] theta = Classifier.maximumLikelihood(xTraining, yTraining, 0.01);
		boolean[] yTrainingHat = Classifier.predictLabel(theta, xTraining, 0.5);
		boolean[] yTestHat = Classifier.predictLabel(theta, xTest, 0.5);
		double trainingAccuracy = 0.0; 
		for(int i=0; i<yTraining.length; i++) {
			if((yTraining[i] > 0.5) == yTrainingHat[i]) { 
				trainingAccuracy += 1.0;
			}
		}
		trainingAccuracy /= yTraining.length;
		double testAccuracy = 0.0; 
		for(int i=0; i<yTest.length; i++) {
			if((yTest[i] > 0.5) == yTestHat[i]) { 
				testAccuracy += 1.0;
			}
		}
		testAccuracy /= yTest.length;
		System.out.println("Training accuracy: " + trainingAccuracy);
		System.out.println("Test accuracy: " + testAccuracy);
		
	}
	
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
			System.out.println("HERE");
			for(Map.Entry<StubModel,ModelType> entry : m.entrySet()) {
				System.out.println(entry.getKey().toString() + ": " + entry.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
			m = new StubModelSet();
		}
		runStubModelClassifier(m);
		
		//j = new JCFLSolverSingle(new E12(), m);
		//j.run(E12.class, m);
		//Experiment experiment = new Experiment(JCFLSolverSingle.class, E12.class);
		File outputDir = manager.getDirectory(FileType.OUTPUT);
		String appDir = outputDir.getParentFile().getName();
		Experiment experiment = new Experiment(JCFLSolverSingle.class, G.class, appDir);
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
		files.add(new JCFLRelationOutputFileNew("Flow3", j.g(), new TupleConverter() {
			@Override
			public int[] convert(Edge edge) {
				String srcString = edge.from.getName().substring(2);
				String sinkString = edge.to.getName().substring(2);
				try {
					int src = Integer.parseInt(srcString);
					int sink = Integer.parseInt(sinkString);
					return new int[]{src,sink};
				} catch(Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Failed to parse src-sink pair: (" + srcString + "," + sinkString + ")!");
				}
			}
		}));
		files.add(new JCFLRelationOutputFileNew("LabelRef3", j.g(), new TupleConverter() {
			@Override
			public int[] convert(Edge edge) {
				String srcString = edge.from.getName().substring(2);
				String[] varStrings = edge.to.getName().substring(1).split("_");
				try {
					int src = Integer.parseInt(srcString);
					int var = Integer.parseInt(varStrings[0]);
					int ctxt = Integer.parseInt(varStrings[1]);
					return new int[]{ctxt,src,var};
				} catch(Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Failed to parse src-ref pair: (" + srcString + "," + varStrings[0] + "_" + varStrings[1] + ")!");
				}
			}
		}));
		files.add(new JCFLRelationOutputFileNew("LabelPrim3", j.g(), new TupleConverter() {
			@Override
			public int[] convert(Edge edge) {
				String srcString = edge.from.getName().substring(2);
				String[] varStrings = edge.to.getName().substring(1).split("_");
				try {
					int src = Integer.parseInt(srcString);
					int var = Integer.parseInt(varStrings[0]);
					int ctxt = Integer.parseInt(varStrings[1]);
					return new int[]{ctxt,src,var};
				} catch(Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Failed to parse src-prim pair: (" + srcString + "," + varStrings[0] + "_" + varStrings[1] + ")!");
				}
			}
		}));
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
	}
	
	public static void main(String[] args) {
		String appDirectory = "../stamp_output/_home_obastani_Documents_projects_stamp_stamptest_SymcApks_24feff7f70fc1f4369069d64a9998d43.apk/";
		FileManager manager = getFileManager(appDirectory);
		run(manager, new FileRelationAdder(manager));
	}
}
