package stamp.harnessgen;

import java.util.Set;
import java.util.HashSet;

public class Layout
{
	final Set<String> customWidgets = new HashSet();
	final Set<String> callbacks = new HashSet();
	final int id;
	
	Layout(int id)
	{
		this.id = id;
	}
	
}