package android.hardware;

import edu.stanford.stamp.annotation.STAMP;
import edu.stanford.stamp.annotation.Flow;
import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

public class Camera {

    public static class CameraInfo {

        public CameraInfo() {
            throw new RuntimeException("Stub!");
        }

        public static final int CAMERA_FACING_BACK = 0;

        public static final int CAMERA_FACING_FRONT = 1;

        public int facing;

        public int orientation;
    }

    public static interface PreviewCallback {

        public abstract void onPreviewFrame(byte[] data, android.hardware.Camera camera);
    }

    public static interface AutoFocusCallback {

        public abstract void onAutoFocus(boolean success, android.hardware.Camera camera);
    }

    public static interface AutoFocusMoveCallback {

        public abstract void onAutoFocusMoving(boolean start, android.hardware.Camera camera);
    }

    public static interface ShutterCallback {

        public abstract void onShutter();
    }

    public static interface PictureCallback {

        public abstract void onPictureTaken(byte[] data, android.hardware.Camera camera);
    }

    public static interface OnZoomChangeListener {

        public abstract void onZoomChange(int zoomValue, boolean stopped, android.hardware.Camera camera);
    }

    public static interface FaceDetectionListener {

        public abstract void onFaceDetection(android.hardware.Camera.Face[] faces, android.hardware.Camera camera);
    }

    public static class Face {

        public Face() {
            throw new RuntimeException("Stub!");
        }

        public android.graphics.Rect rect;

        public int score;

        public int id;

        public android.graphics.Point leftEye;

        public android.graphics.Point rightEye;

        public android.graphics.Point mouth;
    }

    public static interface ErrorCallback {

        public abstract void onError(int error, android.hardware.Camera camera);
    }

    public class Size {

        public Size(int w, int h) {
            throw new RuntimeException("Stub!");
        }

        public boolean equals(java.lang.Object obj) {
            throw new RuntimeException("Stub!");
        }

        public int hashCode() {
            throw new RuntimeException("Stub!");
        }

        public int width;

        public int height;
    }

    public static class Area {

        public Area(android.graphics.Rect rect, int weight) {
            throw new RuntimeException("Stub!");
        }

        public boolean equals(java.lang.Object obj) {
            throw new RuntimeException("Stub!");
        }

        public android.graphics.Rect rect;

        public int weight;
    }

    public class Parameters {

        Parameters() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String flatten() {
            throw new RuntimeException("Stub!");
        }

        public void unflatten(java.lang.String flattened) {
            throw new RuntimeException("Stub!");
        }

        public void remove(java.lang.String key) {
            throw new RuntimeException("Stub!");
        }

        public void set(java.lang.String key, java.lang.String value) {
            throw new RuntimeException("Stub!");
        }

