package stamp.missingmodels.util.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import stamp.missingmodels.util.cflsolver.core.Util.MultivalueMap;
import stamp.missingmodels.util.processor.LogReader.Processor;

public class AliasModelsProcessor implements Processor {
	private Map<String,Integer> appLinesOfCodeMap = new HashMap<String,Integer>();
	private MultivalueMap<String,String> appAliasModels = new MultivalueMap<String,String>(); // just do this one manually for now
	private MultivalueMap<String,String> appPhantomObjectModels = new MultivalueMap<String,String>();
	private Set<String> allPhantomObjectModels = new HashSet<String>();

	@Override
	public void process(String appName, String line) {
		if(line.startsWith("MODEL EDGE")) {
			this.appAliasModels.add(appName, line.split("MODEL EDGE: ")[1]);
		} else if(line.startsWith("PhantomObjectDyn")) {
			String method = line.split("##")[1];
			this.appPhantomObjectModels.add(appName, method);
			this.allPhantomObjectModels.add(method);
		}
	}

	@Override
	public void startProcessing(String appName) {
	}

	@Override
	public void process(String appName, int appLinesOfCode, int frameworkLinesOfCode) {
		this.appLinesOfCodeMap.put(appName, appLinesOfCode);
	}

	@Override
	public void finishProcessing(String appName) {
	}
	
	public String getModelsHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(" & ");
		sb.append("# Alias Models").append(" & ");
		sb.append("# Phantom Object Models").append(" & ");
		sb.append("# Total Models");
		return sb.toString();
	}
	
	public String getModelsAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append("TODO").append(" & ");
		sb.append(this.appPhantomObjectModels.get(appName).size()).append(" & ");
		sb.append("TODO");
		return sb.toString();
	}
	
	public String getModelsTotal() {
		StringBuilder sb = new StringBuilder();
		sb.append(" -- ").append(" & ");
		sb.append("0").append(" & ");
		sb.append(this.allPhantomObjectModels.size()).append(" & ");
		sb.append(this.allPhantomObjectModels.size());
		return sb.toString();
	}
	
	public Iterable<String> getAppNames() {
		return this.appLinesOfCodeMap.keySet();
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
		sb.append(appLinesOfCodeMap.get(appName)).append(" & ");
		sb.append("???").append(" & ");
		sb.append("???");
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
			System.out.println(ap.getModelsHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getModelsAppLine(appName) + "\\\\");
			}
			System.out.println(ap.getModelsTotal());
			break;
		}
	}
	
	public static void main(String[] args) {
		run("../results/fifth", 0);
	}
}
