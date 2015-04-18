package stamp.app;

import java.util.List;

/*
 * @author Saswat Anand
*/
public class Widget
{
	private String className;
	public final String idStr;
	public final Integer id;
	private boolean isCustom;
	private List<Widget> children;
	
	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public void setCustom()
	{
		this.isCustom = true;
	}
	
	public boolean isCustom()
	{
		return this.isCustom;
	}

	void setChildren(List<Widget> children)
	{
		this.children = children;
	}

	public List<Widget> getChildren()
	{
		return children;
	}

	public Widget(String className, String idStr, Integer id)
	{
		this.className = className;
		this.idStr = idStr;
		this.id = id;
	}
	
	public String toString()
	{
		return "{\"class\": \""+className+"\", \"id-str\": \""+idStr+"\", \"id\": "+id+"}";
	}
	
	public boolean equals(Object other)
	{
		if(!(other instanceof Widget))
			return false;
		Widget o = (Widget) other;
		return 
			o.className.equals(className) &&
			o.idStr.equals(idStr) &&
			o.id.equals(id);
	}
}