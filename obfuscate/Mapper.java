import java.util.*;
import java.io.*;

public class Mapper
{
	public Map<String,String> oldToNewClassName = new HashMap();
	private String mappingFile;

	Mapper(String mappingFile)
	{
		this.mappingFile = mappingFile;
		readMappingFile();
	}

	public void readMappingFile()
	{
		try{
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
		}catch(IOException e){
			throw new Error(e);
		}
	}
	
}