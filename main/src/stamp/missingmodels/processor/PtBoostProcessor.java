package stamp.missingmodels.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.processor.LogReader.Processor;

public class PtBoostProcessor implements Processor {
	private final List<String> appNames = new ArrayList<String>();
	
	private int solveTime = -1;
	
	private Map<String,Integer> ptSolveTime = new HashMap<String,Integer>();
	private Map<String,Integer> ptSize = new HashMap<String,Integer>();
	
	private final List<Map<String,Integer>> ptBoostSolveTimes;
	private final List<Map<String,Integer>> ptFilterSizes;

	private final Map<String,Integer> appLinesOfCode = new HashMap<String,Integer>();
	private final Map<String,Integer> frameworkLinesOfCode = new HashMap<String,Integer>();
	
	private static final int NUM_PT_BOOST = 6;
	
	public PtBoostProcessor() {
		this.ptBoostSolveTimes = new ArrayList<Map<String,Integer>>();
		this.ptFilterSizes = new ArrayList<Map<String,Integer>>();
		for(int i=0; i<NUM_PT_BOOST; i++) {
			this.ptBoostSolveTimes.add(new HashMap<String,Integer>());
			this.ptFilterSizes.add(new HashMap<String,Integer>());
		}
	}
	
	@Override
	public void startProcessing(String appName) {}
	
	@Override
	public void process(String appName, String line) {
		if(line.startsWith("SOLVE_TIME=")) {
			this.solveTime = Integer.parseInt(line.substring("SOLVE_TIME=".length()));
		} else if(line.startsWith("Relation pt:")) {
			if(this.solveTime != -1) {
				this.ptSolveTime.put(appName, this.solveTime);
			}
			this.ptSize.put(appName, Integer.parseInt(line.substring("Relation pt:".length()+1).split(" ")[0]));
			this.solveTime = -1;
		} else {
			for(int i=1; i<=NUM_PT_BOOST; i++) {
				String ptBoostI = "ptBoost" + Integer.toString(i);
				String relationBoost = "Relation " + ptBoostI + ":";
				if(line.startsWith(relationBoost)) {
					if(this.solveTime != -1) {
						this.ptBoostSolveTimes.get(i-1).put(appName, this.solveTime);
					}
					this.solveTime = -1;
				}

				String ptFilterI = "ptFilter" + Integer.toString(i);
				String relationFilter = "Relation " + ptFilterI + ":";
				if(line.startsWith(relationFilter)) {
					this.ptFilterSizes.get(i-1).put(appName, Integer.parseInt(line.substring(relationFilter.length()+1).split(" ")[0]));
				}
			}
		}
	}
	
	@Override
	public void process(String appName, int appLinesOfCode, int frameworkLinesOfCode) {
		this.appLinesOfCode.put(appName, appLinesOfCode);
		this.frameworkLinesOfCode.put(appName, frameworkLinesOfCode);
	}
	
	@Override
	public void finishProcessing(String appName) {
		if(this.ptSolveTime.get(appName) == null || this.ptSize.get(appName) == null) {
			return;
		}
		for(int i=0; i<NUM_PT_BOOST; i++) {
			if(this.ptBoostSolveTimes.get(i).get(appName) == null || this.ptFilterSizes.get(i).get(appName) == null) {
				return;
			}
		}
		if(this.appLinesOfCode.get(appName) == null || this.frameworkLinesOfCode.get(appName) == null) {
			return;
		}
		this.appNames.add(appName);
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
		sb.append("Pt Solve Time").append(",");
		for(int i=0; i<NUM_PT_BOOST; i++) {
			sb.append("PtBoost" + Integer.toString(i+1) + " Solve Time").append(",");
		}
		sb.append("Pt Size").append(",");
		for(int i=0; i<NUM_PT_BOOST; i++) {
			sb.append("PtFilter" + Integer.toString(i+1) + " Size").append(",");
		}
		sb.append("App Lines of Code").append(",");
		sb.append("Framework Lines of Code");
		return sb.toString();		
	}
	
	public String getInfoFor(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(",");
		sb.append(this.ptSolveTime.get(appName)).append(",");
		for(int i=0; i<NUM_PT_BOOST; i++) {
			sb.append(this.ptBoostSolveTimes.get(i).get(appName)).append(",");
		}
		sb.append(this.ptSize.get(appName)).append(",");
		for(int i=0; i<NUM_PT_BOOST; i++) {
			sb.append(this.ptFilterSizes.get(i).get(appName)).append(",");
		}
		sb.append(this.appLinesOfCode.get(appName)).append(",");
		sb.append(this.frameworkLinesOfCode.get(appName));
		return sb.toString();
	}
	
	public static void main(String[] args) {
		PtBoostProcessor pbp = new PtBoostProcessor();
		new LogReader("../stamp_output", pbp).run();
		System.out.println(pbp.getHeader());
		for(String appName : pbp.getAppNames()) {
			System.out.println(pbp.getInfoFor(appName));
		}
	}
}
