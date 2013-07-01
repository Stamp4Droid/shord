package android.os;
public final class CancellationSignal
{
public static interface OnCancelListener
{
public abstract  void onCancel();
}
public  CancellationSignal() { throw new RuntimeException("Stub!"); }
public  boolean isCanceled() { throw new RuntimeException("Stub!"); }
public  void throwIfCanceled() { throw new RuntimeException("Stub!"); }
public  void cancel() { throw new RuntimeException("Stub!"); }
public  void setOnCancelListener(android.os.CancellationSignal.OnCancelListener listener) { throw new RuntimeException("Stub!"); }
}
