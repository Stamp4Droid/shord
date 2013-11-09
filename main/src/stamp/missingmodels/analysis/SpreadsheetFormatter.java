package stamp.missingmodels.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpreadsheetFormatter {
	private static String rootPath = "experiment3_results";
	private static String locPath = "experimentLOC_results";
	
	private static int correctModels = 0;
	private static int totalModels = 0;
	
	public static String convert(String appName, Map<String,String> resultInfo) {
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append("\t");

		// get jimple LOC for app
		String loc = resultInfo.get("Lines of code [app,framework]");
		loc = loc.substring(1,loc.length()-1);
		String[] locTokens = loc.split(",");
		sb.append(locTokens[0]).append("\t");
		
		sb.append(resultInfo.get("Number of proposed models")).append("\t");
		sb.append(resultInfo.get("Number of rounds")).append("\t");
		sb.append(resultInfo.get("Accuracy")).append("\t");
		
		// get number of before/after flows
		String flow = resultInfo.get("Number of flows [before,after]");
		flow = flow.substring(1,flow.length()-1);
		String[] flowTokens = flow.split(",");
		sb.append(flowTokens[0]).append("\t").append(flowTokens[1]).append("\t");
		
		sb.append(resultInfo.get("Maximum running time")).append("\t");
		
		// set some global parameters
		correctModels += (int)(Double.parseDouble(resultInfo.get("Accuracy"))*Integer.parseInt(resultInfo.get("Number of proposed models")));
		totalModels += Integer.parseInt(resultInfo.get("Number of proposed models"));
		
		
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		File root = new File("../" + rootPath);
		List<String> appList = new ArrayList<String>();
		final Map<String,Map<String,String>> resultInfo = new HashMap<String,Map<String,String>>();
		for(File appDir : root.listFiles()) {
			Map<String,String> curResultInfo = new HashMap<String,String>();
			String[] appTokens = appDir.getName().split("_");
			String appName = appTokens[appTokens.length-1];
			try {
				File resultFile = new File(appDir, "cfl/results.txt");
				BufferedReader br = new BufferedReader(new FileReader(resultFile));
				String line;
				while((line = br.readLine()) != null) {
					String[] tokens = line.split(": ");
					if(tokens.length == 2) {
						curResultInfo.put(tokens[0], tokens[1]);
					}
				}
				br.close();
				resultInfo.put(appName, curResultInfo);
				appList.add(appName);
			} catch(IOException e) {
				//e.printStackTrace();
			}
		}
		
		// FIX LINES OF CODE
		File locRoot = new File("../" + locPath);
		for(File appDir : locRoot.listFiles()) {
			try {
				String[] appTokens = appDir.getName().split("_");
				String appName = appTokens[appTokens.length-1];
				Map<String,String> curResultInfo = resultInfo.get(appName);
				curResultInfo.put("Lines of app code", "-1");
				File locFile = new File(appDir, "loc.txt");
				BufferedReader br = new BufferedReader(new FileReader(locFile));
				br.readLine();
				String appLoc = br.readLine();
				String loc = "[" + appLoc + "," + br.readLine() + "]";
				br.close();
				curResultInfo.put("Lines of code [app,framework]", loc);
				// add the jimple LOC
				curResultInfo.put("Lines of app code", appLoc);
			} catch(Exception e) {
				//e.printStackTrace();
			}
		}
		
		// Sort by lines of code
		Collections.sort(appList, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				int loc0 = Integer.parseInt(resultInfo.get(arg0).get("Lines of app code"));
				int loc1 = Integer.parseInt(resultInfo.get(arg1).get("Lines of app code"));
				if(loc0 > loc1) {
					return -1;
				} else if(loc0 == loc1) {
					return 0;
				} else {
					return 1;
				}
			}
		});
		
		for(String appName : appList) {
			//System.out.println(s);
			System.out.println(convert(appName, resultInfo.get(appName)));
		}
		System.out.println(correctModels + "," + totalModels);
	}
}
