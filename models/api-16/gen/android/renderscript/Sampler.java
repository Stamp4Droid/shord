package android.renderscript;
public class Sampler
  extends android.renderscript.BaseObj
{
public static enum Value
{
CLAMP(),
LINEAR(),
LINEAR_MIP_LINEAR(),
LINEAR_MIP_NEAREST(),
NEAREST(),
WRAP();
}
public static class Builder
{
public  Builder(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public  void setMinification(android.renderscript.Sampler.Value v) { throw new RuntimeException("Stub!"); }
public  void setMagnification(android.renderscript.Sampler.Value v) { throw new RuntimeException("Stub!"); }
public  void setWrapS(android.renderscript.Sampler.Value v) { throw new RuntimeException("Stub!"); }
public  void setWrapT(android.renderscript.Sampler.Value v) { throw new RuntimeException("Stub!"); }
public  void setAnisotropy(float v) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Sampler create() { throw new RuntimeException("Stub!"); }
}
Sampler() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Sampler.Value getMinification() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Sampler.Value getMagnification() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Sampler.Value getWrapS() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Sampler.Value getWrapT() { throw new RuntimeException("Stub!"); }
public  float getAnisotropy() { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Sampler CLAMP_NEAREST(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Sampler CLAMP_LINEAR(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Sampler CLAMP_LINEAR_MIP_LINEAR(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Sampler WRAP_NEAREST(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Sampler WRAP_LINEAR(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
public static  android.renderscript.Sampler WRAP_LINEAR_MIP_LINEAR(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
}
