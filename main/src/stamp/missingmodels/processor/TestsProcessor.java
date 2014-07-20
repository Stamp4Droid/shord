package stamp.missingmodels.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.processor.LogReader.Processor;
import stamp.missingmodels.util.Util.MultivalueMap;

/**
 * Retrieve LP running time, total cut size, and lines of code from loc and log files 
 * @author obastani
 *
 */
public class TestsProcessor implements Processor {
	private final List<String> appNames = new ArrayList<String>();
	
	private boolean isLpRunningTime = false;
	private double currentCoverage = -1.0;
	private final Map<String,Map<Double,Integer>> lpRunningTimes = new HashMap<String,Map<Double,Integer>>();
	
	private final Map<String,Map<Double,Integer>> numVariables = new HashMap<String,Map<Double,Integer>>();
	private final Map<String,Map<Double,Integer>> numConstraints = new HashMap<String,Map<Double,Integer>>();
	
	private final Map<String,Map<Double,Integer>> numFlowEdges = new HashMap<String,Map<Double,Integer>>();
	private final Map<String,Map<Double,Integer>> numBaseEdges = new HashMap<String,Map<Double,Integer>>();
	private final Map<String,Map<Double,Map<Integer,Integer>>> numCutEdges = new HashMap<String,Map<Double,Map<Integer,Integer>>>();
	
	private final MultivalueMap<String,Double> coverages = new MultivalueMap<String,Double>();
	
	private final Map<String,Integer> linesOfCode = new HashMap<String,Integer>();
	
	private boolean isZerothCutEdge = false;
	private String zerothCutEdgeCaller = null;
	private final Map<String,MultivalueMap<Double,String>> zerothCut = new HashMap<String,MultivalueMap<Double,String>>();
	
	@Override
	public void startProcessing(String appName) {
		this.currentCoverage = -1.0;
		
		this.lpRunningTimes.put(appName, new HashMap<Double,Integer>());
		
		this.numVariables.put(appName, new HashMap<Double,Integer>());
		this.numConstraints.put(appName, new HashMap<Double,Integer>());
		
		this.numFlowEdges.put(appName, new HashMap<Double,Integer>());
		this.numBaseEdges.put(appName, new HashMap<Double,Integer>());
		this.numCutEdges.put(appName, new HashMap<Double,Map<Integer,Integer>>());
		
		this.zerothCut.put(appName, new MultivalueMap<Double,String>());
	}
	
	@Override
	public void process(String appName, String line) {
		if(line.startsWith("Running method coverage:")) {
			this.currentCoverage = Double.parseDouble(line.split(" ")[3].trim());
			this.coverages.add(appName, this.currentCoverage);
		} else if(this.isLpRunningTime && line.startsWith("Done in")) {
			this.lpRunningTimes.get(appName).put(this.currentCoverage, Integer.parseInt(line.split(" ")[2].split("ms")[0]));
			this.isLpRunningTime = false;
		} else if(line.startsWith("Solving LP")) {
			this.isLpRunningTime = true;
		} else if(line.startsWith("Num initial edges:")) {
			if(this.numFlowEdges.get(appName).get(this.currentCoverage) == null) {
				this.numFlowEdges.get(appName).put(this.currentCoverage, Integer.parseInt(line.split(" ")[3].trim()));
			}
		} else if(line.startsWith("Num base edges:")) {
			if(this.numBaseEdges.get(appName).get(this.currentCoverage) == null) {
				this.numBaseEdges.get(appName).put(this.currentCoverage, Integer.parseInt(line.split(" ")[3].trim()));
			}
		} else if(line.startsWith("Number of variables:")) {
			if(this.numVariables.get(appName).get(this.currentCoverage) == null) {
				this.numVariables.get(appName).put(this.currentCoverage, Integer.parseInt(line.split(" ")[3].trim()));
			}
		} else if(line.startsWith("Number of constraints:")) {
			if(this.numConstraints.get(appName).get(this.currentCoverage) == null) {
				this.numConstraints.get(appName).put(this.currentCoverage, Integer.parseInt(line.split(" ")[3].trim()));
			}
		} else if(line.startsWith("total cut")) {
			String[] tokens = line.split(" ");
			int cutNum = Integer.parseInt(tokens[2].trim().substring(0, tokens[2].length()-1));
			int numCuts = Integer.parseInt(tokens[3].trim());
			Map<Integer,Integer> cutMap = this.numCutEdges.get(appName).get(this.currentCoverage);
			if(cutMap == null) {
				cutMap = new HashMap<Integer,Integer>();
				this.numCutEdges.get(appName).put(this.currentCoverage, cutMap);
			}
			cutMap.put(cutNum, numCuts);
		} else if(this.isZerothCutEdge) {
			if(this.zerothCutEdgeCaller == null) {
				this.zerothCutEdgeCaller = line.split("caller:")[1].trim();
			} else {
				String callee = line.split("callee:")[1].trim();
				this.zerothCut.get(appName).add(this.currentCoverage, this.zerothCutEdgeCaller + " -> " + callee);
				this.isZerothCutEdge = false;
				this.zerothCutEdgeCaller = null;
			}
		} else if(line.startsWith("in cut 0:")) {
			this.isZerothCutEdge = true;
		}
	}

