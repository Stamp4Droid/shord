package stamp.missingmodels.util.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.Files;

public class AppAggregator {
	public static void main(String[] args) throws Exception {
		String appListFilePath = "../apps.txt";
		String appDirPath = "/Users/obastani/Documents/Files/stamp/stamptest/SymcApks/";
		String newAppDirPath = "../apps_agg/";
		
		BufferedReader br = new BufferedReader(new FileReader(appListFilePath));
		File appDir = new File(appDirPath);
		File newAppDir = new File(newAppDirPath);
		newAppDir.mkdirs();
		
		String line;
		Set<String> strs = new HashSet<String>();
		while((line = br.readLine()) != null) {
			boolean found = false;
			for(File file : appDir.listFiles()) {
				String[] tokens = file.getName().split("_");
				if(tokens[tokens.length - 1].equals(line.trim())) {
					Files.copy(file, new File(newAppDir, file.getName()));
					found = true;
					if(strs.contains(tokens[tokens.length - 1])) {
						System.out.println("Duplicate: " + tokens[tokens.length - 1]);
					}
					strs.add(tokens[tokens.length - 1]);
					break;
				}
			}
			if(!found) {
				System.out.println("Not found: " + line);
			}
		}
		br.close();
		System.out.println(strs.size());
	}
}
