package android.renderscript;
public class Script
  extends android.renderscript.BaseObj
{
public static class Builder
{
Builder() { throw new RuntimeException("Stub!"); }
}
public static class FieldBase
{
protected  FieldBase() { throw new RuntimeException("Stub!"); }
protected  void init(android.renderscript.RenderScript rs, int dimx) { throw new RuntimeException("Stub!"); }
protected  void init(android.renderscript.RenderScript rs, int dimx, int usages) { throw new RuntimeException("Stub!"); }
public  android.renderscript.Element getElement() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Type getType() { throw new RuntimeException("Stub!"); }
public  android.renderscript.Allocation getAllocation() { throw new RuntimeException("Stub!"); }
public  void updateAllocation() { throw new RuntimeException("Stub!"); }
protected android.renderscript.Element mElement;
protected android.renderscript.Allocation mAllocation;
}
Script() { throw new RuntimeException("Stub!"); }
protected  void invoke(int slot) { throw new RuntimeException("Stub!"); }
protected  void invoke(int slot, android.renderscript.FieldPacker v) { throw new RuntimeException("Stub!"); }
protected  void forEach(int slot, android.renderscript.Allocation ain, android.renderscript.Allocation aout, android.renderscript.FieldPacker v) { throw new RuntimeException("Stub!"); }
public  void bindAllocation(android.renderscript.Allocation va, int slot) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, float v) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, double v) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, int v) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, long v) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, boolean v) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, android.renderscript.BaseObj o) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, android.renderscript.FieldPacker v) { throw new RuntimeException("Stub!"); }
public  void setVar(int index, android.renderscript.FieldPacker v, android.renderscript.Element e, int[] dims) { throw new RuntimeException("Stub!"); }
public  void setTimeZone(java.lang.String timeZone) { throw new RuntimeException("Stub!"); }
}
