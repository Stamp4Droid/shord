class AppWidgetProvider
{
    public  AppWidgetProvider() { 
        AppWidgetProvider.this.onReceive(null, new android.content.Intent());
        AppWidgetProvider.this.onUpdate(null, null, new int[1]);
        //AppWidgetProvider.this.onAppWidgetOptionsChanged(null, null, 1, null);
        AppWidgetProvider.this.onDeleted(null, new int[2]);
        AppWidgetProvider.this.onEnabled(null);
        AppWidgetProvider.this.onDisabled(null);
    }
}
