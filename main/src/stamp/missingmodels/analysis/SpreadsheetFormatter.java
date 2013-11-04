package stamp.missingmodels.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpreadsheetFormatter {
	private static String rootPath = "experiment3_results";
	private static String locPath = "experimentLOC_results";
	
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
		
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		File root = new File("../" + rootPath);
		List<String> appList = new ArrayList<String>();
		Map<String,Map<String,String>> resultInfo = new HashMap<String,Map<String,String>>();
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
				File locFile = new File(appDir, "loc.txt");
				BufferedReader br = new BufferedReader(new FileReader(locFile));
				br.readLine();
				String loc = "[" + br.readLine() + "," + br.readLine() + "]";
				br.close();
				Map<String,String> curResultInfo = resultInfo.get(appName);
				curResultInfo.put("Lines of code [app,framework]", loc);
			} catch(Exception e) {
				//e.printStackTrace();
			}
			
		}		
		
		for(String appName : appList) {
			//System.out.println(s);
			System.out.println(convert(appName, resultInfo.get(appName)));
		}
	}
}
