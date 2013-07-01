package android.renderscript;
public class Type
  extends android.renderscript.BaseObj
{
public static enum CubemapFace
{
NEGATIVE_X(),
NEGATIVE_Y(),
NEGATIVE_Z(),
POSITIVE_X(),
POSITIVE_Y(),
POSITIVE_Z(),
POSITVE_X(),
POSITVE_Y(),
POSITVE_Z();
}
public static class Builder
{
public  Builder(android.renderscript.RenderScript rs, android.renderscript.Element e) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Type.Builder setX(int value) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Type.Builder setY(int value) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Type.Builder setMipmaps(boolean value) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Type.Builder setFaces(boolean value) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Type create() { throw new RuntimeException("Stub!"); }
}
Type() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Element getElement() { throw new RuntimeException("Stub!"); }
public  int getX() { throw new RuntimeException("Stub!"); }
public  int getY() { throw new RuntimeException("Stub!"); }
public  int getZ() { throw new RuntimeException("Stub!"); }
public  boolean hasMipmaps() { throw new RuntimeException("Stub!"); }
public  boolean hasFaces() { throw new RuntimeException("Stub!"); }
public  int getCount() { throw new RuntimeException("Stub!"); }
}
