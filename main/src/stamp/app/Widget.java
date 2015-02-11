package stamp.app;

/*
 * @author Saswat Anand
*/
public class Widget
{
	private String className;
	public final String idStr;
	public final Integer id;
	private boolean isCustom;

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

	public Widget(String className, String idStr, Integer id)
	{
		this.className = className;
		this.idStr = idStr;
		this.id = id;
	}
	
	public String toString()
	{
		return "{class: "+className+", id-str: "+idStr+", id: "+id+"}";
	}
}