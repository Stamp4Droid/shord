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
	
	public AliasModelsProcessor(Map<String,Integer> pairAliasModels, Map<String,Integer> singleAliasModels, Map<String,Integer> phantomObjectModels) {
		this.pairAliasModels = pairAliasModels;
		this.singleAliasModels = singleAliasModels;
		this.phantomObjectModels = phantomObjectModels;
	}
	
	private final Map<String,Integer> pairAliasModels;
	private final Map<String,Integer> singleAliasModels;
	private final Map<String,Integer> phantomObjectModels;
	private final Map<String,Integer> appLinesOfCodeMap = new HashMap<String,Integer>();
	private final MultivalueMap<String,String> appSingleAliasModels = new MultivalueMap<String,String>();
	private final MultivalueMap<String,String> appSingleCorrectAliasModels = new MultivalueMap<String,String>();
	private final HashSet<String> allSingleAliasModels = new HashSet<String>();
	private final HashSet<String> allSingleCorrectAliasModels = new HashSet<String>();
	private final MultivalueMap<String,String> appPairAliasModels = new MultivalueMap<String,String>();
	private final MultivalueMap<String,String> appPairCorrectAliasModels = new MultivalueMap<String,String>();
	private final HashSet<String> allPairAliasModels = new HashSet<String>();
	private final HashSet<String> allPairCorrectAliasModels = new HashSet<String>();
	private final MultivalueMap<String,String> appPhantomObjectModels = new MultivalueMap<String,String>();
	private final MultivalueMap<String,String> appCorrectPhantomObjectModels = new MultivalueMap<String,String>();
	private final Set<String> allPhantomObjectModels = new HashSet<String>();
	private final Set<String> allCorrectPhantomObjectModels = new HashSet<String>();
	private final Map<String,Integer> appFrameworkMethodCalls = new HashMap<String,Integer>();
	private final Map<String,Integer> appEscapedObjects = new HashMap<String,Integer>();
	private final Map<String,Integer> appAliasingStatements = new HashMap<String,Integer>();
	private final Map<String,Double> appRunningTimes = new HashMap<String,Double>();
	private final HashSet<String> allMissingSingleAliasModels = new HashSet<String>();
	private final HashSet<String> allMissingPairAliasModels = new HashSet<String>();
	private final HashSet<String> allMissingPhantomObjectModels = new HashSet<String>();
	
	private static final String[] runningTimeAnalyses = new String[]{"cfl-alias-models-dlog", "cfl-active-alias-models-dlog", "alias-models-short-java"};
	private static boolean isRunningTimeAnalysis(String line) {
		for(String runningTimeAnalysis : runningTimeAnalyses) {
			if(line.equals("LEAVE: " + runningTimeAnalysis)) {
				return true;
			}
		}
		return false;
	}
	
	// 0 : after PRINTING MODEL statement
	// 1 : after entry (param/return)
	// 2 : after first interal edge of model (load/store)
	// 3 : invalid model, ignore
	private int state = 3;
	private StringBuilder curModel;
	private boolean runningTimeFlag = false;
	// header: the header of the model
	// model: the concatenated elements of the model
	// flag: whether or not to include the model (disabled if header mismatch)
	private String header = null;
	private String model = null;
	boolean flag = false;
	@Override
	public void process(String appName, String line) {
		if(line.startsWith("PhantomObjectDyn")) {
			String method = line.split("##")[1];
			if(!this.phantomObjectModels.containsKey(method)) {
				this.allMissingPhantomObjectModels.add(method);
			} else if(this.phantomObjectModels.get(method) != 0) {
				this.appPhantomObjectModels.add(appName, method);
				this.allPhantomObjectModels.add(method);
				if(this.phantomObjectModels.get(method) == 1) {
					this.appCorrectPhantomObjectModels.add(appName, method);
					this.allCorrectPhantomObjectModels.add(method);
				}
			}
		} else if(line.startsWith("Relation StubInvokeMonitorRet:")) {
			this.appFrameworkMethodCalls.put(appName, (int)Float.parseFloat(line.split(" ")[4]));
		} else if(line.startsWith("Relation LoadNF:") || line.startsWith("Relation StoreNF:") || line.startsWith("Relation AllocNF:") || line.startsWith("Relation AssignNF:") || line.startsWith("Relation InvokeMonitorArg") || line.startsWith("Relation InvokeMonitorReg")) {
			this.appAliasingStatements.put(appName, (int)Float.parseFloat(line.split(" ")[4]) + this.appAliasingStatements.get(appName));
		} else if(line.startsWith("Relation EscapeCountH:")) {
			this.appEscapedObjects.put(appName,  (int)Float.parseFloat(line.split(" ")[4]));
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
		} else if(isRunningTimeAnalysis(line)) {
			runningTimeFlag = true;
		} else if(runningTimeFlag) {
			String[] tokens = line.split(" ")[2].split(":");
			double runningTime = 60.0*Integer.parseInt(tokens[0]) + Integer.parseInt(tokens[1]) + 1.0/60*Integer.parseInt(tokens[2]) + 1.0/60/1000*Integer.parseInt(tokens[3]);
			this.appRunningTimes.put(appName, runningTime + this.appRunningTimes.get(appName));
			runningTimeFlag = false;
		} else if(line.startsWith("MODEL PATH")) {
			if(line.contains("store[-1]") || line.contains("load[-1]")) {
				if(header == null) {
					header = line.split(" ")[2];
					model = extract(line);
				} else {
					if(!line.split(" ")[2].equals(header)) {
						flag = true;
					}
					model = model + extract(line);
				}
			}
		} else if(line.startsWith("Done!")) {
			if(model != null && !flag && check(model)) {
				String finalModel = model.substring(0, model.length()-2);
				if(!this.pairAliasModels.containsKey(finalModel)) {
					this.allMissingPairAliasModels.add(finalModel);
				}
				if(this.pairAliasModels.containsKey(finalModel) && this.pairAliasModels.get(finalModel) != 0) {
					allPairAliasModels.add(finalModel);
					appPairAliasModels.add(appName, finalModel);
					if(this.pairAliasModels.get(finalModel) == 1) {
						allPairCorrectAliasModels.add(finalModel);
						appPairCorrectAliasModels.add(appName, finalModel);
					}
				}
			}
			header = null;
			model = null;
			flag = false;
		}
	}

	private static String extract(String line) {
		String tail = line.split("PATH: ")[1];
		return tail.substring(0, tail.length()-2) + ";;";
	}
	
	private static boolean check(String model) {
		return model.split(";;").length == 2;
	}
	
	private void updateModel(String appName, String model) {
		if(!this.singleAliasModels.containsKey(model)) {
			this.allMissingSingleAliasModels.add(model);
			return;
		}
		if(this.singleAliasModels.get(model) == 0) {
			return;
		}
		this.appSingleAliasModels.add(appName, curModel.toString());
		this.allSingleAliasModels.add(curModel.toString());
		if(this.singleAliasModels.get(model) == 1) {
			this.appSingleCorrectAliasModels.add(appName, model);
			this.allSingleCorrectAliasModels.add(model);
		}
	}

	@Override
	public void startProcessing(String appName) {
		this.appAliasingStatements.put(appName, 0);
		this.appRunningTimes.put(appName, 0.0);
	}

	@Override
	public void process(String appName, int appLinesOfCode, int frameworkLinesOfCode) {
		this.appLinesOfCodeMap.put(appName, appLinesOfCode);
	}

	@Override
	public void finishProcessing(String appName) {
	}
	
	public String getAliasModelsHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(" & ");
		sb.append("Running Time (min)").append(" & ");
		sb.append("# $1$-Shallow Alias Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy").append(" & ");
		sb.append("# Phantom Object Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		return sb.toString();
	}
	
	public String getAliasModelsAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append(this.appRunningTimes.get(appName)).append(" & ");
		sb.append(this.appSingleAliasModels.get(appName).size() + 2*this.appPairAliasModels.get(appName).size()).append(" & ");
		sb.append(this.appSingleCorrectAliasModels.get(appName).size() + 2*this.appPairCorrectAliasModels.get(appName).size()).append(" & ");
		sb.append((float)(this.appSingleCorrectAliasModels.get(appName).size() + 2*this.appPairCorrectAliasModels.get(appName).size())/(this.appSingleAliasModels.get(appName).size() +  + 2*this.appPairAliasModels.get(appName).size())).append(" & ");
		sb.append(this.appPhantomObjectModels.get(appName).size()).append(" & ");
		sb.append(this.appCorrectPhantomObjectModels.get(appName).size()).append(" & ");
		sb.append((float)this.appCorrectPhantomObjectModels.get(appName).size()/this.appPhantomObjectModels.get(appName).size());
		return sb.toString();
	}
	
	public String getAliasModelsTotal() {
		StringBuilder sb = new StringBuilder();
		sb.append("Total").append(" & ");
		sb.append("--").append(" & ");
		sb.append(this.allSingleAliasModels.size() + 2*this.allPairAliasModels.size()).append(" & ");
		sb.append(this.allSingleCorrectAliasModels.size() + 2*this.allPairCorrectAliasModels.size()).append(" & ");
		sb.append((float)(this.allSingleCorrectAliasModels.size() + 2*this.allPairCorrectAliasModels.size())/(this.allSingleAliasModels.size() + 2*this.allPairAliasModels.size())).append(" & ");
		sb.append(this.allPhantomObjectModels.size()).append(" & ");
		sb.append(this.allCorrectPhantomObjectModels.size()).append(" & ");
		sb.append((float)this.allCorrectPhantomObjectModels.size()/this.allPhantomObjectModels.size());
		return sb.toString();
	}
	
	public String getAliasModelsComparisonHeader() {
		StringBuilder sb = new StringBuilder();
		/*
		sb.append("App Name").append(" & ");
		sb.append("# Alias Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		*/
		sb.append("# $1$-Shallow Alias Models Inferred").append(" & ");
		sb.append("# Correct").append(" & ");
		sb.append("Accuracy");
		return sb.toString();
	}
	
	public String getAliasModelsComparisonAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		/*
		sb.append("TODO").append(" & ");
		sb.append("TODO").append(" & ");
		sb.append("0.0").append(" & ");
		*/
		sb.append(this.appSingleAliasModels.get(appName).size() + 2*this.appPairAliasModels.get(appName).size()).append(" & ");
		sb.append(this.appSingleCorrectAliasModels.get(appName).size() + 2*this.appPairCorrectAliasModels.get(appName).size()).append(" & ");
		sb.append((float)(this.appSingleCorrectAliasModels.get(appName).size() + 2*this.appPairCorrectAliasModels.get(appName).size())/(this.appSingleAliasModels.get(appName).size() +  + 2*this.appPairAliasModels.get(appName).size()));
		return sb.toString();
	}
	
	public String getAliasModelsComparisonTotal() {
		StringBuilder sb = new StringBuilder();
		sb.append(" -- ").append(" & ");
		sb.append(this.allSingleAliasModels.size() + 2*this.allPairAliasModels.size()).append(" & ");
		sb.append(this.allSingleCorrectAliasModels.size() + 2*this.allPairCorrectAliasModels.size()).append(" & ");
		sb.append((float)(this.allSingleCorrectAliasModels.size() + 2*this.allPairAliasModels.size())/(this.allSingleAliasModels.size() + 2*this.allPairAliasModels.size()));
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
	
	public String getMonitorHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("App Name").append(" & ");
		sb.append("# Jimple LOC").append(" & ");
		sb.append("\\# Alias Statements").append(" & ");
		sb.append("$|M_{\\text{pt}}|$").append(" & ");
		sb.append("$|M_o|$").append(" & ");
		sb.append("$|M|$").append(" & ");
		sb.append("$\\frac{|M|}{\\#\\text{Alias Statements}}$");
		return sb.toString();
	}
	
	public String getMonitorAppLine(String appName) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(" & ");
		sb.append(this.appLinesOfCodeMap.get(appName)).append(" & ");
		sb.append(this.appAliasingStatements.get(appName)).append(" & ");
		sb.append(this.appFrameworkMethodCalls.get(appName)).append(" & ");
		sb.append(this.appEscapedObjects.get(appName)).append(" & ");
		sb.append((this.appFrameworkMethodCalls.get(appName) + this.appEscapedObjects.get(appName))).append(" & ");
		sb.append((float)(this.appFrameworkMethodCalls.get(appName) + this.appEscapedObjects.get(appName))/this.appAliasingStatements.get(appName));
		return sb.toString();
	}
	
	public static void run(String directory, int type) {
		AliasModelsProcessor ap = new AliasModelsProcessor(ModelReader.readMap("../results/models/alias_models_loadstore.txt"), ModelReader.readMap("../results/models/alias_models_assign.txt"), ModelReader.readMap("../results/models/phantom_object_models.txt"));		
		new LogReader(directory, ap).run();
		
		switch(type) {
		case 0:
			System.out.println(ap.getAliasModelsHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getAliasModelsAppLine(appName) + "\\\\");
			}
			System.out.println(ap.getAliasModelsTotal());
			break;
		case 1:
			System.out.println(ap.getAliasModelsComparisonHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getAliasModelsComparisonAppLine(appName) + "\\\\");
			}
			System.out.println(ap.getAliasModelsComparisonTotal());
			break;
		case 2:
			System.out.println(ap.getMonitorHeader());
			for(String appName : ap.getAppNames()) {
				System.out.println(ap.getMonitorAppLine(appName) + "\\\\");
			}
			break;
		case 3: // print missing phantom object models
			for(String model : ap.allMissingPhantomObjectModels) {
				System.out.println(model);
			}
			break;
		case 4: // print missing single alias models
			for(String model : ap.allMissingSingleAliasModels) {
				System.out.println(model);
			}
			break;
		case 5: // print missing pair alias models
			for(String model : ap.allMissingPairAliasModels) {
				System.out.println(model);
			}
			break;
		}
	}
	
	public static void main(String[] args) throws Exception {
		run("../results/first_server/", 0);
	}
}
