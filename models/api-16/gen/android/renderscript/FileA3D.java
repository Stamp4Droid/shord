package android.renderscript;
@Deprecated
public class FileA3D
  extends android.renderscript.BaseObj
{
@Deprecated
public static enum EntryType
{
MESH(),
UNKNOWN();
}
@Deprecated
public static class IndexEntry
{
IndexEntry() { throw new RuntimeException("Stub!"); }
@Deprecated
public  java.lang.String getName() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.FileA3D.EntryType getEntryType() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.BaseObj getObject() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh getMesh() { throw new RuntimeException("Stub!"); }
}
FileA3D() { throw new RuntimeException("Stub!"); }
@Deprecated
public  int getIndexEntryCount() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.FileA3D.IndexEntry getIndexEntry(int index) { throw new RuntimeException("Stub!"); }
@Deprecated
public static  android.renderscript.FileA3D createFromAsset(android.renderscript.RenderScript rs, android.content.res.AssetManager mgr, java.lang.String path) { throw new RuntimeException("Stub!"); }
@Deprecated
public static  android.renderscript.FileA3D createFromFile(android.renderscript.RenderScript rs, java.lang.String path) { throw new RuntimeException("Stub!"); }
@Deprecated
public static  android.renderscript.FileA3D createFromFile(android.renderscript.RenderScript rs, java.io.File path) { throw new RuntimeException("Stub!"); }
@Deprecated
public static  android.renderscript.FileA3D createFromResource(android.renderscript.RenderScript rs, android.content.res.Resources res, int id) { throw new RuntimeException("Stub!"); }
}
