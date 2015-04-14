package android.view;

public class StampLayoutInflater extends LayoutInflater
{
	public StampLayoutInflater(android.content.Context context)
	{
		super(context);
	}

	public android.view.LayoutInflater cloneInContext(android.content.Context newContext)
	{
		return this; //TODO: fix
	}
}