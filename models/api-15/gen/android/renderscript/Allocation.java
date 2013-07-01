package android.renderscript;
public class Allocation
  extends android.renderscript.BaseObj
{
public static enum MipmapControl
{
MIPMAP_FULL(),
MIPMAP_NONE(),
MIPMAP_ON_SYNC_TO_TEXTURE();
}
Allocation() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Type getType() { throw new RuntimeException("Stub!"); }
public  void syncAll(int srcLocation) { throw new RuntimeException("Stub!"); }
public  void copyFrom(android.renderscript.BaseObj[] d) { throw new RuntimeException("Stub!"); }
public  void copyFromUnchecked(int[] d) { throw new RuntimeException("Stub!"); }
public  void copyFromUnchecked(short[] d) { throw new RuntimeException("Stub!"); }
public  void copyFromUnchecked(byte[] d) { throw new RuntimeException("Stub!"); }
public  void copyFromUnchecked(float[] d) { throw new RuntimeException("Stub!"); }
public  void copyFrom(int[] d) { throw new RuntimeException("Stub!"); }
public  void copyFrom(short[] d) { throw new RuntimeException("Stub!"); }
public  void copyFrom(byte[] d) { throw new RuntimeException("Stub!"); }
public  void copyFrom(float[] d) { throw new RuntimeException("Stub!"); }
public  void copyFrom(android.graphics.Bitmap b) { throw new RuntimeException("Stub!"); }
public  void setFromFieldPacker(int xoff, android.renderscript.FieldPacker fp) { throw new RuntimeException("Stub!"); }
public  void setFromFieldPacker(int xoff, int component_number, android.renderscript.FieldPacker fp) { throw new RuntimeException("Stub!"); }
public  void generateMipmaps() { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFromUnchecked(int off, int count, int[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFromUnchecked(int off, int count, short[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFromUnchecked(int off, int count, byte[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFromUnchecked(int off, int count, float[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFrom(int off, int count, int[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFrom(int off, int count, short[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFrom(int off, int count, byte[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFrom(int off, int count, float[] d) { throw new RuntimeException("Stub!"); }
public  void copy1DRangeFrom(int off, int count, android.renderscript.Allocation data, int dataOff) { throw new RuntimeException("Stub!"); }
public  void copy2DRangeFrom(int xoff, int yoff, int w, int h, byte[] data) { throw new RuntimeException("Stub!"); }
public  void copy2DRangeFrom(int xoff, int yoff, int w, int h, short[] data) { throw new RuntimeException("Stub!"); }
public  void copy2DRangeFrom(int xoff, int yoff, int w, int h, int[] data) { throw new RuntimeException("Stub!"); }
public  void copy2DRangeFrom(int xoff, int yoff, int w, int h, float[] data) { throw new RuntimeException("Stub!"); }
public  void copy2DRangeFrom(int xoff, int yoff, int w, int h, android.renderscript.Allocation data, int dataXoff, int dataYoff) { throw new RuntimeException("Stub!"); }
public  void copy2DRangeFrom(int xoff, int yoff, android.graphics.Bitmap data) { throw new RuntimeException("Stub!"); }
public  void copyTo(android.graphics.Bitmap b) { throw new RuntimeException("Stub!"); }
public  void copyTo(byte[] d) { throw new RuntimeException("Stub!"); }
public  void copyTo(short[] d) { throw new RuntimeException("Stub!"); }
public  void copyTo(int[] d) { throw new RuntimeException("Stub!"); }
public  void copyTo(float[] d) { throw new RuntimeException("Stub!"); }
public synchronized  void resize(int dimX) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createTyped(android.renderscript.RenderScript rs, android.renderscript.Type type, android.renderscript.Allocation.MipmapControl mips, int usage) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createTyped(android.renderscript.RenderScript rs, android.renderscript.Type type, int usage) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createTyped(android.renderscript.RenderScript rs, android.renderscript.Type type) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createSized(android.renderscript.RenderScript rs, android.renderscript.Element e, int count, int usage) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createSized(android.renderscript.RenderScript rs, android.renderscript.Element e, int count) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createFromBitmap(android.renderscript.RenderScript rs, android.graphics.Bitmap b, android.renderscript.Allocation.MipmapControl mips, int usage) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createFromBitmap(android.renderscript.RenderScript rs, android.graphics.Bitmap b) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createCubemapFromBitmap(android.renderscript.RenderScript rs, android.graphics.Bitmap b, android.renderscript.Allocation.MipmapControl mips, int usage) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createCubemapFromBitmap(android.renderscript.RenderScript rs, android.graphics.Bitmap b) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createCubemapFromCubeFaces(android.renderscript.RenderScript rs, android.graphics.Bitmap xpos, android.graphics.Bitmap xneg, android.graphics.Bitmap ypos, android.graphics.Bitmap yneg, android.graphics.Bitmap zpos, android.graphics.Bitmap zneg, android.renderscript.Allocation.MipmapControl mips, int usage) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createCubemapFromCubeFaces(android.renderscript.RenderScript rs, android.graphics.Bitmap xpos, android.graphics.Bitmap xneg, android.graphics.Bitmap ypos, android.graphics.Bitmap yneg, android.graphics.Bitmap zpos, android.graphics.Bitmap zneg) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createFromBitmapResource(android.renderscript.RenderScript rs, android.content.res.Resources res, int id, android.renderscript.Allocation.MipmapControl mips, int usage) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createFromBitmapResource(android.renderscript.RenderScript rs, android.content.res.Resources res, int id) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Allocation createFromString(android.renderscript.RenderScript rs, java.lang.String str, int usage) { throw new RuntimeException("Stub!"); }
public static final int USAGE_SCRIPT = 1;
public static final int USAGE_GRAPHICS_TEXTURE = 2;
public static final int USAGE_GRAPHICS_VERTEX = 4;
public static final int USAGE_GRAPHICS_CONSTANTS = 8;
public static final int USAGE_GRAPHICS_RENDER_TARGET = 16;
}
