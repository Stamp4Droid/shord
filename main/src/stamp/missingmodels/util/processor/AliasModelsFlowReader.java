package stamp.missingmodels.util.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stamp.missingmodels.util.processor.AliasModelsProcessor.ModelReader;

public class AliasModelsFlowReader {
	public static Map<String,Map<Integer,Integer>> getFlows() throws Exception {
		String dirPath = new String("../../results/compare");
		
		File dir = new File(dirPath);
		Map<String,Map<Integer,Integer>> map = new HashMap<String,Map<Integer,Integer>>();
		List<String> apps = new ArrayList<String>();
		for(File file : dir.listFiles()) {
			if(file.getName().startsWith("flow")) {
				int index = Integer.parseInt("" + file.getName().charAt(4));
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while((line = br.readLine()) != null) {
					String[] tokens = line.split("_");
					String appName = tokens[tokens.length-1].split("/")[0];
					int numFlows = (int)Float.parseFloat(line.split(" ")[4]);
					if(!map.containsKey(appName)) {
						map.put(appName, new HashMap<Integer,Integer>());
					}
					map.get(appName).put(index, numFlows);
					if(index == 1) {
						apps.add(appName);
					}
				}
				br.close();
			}
		}
		return map;
	}
	
	public static void run() throws Exception {
		Map<String,Map<Integer,Integer>> map = getFlows();
		
		AliasModelsProcessor ap = new AliasModelsProcessor(ModelReader.readMap("../../results/models/alias_models_loadstore.txt"), ModelReader.readMap("../../results/models/alias_models_assign.txt"), ModelReader.readMap("../../results/models/phantom_object_models.txt"), ModelReader.readMap("../../results/models/alias_models_long.txt"));		
		new LogReader("../../results/first_server/", ap).run();		
		
		System.out.println("No Models & Inferred Models & No Alias Models & Inferred Alias Models & All Models\\");
		float infDnone = 0.0f;
		int infDnoneCount = 0;
		float infDall = 0.0f;
		int infDallCount = 0;
		int[] counts = new int[6];
		for(String appName : ap.getAppNames()) {
			if(!map.containsKey(appName)) {
				continue;
			}
			for(int i=1; i<6; i++) {
				counts[i] += map.get(appName).get(i);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(appName);
			sb.append(" & " + map.get(appName).get(5));
			sb.append(" & " + map.get(appName).get(3));
			sb.append(" & " + map.get(appName).get(4));
			sb.append(" & " + map.get(appName).get(1));
			sb.append(" & " + map.get(appName).get(2));
			float infDnoneCur = (float)map.get(appName).get(3)/map.get(appName).get(5);
			sb.append(" & " + infDnoneCur);
			if(infDnoneCur <= 100000000.0f) {
				infDnone += infDnoneCur;
				infDnoneCount++;
			}
			float infDallCur = (float)map.get(appName).get(3)/map.get(appName).get(2);
			if(infDallCur <= 1.0) {
				infDall += infDallCur;
				infDallCount++;
			}
			sb.append(" & " + infDallCur);
			System.out.println(sb.toString());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Total");
		sb.append(" & " + counts[5]);
		sb.append(" & " + counts[3]);
		sb.append(" & " + counts[4]);
		sb.append(" & " + counts[1]);
		sb.append(" & " + counts[2]);
		sb.append(" & " + (float)counts[3] / counts[5]);
		sb.append(" & " + (float)counts[3] / counts[2]);
		System.out.println(sb.toString());
		System.out.println(infDnone/infDnoneCount);
		System.out.println(infDall/infDallCount);
	}
	
	public static void main(String[] args) throws Exception {
		run();
	}
}
