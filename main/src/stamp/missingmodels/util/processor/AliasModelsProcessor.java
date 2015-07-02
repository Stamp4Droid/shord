package stamp.missingmodels.util.processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	public static class ModelReader {
		public static Map<String,Integer> readMap(String filename) {
			try {
				Map<String,Integer> models = new HashMap<String,Integer>();
				BufferedReader br = new BufferedReader(new FileReader(filename));
				String line;
				while((line = br.readLine()) != null) {
					if(line.trim().equals("")) { continue; }
					String[] tokens = line.split("##");
					if(tokens.length != 2) { br.close(); throw new RuntimeException("Invalid line: " + line); }
					models.put(tokens[0], Integer.parseInt(tokens[1]));
				}
				br.close();
				return models;
			} catch(IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error reading models file: " + filename);
			}
		}
	}
	
	public AliasModelsProcessor(Map<String,Integer> aliasModels, Map<String,Integer> phantomObjectModels) {
		this.aliasModels = aliasModels;
		this.phantomObjectModels = phantomObjectModels;
	}
	
	private final Map<String,Integer> aliasModels;
	private final Map<String,Integer> phantomObjectModels;
	private final Map<String,Integer> appLinesOfCodeMap = new HashMap<String,Integer>();
	private final MultivalueMap<String,String> appAliasModels = new MultivalueMap<String,String>();
	private final MultivalueMap<String,String> appCorrectAliasModels = new MultivalueMap<String,String>();
	private final HashSet<String> allAliasModels = new HashSet<String>();
	private final HashSet<String> allCorrectAliasModels = new HashSet<String>();
	private final MultivalueMap<String,String> appPhantomObjectModels = new MultivalueMap<String,String>();
	private final MultivalueMap<String,String> appCorrectPhantomObjectModels = new MultivalueMap<String,String>();
	private final Set<String> allPhantomObjectModels = new HashSet<String>();
	private final Set<String> allCorrectPhantomObjectModels = new HashSet<String>();
	private final Map<String,Integer> appFrameworkMethodCalls = new HashMap<String,Integer>();
	private final Map<String,Integer> appEscapedObjects = new HashMap<String,Integer>();
	private final Map<String,Integer> appAliasingStatements = new HashMap<String,Integer>();
	
	// 0 : after PRINTING MODEL statement
	// 1 : after entry (param/return)
	// 2 : after first interal edge of model (load/store)
	// 3 : invalid model, ignore
	int state = 3;
	StringBuilder curModel;
	@Override
	public void process(String appName, String line) {
		if(line.startsWith("PhantomObjectDyn")) {
			String method = line.split("##")[1];
			if(!this.phantomObjectModels.containsKey(method)) {
				this.phantomObjectModels.put(method, 2);
			}
			if(this.phantomObjectModels.get(method) != 0) {
				this.appPhantomObjectModels.add(appName, method);
				this.allPhantomObjectModels.add(method);
				if(this.phantomObjectModels.get(method) == 1) {
					this.appCorrectPhantomObjectModels.add(appName, method);
					this.allCorrectPhantomObjectModels.add(method);
				}
			}
		} else if(line.startsWith("StubI size:") || line.startsWith("FrameworkI size:")) { // backwards compatibility
			this.appFrameworkMethodCalls.put(appName, Integer.parseInt(line.split(" ")[2]));
		} else if(line.startsWith("Load size:") || line.startsWith("Store size:") || line.startsWith("Alloc size:") || line.startsWith("Assign size:")) {
			this.appAliasingStatements.put(appName, Integer.parseInt(line.split(" ")[2]) + this.appAliasingStatements.get(appName));
		} else if(line.startsWith("SAVING dom I size:")) {
			this.appAliasingStatements.put(appName, Integer.parseInt(line.split("size: ")[1]) + this.appAliasingStatements.get(appName));			
		} else if(line.startsWith("EscapeH size:")) {
			this.appEscapedObjects.put(appName,  Integer.parseInt(line.split(" ")[2]));
		} else if(line.startsWith("MODEL EDGE")) {
			// STEP 1: If the model is invalid, continue
			if(state != 3) {
				// STEP 2: Remove the #1 at the end of the line
				String[] modelTokens = line.split("EDGE: ");
				if(modelTokens.length != 2) { throw new RuntimeException("Invalid line: " + line); }
				if(!modelTokens[1].endsWith("#1")) { throw new RuntimeException("Invalid line: " + line); }
				String lineNoWeight = modelTokens[1].substring(0, modelTokens[1].length() - 2);
				// STEP 3: Handle the case where entering or exiting the model
				if(lineNoWeight.contains("-param[-1]-") || lineNoWeight.contains("-return[-1]-")) {
					// STEP 3a: Get the source and sink
					String str;
					if(lineNoWeight.contains("-param[-1]-")) {
						String[] tokens = lineNoWeight.split("-param\\[-1\\]-");
						if(tokens.length != 2) { throw new RuntimeException("Invalid line: " + line); }
						str = "-param[-1]-" + tokens[1];
					} else {
						String[] tokens = lineNoWeight.split("-return\\[-1\\]-");
						if(tokens.length != 2) { throw new RuntimeException("Invalid line: " + line); }
						str = tokens[0] + "-return\\[-1\\]-";
					}
					// STEP 3b: Handle model start / finish
					if(state == 0) {
						// start the model
						curModel.append(str).append("#");
						state = 1;
					} else if(state == 1 || state == 2) {
						// end the model
						curModel.append(str);
						this.updateModel(appName, curModel.toString());
						curModel = null;
						state = 3;
					} else {
						throw new RuntimeException("Invalid state " + state + ": " + line);
					}
				}
				// STEP 4: Handle the case 
				else if(lineNoWeight.contains("-load[-1]-") || lineNoWeight.contains("-store[-1]-")) {
					if(state == 1) {
						// add the line and increment the state
						curModel.append(lineNoWeight).append("#");
						state = 2;
					} else if(state == 2 || state == 3) {
						// invalid state
						curModel = null;
						state = 3;
					} else {
						throw new RuntimeException("Invalid state " + state + ": " + line);
					}
				}
				// STEP 5: Invalid model
				else {
					state = 3;
					//throw new RuntimeException("Invalid line: " + line);
				}
			}
		} else if(line.startsWith("PRINING MODEL") || line.startsWith("PRINTING MODEL")) { // backwards compatibility
			state = 0;
			curModel = new StringBuilder();
		}
	}
	
	private void updateModel(String appName, String model) {
		if(!this.aliasModels.containsKey(model)) {
			this.aliasModels.put(model, 2);
		}
		if(this.aliasModels.get(model) == 0) {
			return;
		}
		this.appAliasModels.add(appName, curModel.toString());
		this.allAliasModels.add(curModel.toString());
		if(this.aliasModels.get(model) == 1) {
			this.appCorrectAliasModels.add(appName, model);
			this.allCorrectAliasModels.add(model);
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
		sb.append(this.appCorrectPhantomObjectModels.get(appName).size()).append(" & ");
		sb.append((float)this.appCorrectPhantomObjectModels.get(appName).size()/this.appPhantomObjectModels.get(appName).size());
		return sb.toString();
	}
	
	public String getPhantomObjectModelsTotal() {
		StringBuilder sb = new StringBuilder();
		sb.append(" -- ").append(" & ");
		sb.append(this.allPhantomObjectModels.size()).append(" & ");
		sb.append(this.allCorrectPhantomObjectModels.size()).append(" & ");
		sb.append((float)this.allCorrectPhantomObjectModels.size()/this.allPhantomObjectModels.size());
		return sb.toString();
	}
	
	public String getAliasModelsHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(" & ");
		/*
		sb.append("# Alias Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		*/
		sb.append("# $1$-Shallow Alias Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		return sb.toString();
	}
	
	public String getAliasModelsAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		/*
		sb.append("TODO").append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("0.0").append(" & ");
		*/
		sb.append(this.appAliasModels.get(appName).size()).append(" & ");
		sb.append(this.appCorrectAliasModels.get(appName).size()).append(" & ");
		sb.append((float)this.appCorrectAliasModels.get(appName).size()/this.appAliasModels.get(appName).size());
		return sb.toString();
	}
	
	public String getAliasModelsTotal() {
		StringBuilder sb = new StringBuilder();
		sb.append(" -- ").append(" & ");
		sb.append(this.allAliasModels.size()).append(" & ");
		sb.append(this.allCorrectAliasModels.size()).append(" & ");
		sb.append((float)this.allCorrectAliasModels.size()/this.allAliasModels.size());
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
		sb.append("# Framework Methods").append(" & ");
		sb.append("Running Time").append(" & ");
		return sb.toString();
	}
	
	public String getStatisticsAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append(this.appLinesOfCodeMap.get(appName)).append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("TODO");
		return sb.toString();
	}
	
	public String getMonitorHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("\\# Alias Statements").append(" & ");
		sb.append("App Name").append(" & ");
		sb.append("$|M_{\\text{pt}}|$").append(" & ");
		sb.append("$|M_o|$").append(" & ");
		sb.append("$|M|$").append(" & ");
		sb.append("$\\frac{|M|}{\\#\\text{Alias Statements}}$");
		return sb.toString();
	}
	
	public String getMonitorAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append(this.appAliasingStatements.get(appName)).append(" & ");
		sb.append(this.appFrameworkMethodCalls.get(appName)).append(" & ");
		sb.append(this.appEscapedObjects.get(appName)).append(" & ");
		sb.append((this.appFrameworkMethodCalls.get(appName) + this.appEscapedObjects.get(appName))).append(" & ");
		sb.append((float)(this.appFrameworkMethodCalls.get(appName) + this.appEscapedObjects.get(appName))/this.appAliasingStatements.get(appName));
		return sb.toString();
	}
	
	public static void run(String directory, int type) {
		AliasModelsProcessor ap = new AliasModelsProcessor(ModelReader.readMap("../results/models/alias_models.txt"), ModelReader.readMap("../results/models/phantom_object_models.txt"));		
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
		case 3:
			System.out.println(ap.getMonitorHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getMonitorAppLine(appName) + "\\\\");
			}
			break;
		case 4: // print phantom object models
			Map<String,Integer> phantomObjectModels = ModelReader.readMap("../results/models/phantom_object_models.txt");
			for(String model : ap.allPhantomObjectModels) {
				if(!phantomObjectModels.containsKey(model)) {
					System.out.println(model);
				}
			}
			break;
		case 5: // print alias models
			Map<String,Integer> aliasModels = ModelReader.readMap("../results/models/alias_models.txt");
			for(String model : ap.allAliasModels) {
				if(!aliasModels.containsKey(model)) {
					System.out.println(model);
				}
			}
			break;
		}
	}
	
	public static void main(String[] args) {
		run("../results/all_server", 3);
	}
}
