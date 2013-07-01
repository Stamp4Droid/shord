package android.renderscript;
public class ProgramRaster
  extends android.renderscript.BaseObj
{
public static enum CullMode
{
BACK(),
FRONT(),
NONE();
}
public static class Builder
{
public  Builder(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramRaster.Builder setPointSpriteEnabled(boolean enable) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramRaster.Builder setCullMode(android.renderscript.ProgramRaster.CullMode m) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramRaster create() { throw new RuntimeException("Stub!"); }
}
ProgramRaster() { throw new RuntimeException("Stub!"); }
public static  android.renderscript.ProgramRaster CULL_BACK(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.ProgramRaster CULL_FRONT(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.ProgramRaster CULL_NONE(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
}
