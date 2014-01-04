import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class AppWidgetProvider
{
    public  AppWidgetProvider() 
	{ 
		ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						android.content.Context context = android.test.mock.MockContext.g;

						AppWidgetProvider.this.onReceive(context, new android.content.Intent());
						AppWidgetProvider.this.onUpdate(context, null, new int[1]);
						//AppWidgetProvider.this.onAppWidgetOptionsChanged(null, null, 1, null);
						AppWidgetProvider.this.onDeleted(context, new int[2]);
						AppWidgetProvider.this.onEnabled(context);
						AppWidgetProvider.this.onDisabled(context);
					}
				});
    }
}