	@Override
	public void process(String appName, int appLinesOfCode, int frameworkLinesOfCode) {
		this.linesOfCode.put(appName, appLinesOfCode+frameworkLinesOfCode);
	}

	@Override
	public void finishProcessing(String appName) {
		if(this.lpRunningTimes.get(appName).get(this.currentCoverage) != null
				&& this.numVariables.get(appName).get(this.currentCoverage) != null
				&& this.numConstraints.get(appName).get(this.currentCoverage) != null
				&& this.numFlowEdges.get(appName).get(this.currentCoverage) != null
				&& this.numBaseEdges.get(appName).get(this.currentCoverage) != null
				&& this.numCutEdges.get(appName).get(this.currentCoverage) != null
				&& this.linesOfCode.get(appName) != null) {
			this.appNames.add(appName);
			if(this.coverages.get(appName).isEmpty()) {
				this.coverages.add(appName, this.currentCoverage);
			}
		}
	}
	
	public List<String> getAppNames() {
		Collections.sort(this.appNames, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if(linesOfCode.get(o1) == linesOfCode.get(o2)) {
					return 0;
				}
				if(linesOfCode.get(o1) == null) {
					return 1;
				}
				if(linesOfCode.get(o2) == null) {
					return -1;
				}
				if(linesOfCode.get(o1) < linesOfCode.get(o2)) {
					return 1;
				}
				return -1;				
			}
		});
		return Collections.unmodifiableList(this.appNames);
	}
	
	public List<Double> getCoverages(String appName) {
		List<Double> coveragesList = new ArrayList<Double>(this.coverages.get(appName));
		Collections.sort(coveragesList);
		return Collections.unmodifiableList(coveragesList);
	}
	
	private int getMaxNumCuts() {
		int numCuts = 0;
		for(String appName : this.getAppNames()) {
			for(double coverage : this.getCoverages(appName)) {
				int curNumCuts = this.numCutEdges.get(appName).get(coverage).size();
				if(curNumCuts > numCuts) {
					numCuts = curNumCuts;
				}
			}
		}
		return numCuts;
	}
	
	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(",");
		sb.append("Coverage").append(",");
		sb.append("LP Solve Time").append(",");
		sb.append("# Variables").append(",");
		sb.append("# Constraints").append(",");
		sb.append("# Flow Edges").append(",");
		sb.append("# Base Edges").append(",");
		for(int i=0; i<this.getMaxNumCuts(); i++) {
			sb.append("# Cut Edges " + i).append(",");
		}
		sb.append("Lines of Code").append(",");
		return sb.toString();		
	}
	
	public String getInfoFor(String appName, double coverage) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(",");
		sb.append(coverage).append(",");
		sb.append(this.lpRunningTimes.get(appName).get(coverage)).append(",");
		sb.append(this.numVariables.get(appName).get(coverage)).append(",");
		sb.append(this.numConstraints.get(appName).get(coverage)).append(",");
		sb.append(this.numFlowEdges.get(appName).get(coverage)).append(",");
		sb.append(this.numBaseEdges.get(appName).get(coverage)).append(",");
		int numCuts = this.getMaxNumCuts();
		for(int i=0; i<numCuts; i++) {
			sb.append(this.numCutEdges.get(appName).get(coverage).get(i)).append(",");
		}
		sb.append(this.linesOfCode.get(appName));
		return sb.toString();
	}
	
	public static void printSummary() {
		TestsProcessor tp = new TestsProcessor();
		new LogReader("../results_experiment2_with_pcs", tp).run();
		//new LogReader("../results_experiment1", tp).run();
		System.out.println(tp.getHeader());
		for(String appName : tp.getAppNames()) {
			for(double coverage : tp.getCoverages(appName)) {
				System.out.println(tp.getInfoFor(appName, coverage));
			}
		}
	}
	
	public static void printEdges() {
		TestsProcessor tp = new TestsProcessor();
		new LogReader("../results_good", tp).run();
		System.out.println("App#Coverage#Calledge");
		for(String appName : tp.getAppNames()) {
			for(double coverage : tp.getCoverages(appName)) {
				List<String> calledges = new ArrayList<String>(tp.zerothCut.get(appName).get(coverage));
				Collections.sort(calledges);
				for(String calledge : calledges) {
					System.out.println(appName + "#" + coverage + "#" + calledge);
				}
			}
		}		
	}
	
	public static void main(String[] args) {
		printSummary();
	}
}
