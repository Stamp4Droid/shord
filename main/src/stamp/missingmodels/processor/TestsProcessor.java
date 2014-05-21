package stamp.missingmodels.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.processor.LogReader.Processor;

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
	private final Map<String,Integer> numCutEdges = new HashMap<String,Integer>();
	
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
		} else if(line.startsWith("total cut:")) {
			this.numCutEdges.put(appName, Integer.parseInt(line.split(" ")[2].trim()));			
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
	
	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(",");
		sb.append("LP Solve Time").append(",");
		sb.append("# Variables").append(",");
		sb.append("# Constraints").append(",");
		sb.append("# Flow Edges").append(",");
		sb.append("# Base Edges").append(",");
		sb.append("# Cut Edges").append(",");
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
		sb.append(this.numCutEdges.get(appName)).append(",");
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
