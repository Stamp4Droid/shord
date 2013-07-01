package android.net.http;
public final class HttpResponseCache
  extends java.net.ResponseCache
  implements java.io.Closeable
{
HttpResponseCache() { throw new RuntimeException("Stub!"); }
public static  android.net.http.HttpResponseCache getInstalled() { throw new RuntimeException("Stub!"); }
public static  android.net.http.HttpResponseCache install(java.io.File directory, long maxSize) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  java.net.CacheResponse get(java.net.URI uri, java.lang.String requestMethod, java.util.Map<java.lang.String, java.util.List<java.lang.String>> requestHeaders) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  java.net.CacheRequest put(java.net.URI uri, java.net.URLConnection urlConnection) throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  long size() { throw new RuntimeException("Stub!"); }
public  long maxSize() { throw new RuntimeException("Stub!"); }
public  void flush() { throw new RuntimeException("Stub!"); }
public  int getNetworkCount() { throw new RuntimeException("Stub!"); }
public  int getHitCount() { throw new RuntimeException("Stub!"); }
public  int getRequestCount() { throw new RuntimeException("Stub!"); }
public  void close() throws java.io.IOException { throw new RuntimeException("Stub!"); }
public  void delete() throws java.io.IOException { throw new RuntimeException("Stub!"); }
}
