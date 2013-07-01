package android.renderscript;
public class Font
  extends android.renderscript.BaseObj
{
public static enum Style
{
BOLD(),
BOLD_ITALIC(),
ITALIC(),
NORMAL();
}
Font() { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Font createFromFile(android.renderscript.RenderScript rs, android.content.res.Resources res, java.lang.String path, float pointSize) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Font createFromFile(android.renderscript.RenderScript rs, android.content.res.Resources res, java.io.File path, float pointSize) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Font createFromAsset(android.renderscript.RenderScript rs, android.content.res.Resources res, java.lang.String path, float pointSize) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Font createFromResource(android.renderscript.RenderScript rs, android.content.res.Resources res, int id, float pointSize) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Font create(android.renderscript.RenderScript rs, android.content.res.Resources res, java.lang.String familyName, android.renderscript.Font.Style fontStyle, float pointSize) { throw new RuntimeException("Stub!"); }
}
