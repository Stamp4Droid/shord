package android.renderscript;
public class ProgramStore
  extends android.renderscript.BaseObj
{
public static enum DepthFunc
{
ALWAYS(),
EQUAL(),
GREATER(),
GREATER_OR_EQUAL(),
LESS(),
LESS_OR_EQUAL(),
NOT_EQUAL();
}
public static enum BlendSrcFunc
{
DST_ALPHA(),
DST_COLOR(),
ONE(),
ONE_MINUS_DST_ALPHA(),
ONE_MINUS_DST_COLOR(),
ONE_MINUS_SRC_ALPHA(),
SRC_ALPHA(),
SRC_ALPHA_SATURATE(),
ZERO();
}
public static enum BlendDstFunc
{
DST_ALPHA(),
ONE(),
ONE_MINUS_DST_ALPHA(),
ONE_MINUS_SRC_ALPHA(),
ONE_MINUS_SRC_COLOR(),
SRC_ALPHA(),
SRC_COLOR(),
ZERO();
}
public static class Builder
{
public  Builder(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.Builder setDepthFunc(android.renderscript.ProgramStore.DepthFunc func) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.Builder setDepthMaskEnabled(boolean enable) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.Builder setColorMaskEnabled(boolean r, boolean g, boolean b, boolean a) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.Builder setBlendFunc(android.renderscript.ProgramStore.BlendSrcFunc src, android.renderscript.ProgramStore.BlendDstFunc dst) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.Builder setDitherEnabled(boolean enable) { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore create() { throw new RuntimeException("Stub!"); }
}
ProgramStore() { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.DepthFunc getDepthFunc() { throw new RuntimeException("Stub!"); }
public  boolean isDepthMaskEnabled() { throw new RuntimeException("Stub!"); }
public  boolean isColorMaskRedEnabled() { throw new RuntimeException("Stub!"); }
public  boolean isColorMaskGreenEnabled() { throw new RuntimeException("Stub!"); }
public  boolean isColorMaskBlueEnabled() { throw new RuntimeException("Stub!"); }
public  boolean isColorMaskAlphaEnabled() { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.BlendSrcFunc getBlendSrcFunc() { throw new RuntimeException("Stub!"); }
public  android.renderscript.ProgramStore.BlendDstFunc getBlendDstFunc() { throw new RuntimeException("Stub!"); }
public  boolean isDitherEnabled() { throw new RuntimeException("Stub!"); }
public static  android.renderscript.ProgramStore BLEND_NONE_DEPTH_TEST(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.ProgramStore BLEND_NONE_DEPTH_NONE(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.ProgramStore BLEND_ALPHA_DEPTH_TEST(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.ProgramStore BLEND_ALPHA_DEPTH_NONE(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
}
