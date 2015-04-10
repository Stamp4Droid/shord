class LayoutInflater
{
	public static  android.view.LayoutInflater from(android.content.Context context) { 
		return new StampLayoutInflater(context);
	}
}