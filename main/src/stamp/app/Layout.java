package stamp.app;

import java.util.*;

public class Layout
{
	public final List<Widget> widgets = new ArrayList();
	public final Set<String> callbacks = new HashSet();
	//public final int id;
	public final String fileName;
	private List<Component> comps = new ArrayList();

	Layout(/*int id,*/ String fileName)
	{
		//this.id = id;
		this.fileName = fileName;
	}
	
	public void addComponent(Component c)
	{
		comps.add(c);
	}
	
	public List<Component> getComponents()
	{
		return this.comps;
	}
}