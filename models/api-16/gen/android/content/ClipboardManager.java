package android.content;
public class ClipboardManager
  extends android.text.ClipboardManager
{
public static interface OnPrimaryClipChangedListener
{
public abstract  void onPrimaryClipChanged();
}
ClipboardManager() { throw new RuntimeException("Stub!"); }
public  void setPrimaryClip(android.content.ClipData clip) { throw new RuntimeException("Stub!"); }
public  android.content.ClipData getPrimaryClip() { throw new RuntimeException("Stub!"); }
public  android.content.ClipDescription getPrimaryClipDescription() { throw new RuntimeException("Stub!"); }
public  boolean hasPrimaryClip() { throw new RuntimeException("Stub!"); }
public  void addPrimaryClipChangedListener(android.content.ClipboardManager.OnPrimaryClipChangedListener what) { throw new RuntimeException("Stub!"); }
public  void removePrimaryClipChangedListener(android.content.ClipboardManager.OnPrimaryClipChangedListener what) { throw new RuntimeException("Stub!"); }
@Deprecated
public  java.lang.CharSequence getText() { throw new RuntimeException("Stub!"); }
@Deprecated
public  void setText(java.lang.CharSequence text) { throw new RuntimeException("Stub!"); }
@Deprecated
public  boolean hasText() { throw new RuntimeException("Stub!"); }
}
