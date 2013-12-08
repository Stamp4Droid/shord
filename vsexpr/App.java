import java.util.*;

public class App
{
	private Map<String,String> results = new HashMap();
	private String id;

	App(String id){
		this.id = id;
	}
	
	void setResult(String tool, String family)
	{
		results.put(tool,family);
	}
	
	String getFamily(String tool)
	{
		return results.get(tool);
	}
}