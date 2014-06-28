package stamp.app;

public class Widget
{
	public final String name;
	public final String id;
	private boolean custom = false;

	public Widget(String name, String id)
	{
		this.name = name;
		this.id = id;
	}

	public Widget(String name)
	{
		this(name, null);
	}	

	public void setCustom()
	{
		this.custom = true;
	}

	public boolean isCustom()
	{
		return custom;
	}
}