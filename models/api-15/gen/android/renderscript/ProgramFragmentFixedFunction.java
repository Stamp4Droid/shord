package android.renderscript;
public class ProgramFragmentFixedFunction
  extends android.renderscript.ProgramFragment
{
public static class Builder
{
public static enum EnvMode
{
DECAL(),
MODULATE(),
REPLACE();
}
public static enum Format
{
ALPHA(),
LUMINANCE_ALPHA(),
RGB(),
RGBA();
}
public  Builder(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramFragmentFixedFunction.Builder setTexture(android.renderscript.ProgramFragmentFixedFunction.Builder.EnvMode env, android.renderscript.ProgramFragmentFixedFunction.Builder.Format fmt, int slot) throws java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramFragmentFixedFunction.Builder setPointSpriteTexCoordinateReplacement(boolean enable) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramFragmentFixedFunction.Builder setVaryingColor(boolean enable) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramFragmentFixedFunction create() { throw new RuntimeException("Stub!"); }
public static final int MAX_TEXTURE = 2;
}
ProgramFragmentFixedFunction() { throw new RuntimeException("Stub!"); }
}
