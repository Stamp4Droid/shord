package stamp.missingmodels.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.processor.LogReader.Processor;
import stamp.missingmodels.util.Util.Pair;

/**
 * Retrieve LP running time, total cut size, and lines of code from loc and log files 
 * @author obastani
 *
 */
public class TestsProcessor implements Processor {
	private final List<String> appNames = new ArrayList<String>();
	
	private boolean isLpRunningTime = false;
	private final Map<String,Integer> lpRunningTimes = new HashMap<String,Integer>();
	
	private final Map<String,Integer> numVariables = new HashMap<String,Integer>();
	private final Map<String,Integer> numConstraints = new HashMap<String,Integer>();
	
	private final Map<String,Integer> numFlowEdges = new HashMap<String,Integer>();
	private final Map<String,Integer> numBaseEdges = new HashMap<String,Integer>();
	private final Map<String,Map<Integer,Integer>> numCutEdges = new HashMap<String,Map<Integer,Integer>>();
	
	private final Map<String,Integer> appLinesOfCode = new HashMap<String,Integer>();
	private final Map<String,Integer> frameworkLinesOfCode = new HashMap<String,Integer>();
	
	@Override
	public void process(String appName, String line) {
		if(this.isLpRunningTime && line.startsWith("Done in")) {
			this.lpRunningTimes.put(appName, Integer.parseInt(line.split(" ")[2].split("ms")[0]));
			this.isLpRunningTime = false;
		} else if(line.startsWith("Solving LP")) {
			this.isLpRunningTime = true;
		} else if(line.startsWith("Num initial edges:")) {
			this.numFlowEdges.put(appName, Integer.parseInt(line.split(" ")[3].trim()));
		} else if(line.startsWith("Num base edges:")) {
			this.numBaseEdges.put(appName, Integer.parseInt(line.split(" ")[3].trim()));
		} else if(line.startsWith("Number of variables:")) {
			this.numVariables.put(appName, Integer.parseInt(line.split(" ")[3].trim()));
		} else if(line.startsWith("Number of constraints:")) {
			this.numConstraints.put(appName, Integer.parseInt(line.split(" ")[3].trim()));
		} else if(line.startsWith("total cut")) {
			String[] tokens = line.split(" ");
			int cutNum = Integer.parseInt(tokens[2].trim().substring(0, tokens[2].length()-1));
			int numCuts = Integer.parseInt(tokens[3].trim());
			Map<Integer,Integer> cutMap = this.numCutEdges.get(appName);
			if(cutMap == null) {
				cutMap = new HashMap<Integer,Integer>();
				this.numCutEdges.put(appName, cutMap);
			}
			cutMap.put(cutNum, numCuts);
		}
	}

	@Override
	public void process(String appName, int appLinesOfCode, int frameworkLinesOfCode) {
		this.appLinesOfCode.put(appName, appLinesOfCode);
		this.frameworkLinesOfCode.put(appName, frameworkLinesOfCode);
	}

	@Override
	public void finishProcessing(String appName) {		
		if(this.lpRunningTimes.get(appName) != null
				&& this.numVariables.get(appName) != null
				&& this.numConstraints.get(appName) != null
				&& this.numFlowEdges.get(appName) != null
				&& this.numBaseEdges.get(appName) != null
				&& this.numCutEdges.get(appName) != null
				&& this.appLinesOfCode.get(appName) != null
				&& this.frameworkLinesOfCode != null) {
			this.appNames.add(appName);
		}
	}
	
	public List<String> getAppNames() {
		Collections.sort(this.appNames, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if(appLinesOfCode.get(o1) == appLinesOfCode.get(o2)) {
					return 0;
				}
				if(appLinesOfCode.get(o1) == null) {
					return 1;
				}
				if(appLinesOfCode.get(o2) == null) {
					return -1;
				}
				if(appLinesOfCode.get(o1) < appLinesOfCode.get(o2)) {
					return 1;
				}
				return -1;				
			}
		});
		return Collections.unmodifiableList(this.appNames);
	}
	
	private int getNumCuts() {
		int numCuts = 0;
		for(String appName : this.getAppNames()) {
			int curNumCuts = this.numCutEdges.get(appName).size();
			if(curNumCuts > numCuts) {
				numCuts = curNumCuts;
			}
		}
		return numCuts;
	}
	
	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(",");
		sb.append("LP Solve Time").append(",");
		sb.append("# Variables").append(",");
		sb.append("# Constraints").append(",");
		sb.append("# Flow Edges").append(",");
		sb.append("# Base Edges").append(",");
		for(int i=0; i<this.getNumCuts(); i++) {
			sb.append("# Cut Edges " + i).append(",");
		}
		sb.append("App Lines of Code").append(",");
		sb.append("Framework Lines of Code");
		return sb.toString();		
	}
	
	public String getInfoFor(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(",");
		sb.append(this.lpRunningTimes.get(appName)).append(",");
		sb.append(this.numVariables.get(appName)).append(",");
		sb.append(this.numConstraints.get(appName)).append(",");
		sb.append(this.numFlowEdges.get(appName)).append(",");
		sb.append(this.numBaseEdges.get(appName)).append(",");
		int numCuts = this.getNumCuts();
		for(int i=0; i<numCuts; i++) {
			sb.append(this.numCutEdges.get(appName).get(i)).append(",");
		}
		sb.append(this.appLinesOfCode.get(appName)).append(",");
		sb.append(this.frameworkLinesOfCode.get(appName));
		return sb.toString();
	}
	
	public static void main(String[] args) {
		TestsProcessor tp = new TestsProcessor();
		new LogReader("../stamp_output", tp).run();
		System.out.println(tp.getHeader());
		for(String appName : tp.getAppNames()) {
			System.out.println(tp.getInfoFor(appName));
		}
	}
}
