package stamp.app;

/*
 * @author Saswat Anand
*/
public class Widget
{
	private String className;
	public final String idStr;
	public final Integer id;

	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public Widget(String className, String idStr, Integer id)
	{
		this.className = className;
		this.idStr = idStr;
		this.id = id;
	}
}