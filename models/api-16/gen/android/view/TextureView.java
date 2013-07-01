package android.view;
public class TextureView
  extends android.view.View
{
public static interface SurfaceTextureListener
{
public abstract  void onSurfaceTextureAvailable(android.graphics.SurfaceTexture surface, int width, int height);
public abstract  void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture surface, int width, int height);
public abstract  boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture surface);
public abstract  void onSurfaceTextureUpdated(android.graphics.SurfaceTexture surface);
}
public  TextureView(android.content.Context context) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
@java.lang.SuppressWarnings(value={"UnusedDeclaration"})
public  TextureView(android.content.Context context, android.util.AttributeSet attrs) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
@java.lang.SuppressWarnings(value={"UnusedDeclaration"})
public  TextureView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) { super((android.content.Context)null,(android.util.AttributeSet)null,0); throw new RuntimeException("Stub!"); }
public  boolean isOpaque() { throw new RuntimeException("Stub!"); }
public  void setOpaque(boolean opaque) { throw new RuntimeException("Stub!"); }
protected  void onAttachedToWindow() { throw new RuntimeException("Stub!"); }
protected  void onDetachedFromWindow() { throw new RuntimeException("Stub!"); }
public  void setLayerType(int layerType, android.graphics.Paint paint) { throw new RuntimeException("Stub!"); }
public  int getLayerType() { throw new RuntimeException("Stub!"); }
public  void buildLayer() { throw new RuntimeException("Stub!"); }
public final  void draw(android.graphics.Canvas canvas) { throw new RuntimeException("Stub!"); }
protected final  void onDraw(android.graphics.Canvas canvas) { throw new RuntimeException("Stub!"); }
protected  void onSizeChanged(int w, int h, int oldw, int oldh) { throw new RuntimeException("Stub!"); }
protected  void onVisibilityChanged(android.view.View changedView, int visibility) { throw new RuntimeException("Stub!"); }
public  void setTransform(android.graphics.Matrix transform) { throw new RuntimeException("Stub!"); }
public  android.graphics.Matrix getTransform(android.graphics.Matrix transform) { throw new RuntimeException("Stub!"); }
public  android.graphics.Bitmap getBitmap() { throw new RuntimeException("Stub!"); }
public  android.graphics.Bitmap getBitmap(int width, int height) { throw new RuntimeException("Stub!"); }
public  android.graphics.Bitmap getBitmap(android.graphics.Bitmap bitmap) { throw new RuntimeException("Stub!"); }
public  boolean isAvailable() { throw new RuntimeException("Stub!"); }
public  android.graphics.Canvas lockCanvas() { throw new RuntimeException("Stub!"); }
public  android.graphics.Canvas lockCanvas(android.graphics.Rect dirty) { throw new RuntimeException("Stub!"); }
public  void unlockCanvasAndPost(android.graphics.Canvas canvas) { throw new RuntimeException("Stub!"); }
public  android.graphics.SurfaceTexture getSurfaceTexture() { throw new RuntimeException("Stub!"); }
public  void setSurfaceTexture(android.graphics.SurfaceTexture surfaceTexture) { throw new RuntimeException("Stub!"); }
public  android.view.TextureView.SurfaceTextureListener getSurfaceTextureListener() { throw new RuntimeException("Stub!"); }
public  void setSurfaceTextureListener(android.view.TextureView.SurfaceTextureListener listener) { throw new RuntimeException("Stub!"); }
}
