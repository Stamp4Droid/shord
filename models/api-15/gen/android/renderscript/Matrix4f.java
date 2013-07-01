package android.renderscript;
public class Matrix4f
{
public  Matrix4f() { throw new RuntimeException("Stub!"); }
public  Matrix4f(float[] dataArray) { throw new RuntimeException("Stub!"); }
public  float[] getArray() { throw new RuntimeException("Stub!"); }
public  float get(int i, int j) { throw new RuntimeException("Stub!"); }
public  void set(int i, int j, float v) { throw new RuntimeException("Stub!"); }
public  void loadIdentity() { throw new RuntimeException("Stub!"); }
public  void load(android.renderscript.Matrix4f src) { throw new RuntimeException("Stub!"); }
public  void loadRotate(float rot, float x, float y, float z) { throw new RuntimeException("Stub!"); }
public  void loadScale(float x, float y, float z) { throw new RuntimeException("Stub!"); }
public  void loadTranslate(float x, float y, float z) { throw new RuntimeException("Stub!"); }
public  void loadMultiply(android.renderscript.Matrix4f lhs, android.renderscript.Matrix4f rhs) { throw new RuntimeException("Stub!"); }
public  void loadOrtho(float l, float r, float b, float t, float n, float f) { throw new RuntimeException("Stub!"); }
public  void loadOrthoWindow(int w, int h) { throw new RuntimeException("Stub!"); }
public  void loadFrustum(float l, float r, float b, float t, float n, float f) { throw new RuntimeException("Stub!"); }
public  void loadPerspective(float fovy, float aspect, float near, float far) { throw new RuntimeException("Stub!"); }
public  void loadProjectionNormalized(int w, int h) { throw new RuntimeException("Stub!"); }
public  void multiply(android.renderscript.Matrix4f rhs) { throw new RuntimeException("Stub!"); }
public  void rotate(float rot, float x, float y, float z) { throw new RuntimeException("Stub!"); }
public  void scale(float x, float y, float z) { throw new RuntimeException("Stub!"); }
public  void translate(float x, float y, float z) { throw new RuntimeException("Stub!"); }
public  boolean inverse() { throw new RuntimeException("Stub!"); }
public  boolean inverseTranspose() { throw new RuntimeException("Stub!"); }
public  void transpose() { throw new RuntimeException("Stub!"); }
}
