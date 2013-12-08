
import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class VirusTotal
{
	public static void main(String[] args)
	{
		String fileName = args[0];
		String name = new File(fileName).getName();
		String id = name.substring(0, name.length()-4);

		App app = new App(id);

		try {
            JSONObject targetJSON = (JSONObject) (new JSONParser())
				.parse(readFileAsString(fileName));

            JSONObject scansJSON = (JSONObject) targetJSON.get("scans");
			
			Set<String> tools = (Set<String>) scansJSON.keySet();
            for(String tool : tools) {
                JSONObject toolResultJSON = (JSONObject) scansJSON.get(tool);
				boolean detected = (Boolean) toolResultJSON.get("detected");
				if(detected){
					String family = (String) toolResultJSON.get("result");
					System.out.println(tool + " " + family);
					app.setResult(tool, family);
				} else
					System.out.println(tool + " " + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

	}
	
    private static String readFileAsString(String filePath) throws IOException 
	{
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

}