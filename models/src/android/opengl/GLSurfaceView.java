import edu.stanford.stamp.annotation.Inline;

class GLSurfaceView {
	@Inline
    public  void setRenderer(final android.opengl.GLSurfaceView.Renderer renderer) {
		renderer.onSurfaceCreated(null, null);
		renderer.onSurfaceChanged(null, 0, 0);
		renderer.onDrawFrame(null);
    }
}