        public void set(java.lang.String key, int value) {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String get(java.lang.String key) {
            throw new RuntimeException("Stub!");
        }

        public int getInt(java.lang.String key) {
            throw new RuntimeException("Stub!");
        }

        public void setPreviewSize(int width, int height) {
            throw new RuntimeException("Stub!");
        }

        public android.hardware.Camera.Size getPreviewSize() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<android.hardware.Camera.Size> getSupportedPreviewSizes() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<android.hardware.Camera.Size> getSupportedVideoSizes() {
            throw new RuntimeException("Stub!");
        }

        public android.hardware.Camera.Size getPreferredPreviewSizeForVideo() {
            throw new RuntimeException("Stub!");
        }

        public void setJpegThumbnailSize(int width, int height) {
            throw new RuntimeException("Stub!");
        }

        public android.hardware.Camera.Size getJpegThumbnailSize() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<android.hardware.Camera.Size> getSupportedJpegThumbnailSizes() {
            throw new RuntimeException("Stub!");
        }

        public void setJpegThumbnailQuality(int quality) {
            throw new RuntimeException("Stub!");
        }

        public int getJpegThumbnailQuality() {
            throw new RuntimeException("Stub!");
        }

        public void setJpegQuality(int quality) {
            throw new RuntimeException("Stub!");
        }

        public int getJpegQuality() {
            throw new RuntimeException("Stub!");
        }

        @java.lang.Deprecated()
        public void setPreviewFrameRate(int fps) {
            throw new RuntimeException("Stub!");
        }

        @java.lang.Deprecated()
        public int getPreviewFrameRate() {
            throw new RuntimeException("Stub!");
        }

        @java.lang.Deprecated()
        public java.util.List<java.lang.Integer> getSupportedPreviewFrameRates() {
            throw new RuntimeException("Stub!");
        }

        public void setPreviewFpsRange(int min, int max) {
            throw new RuntimeException("Stub!");
        }

        public void getPreviewFpsRange(int[] range) {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<int[]> getSupportedPreviewFpsRange() {
            throw new RuntimeException("Stub!");
        }

        public void setPreviewFormat(int pixel_format) {
            throw new RuntimeException("Stub!");
        }

        public int getPreviewFormat() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.Integer> getSupportedPreviewFormats() {
            throw new RuntimeException("Stub!");
        }

        public void setPictureSize(int width, int height) {
            throw new RuntimeException("Stub!");
        }

        public android.hardware.Camera.Size getPictureSize() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<android.hardware.Camera.Size> getSupportedPictureSizes() {
            throw new RuntimeException("Stub!");
        }

        public void setPictureFormat(int pixel_format) {
            throw new RuntimeException("Stub!");
        }

        public int getPictureFormat() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.Integer> getSupportedPictureFormats() {
            throw new RuntimeException("Stub!");
        }

        public void setRotation(int rotation) {
            throw new RuntimeException("Stub!");
        }

        public void setGpsLatitude(double latitude) {
            throw new RuntimeException("Stub!");
        }

        public void setGpsLongitude(double longitude) {
            throw new RuntimeException("Stub!");
        }

        public void setGpsAltitude(double altitude) {
            throw new RuntimeException("Stub!");
        }

        public void setGpsTimestamp(long timestamp) {
            throw new RuntimeException("Stub!");
        }

        public void setGpsProcessingMethod(java.lang.String processing_method) {
            throw new RuntimeException("Stub!");
        }

        public void removeGpsData() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String getWhiteBalance() {
            throw new RuntimeException("Stub!");
        }

        public void setWhiteBalance(java.lang.String value) {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.String> getSupportedWhiteBalance() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String getColorEffect() {
            throw new RuntimeException("Stub!");
        }

        public void setColorEffect(java.lang.String value) {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.String> getSupportedColorEffects() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String getAntibanding() {
            throw new RuntimeException("Stub!");
        }

        public void setAntibanding(java.lang.String antibanding) {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.String> getSupportedAntibanding() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String getSceneMode() {
            throw new RuntimeException("Stub!");
        }

        public void setSceneMode(java.lang.String value) {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.String> getSupportedSceneModes() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String getFlashMode() {
            throw new RuntimeException("Stub!");
        }

        public void setFlashMode(java.lang.String value) {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.String> getSupportedFlashModes() {
            throw new RuntimeException("Stub!");
        }

        public java.lang.String getFocusMode() {
            throw new RuntimeException("Stub!");
        }

        public void setFocusMode(java.lang.String value) {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.String> getSupportedFocusModes() {
            throw new RuntimeException("Stub!");
        }

        public float getFocalLength() {
            throw new RuntimeException("Stub!");
        }

        public float getHorizontalViewAngle() {
            throw new RuntimeException("Stub!");
        }

        public float getVerticalViewAngle() {
            throw new RuntimeException("Stub!");
        }

        public int getExposureCompensation() {
            throw new RuntimeException("Stub!");
        }

        public void setExposureCompensation(int value) {
            throw new RuntimeException("Stub!");
        }

        public int getMaxExposureCompensation() {
            throw new RuntimeException("Stub!");
        }

        public int getMinExposureCompensation() {
            throw new RuntimeException("Stub!");
        }

        public float getExposureCompensationStep() {
            throw new RuntimeException("Stub!");
        }

        public void setAutoExposureLock(boolean toggle) {
            throw new RuntimeException("Stub!");
        }

        public boolean getAutoExposureLock() {
            throw new RuntimeException("Stub!");
        }

        public boolean isAutoExposureLockSupported() {
            throw new RuntimeException("Stub!");
        }

        public void setAutoWhiteBalanceLock(boolean toggle) {
            throw new RuntimeException("Stub!");
        }

        public boolean getAutoWhiteBalanceLock() {
            throw new RuntimeException("Stub!");
        }

        public boolean isAutoWhiteBalanceLockSupported() {
            throw new RuntimeException("Stub!");
        }

        public int getZoom() {
            throw new RuntimeException("Stub!");
        }

        public void setZoom(int value) {
            throw new RuntimeException("Stub!");
        }

        public boolean isZoomSupported() {
            throw new RuntimeException("Stub!");
        }

        public int getMaxZoom() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<java.lang.Integer> getZoomRatios() {
            throw new RuntimeException("Stub!");
        }

        public boolean isSmoothZoomSupported() {
            throw new RuntimeException("Stub!");
        }

        public void getFocusDistances(float[] output) {
            throw new RuntimeException("Stub!");
        }

        public int getMaxNumFocusAreas() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<android.hardware.Camera.Area> getFocusAreas() {
            throw new RuntimeException("Stub!");
        }

        public void setFocusAreas(java.util.List<android.hardware.Camera.Area> focusAreas) {
            throw new RuntimeException("Stub!");
        }

        public int getMaxNumMeteringAreas() {
            throw new RuntimeException("Stub!");
        }

        public java.util.List<android.hardware.Camera.Area> getMeteringAreas() {
            throw new RuntimeException("Stub!");
        }

        public void setMeteringAreas(java.util.List<android.hardware.Camera.Area> meteringAreas) {
            throw new RuntimeException("Stub!");
        }

        public int getMaxNumDetectedFaces() {
            throw new RuntimeException("Stub!");
        }

        public void setRecordingHint(boolean hint) {
            throw new RuntimeException("Stub!");
        }

        public boolean isVideoSnapshotSupported() {
            throw new RuntimeException("Stub!");
        }

        public void setVideoStabilization(boolean toggle) {
            throw new RuntimeException("Stub!");
        }

        public boolean getVideoStabilization() {
            throw new RuntimeException("Stub!");
        }

        public boolean isVideoStabilizationSupported() {
            throw new RuntimeException("Stub!");
        }

        public static final java.lang.String WHITE_BALANCE_AUTO = "auto";

        public static final java.lang.String WHITE_BALANCE_INCANDESCENT = "incandescent";

        public static final java.lang.String WHITE_BALANCE_FLUORESCENT = "fluorescent";

        public static final java.lang.String WHITE_BALANCE_WARM_FLUORESCENT = "warm-fluorescent";

        public static final java.lang.String WHITE_BALANCE_DAYLIGHT = "daylight";

        public static final java.lang.String WHITE_BALANCE_CLOUDY_DAYLIGHT = "cloudy-daylight";

        public static final java.lang.String WHITE_BALANCE_TWILIGHT = "twilight";

        public static final java.lang.String WHITE_BALANCE_SHADE = "shade";

        public static final java.lang.String EFFECT_NONE = "none";

        public static final java.lang.String EFFECT_MONO = "mono";

        public static final java.lang.String EFFECT_NEGATIVE = "negative";

        public static final java.lang.String EFFECT_SOLARIZE = "solarize";

        public static final java.lang.String EFFECT_SEPIA = "sepia";

        public static final java.lang.String EFFECT_POSTERIZE = "posterize";

        public static final java.lang.String EFFECT_WHITEBOARD = "whiteboard";

        public static final java.lang.String EFFECT_BLACKBOARD = "blackboard";

        public static final java.lang.String EFFECT_AQUA = "aqua";

        public static final java.lang.String ANTIBANDING_AUTO = "auto";

        public static final java.lang.String ANTIBANDING_50HZ = "50hz";

        public static final java.lang.String ANTIBANDING_60HZ = "60hz";

        public static final java.lang.String ANTIBANDING_OFF = "off";

        public static final java.lang.String FLASH_MODE_OFF = "off";

        public static final java.lang.String FLASH_MODE_AUTO = "auto";

        public static final java.lang.String FLASH_MODE_ON = "on";

        public static final java.lang.String FLASH_MODE_RED_EYE = "red-eye";

        public static final java.lang.String FLASH_MODE_TORCH = "torch";

        public static final java.lang.String SCENE_MODE_AUTO = "auto";

        public static final java.lang.String SCENE_MODE_ACTION = "action";

        public static final java.lang.String SCENE_MODE_PORTRAIT = "portrait";

        public static final java.lang.String SCENE_MODE_LANDSCAPE = "landscape";

        public static final java.lang.String SCENE_MODE_NIGHT = "night";

        public static final java.lang.String SCENE_MODE_NIGHT_PORTRAIT = "night-portrait";

        public static final java.lang.String SCENE_MODE_THEATRE = "theatre";

        public static final java.lang.String SCENE_MODE_BEACH = "beach";

        public static final java.lang.String SCENE_MODE_SNOW = "snow";

        public static final java.lang.String SCENE_MODE_SUNSET = "sunset";

        public static final java.lang.String SCENE_MODE_STEADYPHOTO = "steadyphoto";

        public static final java.lang.String SCENE_MODE_FIREWORKS = "fireworks";

        public static final java.lang.String SCENE_MODE_SPORTS = "sports";

        public static final java.lang.String SCENE_MODE_PARTY = "party";

        public static final java.lang.String SCENE_MODE_CANDLELIGHT = "candlelight";

        public static final java.lang.String SCENE_MODE_BARCODE = "barcode";

        public static final java.lang.String FOCUS_MODE_AUTO = "auto";

        public static final java.lang.String FOCUS_MODE_INFINITY = "infinity";

        public static final java.lang.String FOCUS_MODE_MACRO = "macro";

        public static final java.lang.String FOCUS_MODE_FIXED = "fixed";

        public static final java.lang.String FOCUS_MODE_EDOF = "edof";

        public static final java.lang.String FOCUS_MODE_CONTINUOUS_VIDEO = "continuous-video";

        public static final java.lang.String FOCUS_MODE_CONTINUOUS_PICTURE = "continuous-picture";

        public static final int FOCUS_DISTANCE_NEAR_INDEX = 0;

        public static final int FOCUS_DISTANCE_OPTIMAL_INDEX = 1;

        public static final int FOCUS_DISTANCE_FAR_INDEX = 2;

        public static final int PREVIEW_FPS_MIN_INDEX = 0;

        public static final int PREVIEW_FPS_MAX_INDEX = 1;
    }

    public static native int getNumberOfCameras();

    public static native void getCameraInfo(int cameraId, android.hardware.Camera.CameraInfo cameraInfo);

    protected void finalize() {
        throw new RuntimeException("Stub!");
    }

    public final void release() {
        throw new RuntimeException("Stub!");
    }

    public final native void unlock();

    public final native void lock();

    public final native void reconnect() throws java.io.IOException;

    public final void setPreviewDisplay(android.view.SurfaceHolder holder) throws java.io.IOException {
        throw new RuntimeException("Stub!");
    }

    public final native void setPreviewTexture(android.graphics.SurfaceTexture surfaceTexture) throws java.io.IOException;

    public final native void startPreview();

    public final void stopPreview() {
        throw new RuntimeException("Stub!");
    }

    public final void addCallbackBuffer(byte[] callbackBuffer) {
        throw new RuntimeException("Stub!");
    }

    public final void autoFocus(android.hardware.Camera.AutoFocusCallback cb) {
        throw new RuntimeException("Stub!");
    }

    public final void cancelAutoFocus() {
        throw new RuntimeException("Stub!");
    }

    public void setAutoFocusMoveCallback(android.hardware.Camera.AutoFocusMoveCallback cb) {
        throw new RuntimeException("Stub!");
    }

    public final native void startSmoothZoom(int value);

    public final native void stopSmoothZoom();

    public final native void setDisplayOrientation(int degrees);

    public final void setZoomChangeListener(android.hardware.Camera.OnZoomChangeListener listener) {
        throw new RuntimeException("Stub!");
    }

    public final void setFaceDetectionListener(android.hardware.Camera.FaceDetectionListener listener) {
        throw new RuntimeException("Stub!");
    }

    public final void startFaceDetection() {
        throw new RuntimeException("Stub!");
    }

    public final void stopFaceDetection() {
        throw new RuntimeException("Stub!");
    }

    public final void setErrorCallback(android.hardware.Camera.ErrorCallback cb) {
        throw new RuntimeException("Stub!");
    }

    public void setParameters(android.hardware.Camera.Parameters params) {
        throw new RuntimeException("Stub!");
    }

    public android.hardware.Camera.Parameters getParameters() {
        throw new RuntimeException("Stub!");
    }

    public static final java.lang.String ACTION_NEW_PICTURE = "android.hardware.action.NEW_PICTURE";

    public static final java.lang.String ACTION_NEW_VIDEO = "android.hardware.action.NEW_VIDEO";

    public static final int CAMERA_ERROR_UNKNOWN = 1;

    public static final int CAMERA_ERROR_SERVER_DIED = 100;

    @STAMP(flows = { @Flow(from = "$CAMERA.picture", to = "@return") })
    public byte[] getPicture() {
        return new byte[1];
    }

    private Camera() {
    }

    public static android.hardware.Camera open(int cameraId) {
        return new Camera();
    }

    public static android.hardware.Camera open() {
        return new Camera();
    }

    public final void takePicture(android.hardware.Camera.ShutterCallback shutter, final android.hardware.Camera.PictureCallback raw, final android.hardware.Camera.PictureCallback jpeg) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                raw.onPictureTaken(getPicture(), Camera.this);
                jpeg.onPictureTaken(getPicture(), Camera.this);
            }
        });
    }

    public final void takePicture(android.hardware.Camera.ShutterCallback shutter, final android.hardware.Camera.PictureCallback raw, final android.hardware.Camera.PictureCallback postview, final android.hardware.Camera.PictureCallback jpeg) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                raw.onPictureTaken(getPicture(), Camera.this);
                postview.onPictureTaken(getPicture(), Camera.this);
                jpeg.onPictureTaken(getPicture(), Camera.this);
            }
        });
    }

    public final void setPreviewCallback(final android.hardware.Camera.PreviewCallback cb) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                cb.onPreviewFrame(getPicture(), Camera.this);
            }
        });
    }

    public final void setOneShotPreviewCallback(final android.hardware.Camera.PreviewCallback cb) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                cb.onPreviewFrame(getPicture(), Camera.this);
            }
        });
    }

    public final void setPreviewCallbackWithBuffer(final android.hardware.Camera.PreviewCallback cb) {
        ApplicationDriver.getInstance().registerCallback(new Callback() {

            public void run() {
                cb.onPreviewFrame(getPicture(), Camera.this);
            }
        });
    }
}

