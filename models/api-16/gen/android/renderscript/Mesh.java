package android.renderscript;
@Deprecated
public class Mesh
  extends android.renderscript.BaseObj
{
@Deprecated
public static enum Primitive
{
LINE(),
LINE_STRIP(),
POINT(),
TRIANGLE(),
TRIANGLE_FAN(),
TRIANGLE_STRIP();
}
@Deprecated
public static class Builder
{
@Deprecated
public  Builder(android.renderscript.RenderScript rs, int usage) { throw new RuntimeException("Stub!"); }
@Deprecated
public  int getCurrentVertexTypeIndex() { throw new RuntimeException("Stub!"); }
@Deprecated
public  int getCurrentIndexSetIndex() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.Builder addVertexType(android.renderscript.Type t) throws java.lang.IllegalStateException { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.Builder addVertexType(android.renderscript.Element e, int size) throws java.lang.IllegalStateException { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.Builder addIndexSetType(android.renderscript.Type t, android.renderscript.Mesh.Primitive p) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.Builder addIndexSetType(android.renderscript.Mesh.Primitive p) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.Builder addIndexSetType(android.renderscript.Element e, int size, android.renderscript.Mesh.Primitive p) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh create() { throw new RuntimeException("Stub!"); }
}
@Deprecated
public static class AllocationBuilder
{
@Deprecated
public  AllocationBuilder(android.renderscript.RenderScript rs) { throw new RuntimeException("Stub!"); }
@Deprecated
public  int getCurrentVertexTypeIndex() { throw new RuntimeException("Stub!"); }
@Deprecated
public  int getCurrentIndexSetIndex() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.AllocationBuilder addVertexAllocation(android.renderscript.Allocation a) throws java.lang.IllegalStateException { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.AllocationBuilder addIndexSetAllocation(android.renderscript.Allocation a, android.renderscript.Mesh.Primitive p) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.AllocationBuilder addIndexSetType(android.renderscript.Mesh.Primitive p) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh create() { throw new RuntimeException("Stub!"); }
}
@Deprecated
public static class TriangleMeshBuilder
{
@Deprecated
public  TriangleMeshBuilder(android.renderscript.RenderScript rs, int vtxSize, int flags) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.TriangleMeshBuilder addVertex(float x, float y) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.TriangleMeshBuilder addVertex(float x, float y, float z) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.TriangleMeshBuilder setTexture(float s, float t) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.TriangleMeshBuilder setNormal(float x, float y, float z) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.TriangleMeshBuilder setColor(float r, float g, float b, float a) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.TriangleMeshBuilder addTriangle(int idx1, int idx2, int idx3) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh create(boolean uploadToBufferObject) { throw new RuntimeException("Stub!"); }
@Deprecated
public static final int COLOR = 1;
@Deprecated
public static final int NORMAL = 2;
@Deprecated
public static final int TEXTURE_0 = 256;
}
Mesh() { throw new RuntimeException("Stub!"); }
@Deprecated
public  int getVertexAllocationCount() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Allocation getVertexAllocation(int slot) { throw new RuntimeException("Stub!"); }
@Deprecated
public  int getPrimitiveCount() { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Allocation getIndexSetAllocation(int slot) { throw new RuntimeException("Stub!"); }
@Deprecated
public  android.renderscript.Mesh.Primitive getPrimitive(int slot) { throw new RuntimeException("Stub!"); }
}
