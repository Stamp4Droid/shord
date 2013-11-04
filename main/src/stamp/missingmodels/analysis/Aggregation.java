package stamp.missingmodels.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import stamp.missingmodels.util.FileManager;
import stamp.missingmodels.util.FileManager.FileType;
import stamp.missingmodels.util.StubModelSet;
import stamp.missingmodels.util.StubModelSet.StubModel;
import stamp.missingmodels.util.Util.MultivalueMap;
import stamp.missingmodels.viz.flow.JCFLSolverFiles.StubModelSetInputFile;


public class Aggregation {
	private static String rootPath = "experiment2_results";
	private static MultivalueMap<String,StubModel> stubModelsByApp = new MultivalueMap<String,StubModel>();
	private static Random random = new Random();
	
	public static List<String> randomize(List<String> input) {
		List<String> copy = new ArrayList<String>(input);
		List<String> output = new ArrayList<String>();
		while(copy.size() > 0) {
			int index = random.nextInt(copy.size());
			output.add(copy.get(index));
			copy.remove(index);
		}
		return output;
	}
	
	public static double[] run(List<String> appNames) {		
		Set<StubModel> accumulatedModels = new HashSet<StubModel>();
		int prevModels = 0;
		double[] result = new double[appNames.size()];
		for(int i=0; i<appNames.size(); i++) {
			String appName = appNames.get(i);
			accumulatedModels.addAll(stubModelsByApp.get(appName));
			int addedModels = accumulatedModels.size() - prevModels;
			//System.out.println("Step " + i + ": " + appName);
			//System.out.println((double)addedModels/stubModelsByApp.get(appName).size());
			result[i] = (double)addedModels/stubModelsByApp.get(appName).size();
			prevModels = accumulatedModels.size();
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		File root = new File("../" + rootPath);
		for(File appDir : root.listFiles()) {
			String[] tokens = appDir.getName().split("_");
			String appName = tokens[tokens.length-1];
			try {
				File outputDir = new File(appDir, "cfl/");
				FileManager manager = new FileManager(new File("osbert/permanent/"), outputDir, new File("osbert/scratch"), true);
				StubModelSet m = manager.read(new StubModelSetInputFile("StubModelSet.txt", FileType.OUTPUT));
				for(StubModel s : m.keySet()) {
					stubModelsByApp.add(appName, s);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		List<String> appNames = new ArrayList<String>();
		for(String appName : stubModelsByApp.keySet()) {
			appNames.add(appName);
		}
		
		//run(appNames);
		int numTrials = 100;
		double[] averageResult = new double[appNames.size()];
		for(int i=0; i<numTrials; i++) {
			double[] result = run(randomize(appNames));
			for(int j=0; j<result.length; j++) {
				averageResult[j] += result[j]/numTrials;	
			}
		}
		for(int i=0; i<averageResult.length; i++) {
			System.out.println(averageResult[i]);
		}
	}
}
