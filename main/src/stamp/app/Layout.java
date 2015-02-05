package stamp.app;

import java.util.*;

public class Layout
{
	public final List<Widget> widgets = new ArrayList();
	public final Set<String> callbacks = new HashSet();
	public final int id;
	public final String fileName;

	Layout(int id, String fileName)
	{
		this.id = id;
		this.fileName = fileName;
	}
	
}