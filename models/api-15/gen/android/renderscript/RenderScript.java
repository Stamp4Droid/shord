package android.renderscript;
public class RenderScript
{
public static class RSMessageHandler
  implements java.lang.Runnable
{
public  RSMessageHandler() { throw new RuntimeException("Stub!"); }
public  void run() { throw new RuntimeException("Stub!"); }
protected int[] mData = null;
protected int mID;
protected int mLength;
}
public static class RSErrorHandler
  implements java.lang.Runnable
{
public  RSErrorHandler() { throw new RuntimeException("Stub!"); }
public  void run() { throw new RuntimeException("Stub!"); }
protected java.lang.String mErrorMessage;
protected int mErrorNum;
}
public static enum Priority
{
LOW(),
NORMAL();
}
RenderScript() { throw new RuntimeException("Stub!"); }
public  void setMessageHandler(android.renderscript.RenderScript.RSMessageHandler msg) { throw new RuntimeException("Stub!"); }
public  android.renderscript.RenderScript.RSMessageHandler getMessageHandler() { throw new RuntimeException("Stub!"); }
public  void setErrorHandler(android.renderscript.RenderScript.RSErrorHandler msg) { throw new RuntimeException("Stub!"); }
public  android.renderscript.RenderScript.RSErrorHandler getErrorHandler() { throw new RuntimeException("Stub!"); }
public  void setPriority(android.renderscript.RenderScript.Priority p) { throw new RuntimeException("Stub!"); }
public final  android.content.Context getApplicationContext() { throw new RuntimeException("Stub!"); }
public static  android.renderscript.RenderScript create(android.content.Context ctx) { throw new RuntimeException("Stub!"); }
public  void contextDump() { throw new RuntimeException("Stub!"); }
public  void finish() { throw new RuntimeException("Stub!"); }
public  void destroy() { throw new RuntimeException("Stub!"); }
}
