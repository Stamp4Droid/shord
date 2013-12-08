import java.util.*;
import java.io.*;

public class FixManifest
{
	static Map<String,String> oldToNewClassName = new HashMap();

	public static void main(String[] args) throws Exception
	{
		String mappingFile = args[0];
		String manifestFile = args[1];
		
		
		readMappingFile(mappingFile);
		fixManifest(manifestFile);
	}

	private static void fixManifest(String manifestFile) throws Exception
	{
		File tmpFile = File.createTempFile("stamp_android_manifest", null, null);
		tmpFile.deleteOnExit();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile)));

		BufferedReader reader = new BufferedReader(new FileReader(manifestFile));
		String line;
		while((line = reader.readLine()) != null){
			for(Map.Entry<String,String> entry : oldToNewClassName.entrySet()){
				String oldName = entry.getKey();
				String newName = entry.getValue();
				line = line.replaceAll(oldName, newName);
			}
			writer.println(line);
		}
		reader.close();
		writer.close();
		
		copy(tmpFile, new File(manifestFile));
	}

	private static void readMappingFile(String mappingFile) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
		String line;
		while((line = reader.readLine()) != null){
			if(line.endsWith(":")){
				line = line.substring(0, line.length()-1);
				int i = line.indexOf("->");
				String oldName = line.substring(0, i-1);
				String newName = line.substring(i+2);
				System.out.println(oldName+" "+newName);
				oldToNewClassName.put(oldName, newName);
			}
		}
		reader.close();
	}

	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	public static void copy(File srcFile, File dstFile) throws IOException
	{
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new BufferedInputStream(new FileInputStream(srcFile), DEFAULT_BUFFER_SIZE);
			output = new BufferedOutputStream(new FileOutputStream(dstFile), DEFAULT_BUFFER_SIZE);
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			for (int length = 0; ((length = input.read(buffer)) > 0);) {
				output.write(buffer, 0, length);
			}
		} finally {
			if (output != null) try { output.close(); } catch (IOException e) { throw e; }
			if (input != null) try { input.close(); } catch (IOException e) { throw e; }
		}
	}


}