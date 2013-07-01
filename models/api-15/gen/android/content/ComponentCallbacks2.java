package android.content;
public interface ComponentCallbacks2
  extends android.content.ComponentCallbacks
{
public abstract  void onTrimMemory(int level);
public static final int TRIM_MEMORY_COMPLETE = 80;
public static final int TRIM_MEMORY_MODERATE = 60;
public static final int TRIM_MEMORY_BACKGROUND = 40;
public static final int TRIM_MEMORY_UI_HIDDEN = 20;
}
