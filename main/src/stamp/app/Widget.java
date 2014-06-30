package stamp.app;

public class Widget
{
	public final String name;
	public final String id;
	public final Layout layout;
	private boolean custom = false;

	public Widget(String name, String id, Layout layout)
	{
		this.name = name;
		this.id = id;
		this.layout = layout;
	}

	public Widget(String name, Layout layout)
	{
		this(name, null, layout);
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