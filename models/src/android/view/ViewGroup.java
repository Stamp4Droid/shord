class ViewGroup
{
	public android.view.View child;

	public  void addView(android.view.View child) { this.child = child; }

	public  void addView(android.view.View child, int index) { this.child = child; }

	public  void addView(android.view.View child, int width, int height) { this.child = child; }

	public  void addView(android.view.View child, android.view.ViewGroup.LayoutParams params) { this.child = child; }

	public  void addView(android.view.View child, int index, android.view.ViewGroup.LayoutParams params) { this.child = child; }
}