package stamp.app;

import java.util.Set;
import java.util.HashSet;

public class Layout
{
	public final Set<String> customWidgets = new HashSet();
	public final Set<String> callbacks = new HashSet();
	//public final int id;
	public final String fileName;

	Layout(/*int id,*/ String fileName)
	{
		//this.id = id;
		this.fileName = fileName;
	}
	
}