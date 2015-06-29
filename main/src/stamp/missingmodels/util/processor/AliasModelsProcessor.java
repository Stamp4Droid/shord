package stamp.missingmodels.util.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.processor.LogReader.Processor;

public class AliasModelsProcessor implements Processor {
	private final Map<String,Integer> appLinesOfCodeMap = new HashMap<String,Integer>();
	private final MultivalueMap<String,String> appAliasModels = new MultivalueMap<String,String>(); // just do this one manually for now
	private final MultivalueMap<String,String> appPhantomObjectModels = new MultivalueMap<String,String>();
	private final Set<String> allPhantomObjectModels = new HashSet<String>();
	private final Map<String,Integer> appFrameworkMethodCalls = new HashMap<String,Integer>();
	private final Map<String,Integer> appAliasingStatements = new HashMap<String,Integer>();

	@Override
	public void process(String appName, String line) {
		String relFrameworkIName = "StubI";
		if(line.startsWith("MODEL EDGE")) {
			this.appAliasModels.add(appName, line.split("MODEL EDGE: ")[1]);
		} else if(line.startsWith("PhantomObjectDyn")) {
			String method = line.split("##")[1];
			this.appPhantomObjectModels.add(appName, method);
			this.allPhantomObjectModels.add(method);
		} else if(line.startsWith(relFrameworkIName + " size:")) {
			this.appFrameworkMethodCalls.put(appName, Integer.parseInt(line.split(" ")[2]));
		} else if(line.startsWith("Load size:") || line.startsWith("Store size:") || line.startsWith("Alloc size:") || line.startsWith("Assign size:")) {
			this.appAliasingStatements.put(appName, Integer.parseInt(line.split(" ")[2]) + this.appAliasingStatements.get(appName));
		}
	}

	@Override
	public void startProcessing(String appName) {
		this.appAliasingStatements.put(appName, 0);
	}

	@Override
	public void process(String appName, int appLinesOfCode, int frameworkLinesOfCode) {
		this.appLinesOfCodeMap.put(appName, appLinesOfCode);
	}

	@Override
	public void finishProcessing(String appName) {
	}
	
	public String getPhantomObjectModelsHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(" & ");
		sb.append("# Phantom Object Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		return sb.toString();
	}
	
	public String getPhantomObjectModelsAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append(this.appPhantomObjectModels.get(appName).size()).append(" & ");
		sb.append(this.appPhantomObjectModels.get(appName).size()).append(" & ");
		sb.append("1.0");
		return sb.toString();
	}
	
	public String getPhantomObjectModelsTotal() {
		StringBuilder sb = new StringBuilder();
		sb.append(" -- ").append(" & ");
		sb.append(this.allPhantomObjectModels.size()).append(" & ");
		sb.append(this.allPhantomObjectModels.size()).append(" & ");
		sb.append("1.0");
		return sb.toString();
	}
	
	public String getAliasModelsHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(" & ");
		sb.append("# Alias Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		sb.append("# $1$-Shallow Alias Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		return sb.toString();
	}
	
	public String getAliasModelsAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("0").append(" & ");
		sb.append("0.0").append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("1.0");
		return sb.toString();
	}
	
	public String getAliasModelsTotal() {
		StringBuilder sb = new StringBuilder();
		sb.append(" -- ").append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("1.0");
		return sb.toString();
	}
	
	public Iterable<String> getAppNames() {
		List<String> appNamesSorted = new ArrayList<String>(this.appLinesOfCodeMap.keySet());
		Collections.sort(appNamesSorted, new Comparator<String>() {
			@Override
			public int compare(String appName1, String appName2) {
				if(appLinesOfCodeMap.get(appName1) > appLinesOfCodeMap.get(appName2)) {
					return -1;
				} else if(appLinesOfCodeMap.get(appName1) < appLinesOfCodeMap.get(appName2)) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		return appNamesSorted;
	}
	
	public String getStatisticsHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(" & ");
		sb.append("# Jimple LOC").append(" & ");
		sb.append("# Aliasing Statements").append(" & ");
		sb.append("# Invocations of Stubs");
		return sb.toString();
	}
	
	public String getStatisticsAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append(this.appLinesOfCodeMap.get(appName)).append(" & ");
		sb.append(this.appAliasingStatements.get(appName)).append(" & ");
		sb.append(this.appFrameworkMethodCalls.get(appName));
		return sb.toString();
	}
	
	public static void run(String directory, int type) {
		AliasModelsProcessor ap = new AliasModelsProcessor();
		new LogReader(directory, ap).run();
		
		switch(type) {
		case 0:			
			System.out.println(ap.getStatisticsHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getStatisticsAppLine(appName) + "\\\\");
			}
			break;
		case 1:
			System.out.println(ap.getPhantomObjectModelsHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getPhantomObjectModelsAppLine(appName) + "\\\\");
			}
			System.out.println(ap.getPhantomObjectModelsTotal());
			break;
		case 2:
			System.out.println(ap.getAliasModelsHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getAliasModelsAppLine(appName) + "\\\\");
			}
			System.out.println(ap.getAliasModelsTotal());
			break;
		}
	}
	
	public static void main(String[] args) {
		run("../results/eighth", 0);
	}
}
