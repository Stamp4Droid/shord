package android.renderscript;
public class Program
  extends android.renderscript.BaseObj
{
public static enum TextureType
{
TEXTURE_2D(),
TEXTURE_CUBE();
}
public static class BaseProgramBuilder
{
protected  BaseProgramBuilder(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Program.BaseProgramBuilder setShader(java.lang.String s) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Program.BaseProgramBuilder setShader(android.content.res.Resources resources, int resourceID) { throw new RuntimeException("Stub!"); }
public  int getCurrentConstantIndex() { throw new RuntimeException("Stub!"); }
public  int getCurrentTextureIndex() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Program.BaseProgramBuilder addConstant(android.renderscript.Type t) throws java.lang.IllegalStateException { throw new RuntimeException("Stub!"); }
public  android.renderscript.Program.BaseProgramBuilder addTexture(android.renderscript.Program.TextureType texType) throws java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
protected  void initProgram(android.renderscript.Program p) { throw new RuntimeException("Stub!"); }
}
Program() { throw new RuntimeException("Stub!"); }
public  void bindConstants(android.renderscript.Allocation a, int slot) { throw new RuntimeException("Stub!"); }
public  void bindTexture(android.renderscript.Allocation va, int slot) throws java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
public  void bindSampler(android.renderscript.Sampler vs, int slot) throws java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
}
