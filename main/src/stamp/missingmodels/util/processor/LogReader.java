package stamp.missingmodels.util.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogReader {
	public interface Processor {
		public void process(String appName, String line);
		public void startProcessing(String appName);
		public void process(String appName, int appLinesOfCode, int frameworkLinesOfCode);
		public void finishProcessing(String appName); // clean up in case of failure
	}
	
	private final String rootPath;
	private final List<Processor> processors;
	
	public LogReader(String rootPath, List<Processor> processors) {
		this.rootPath = rootPath;
		this.processors = processors;
	}
	
	public LogReader(String rootPath, Processor processor) {
		this.rootPath = rootPath;
		this.processors = new ArrayList<Processor>();
		this.processors.add(processor);
	}
	
	public void run() {
		File root = new File(this.rootPath);
		for(File appDir : root.listFiles()) {
			try {
				if(appDir.getCanonicalPath().endsWith("app-reports.db")) {
					continue;
				}
				String[] appTokens = appDir.getName().split("_");
				String appName = appTokens[appTokens.length-1];
				
				File resultFile = new File(appDir, "log.txt");
				BufferedReader br = new BufferedReader(new FileReader(resultFile));
				
				for(Processor processor : this.processors) {
					processor.startProcessing(appName);
				}
				
				String line;
				while((line = br.readLine()) != null) {
					for(Processor processor : this.processors) {
						processor.process(appName, line);
					}
				}
				br.close();
				
				File locFile = new File(appDir, "loc.txt");
				br = new BufferedReader(new FileReader(locFile));
				int appLoc = Integer.parseInt(br.readLine());
				int frameworkLoc = Integer.parseInt(br.readLine());
				br.close();
				for(Processor processor : this.processors) {
					processor.process(appName, appLoc, frameworkLoc);
					processor.finishProcessing(appName);
				}				
			} catch(IOException e) {
				//e.printStackTrace();
			}
		}
	}
}
