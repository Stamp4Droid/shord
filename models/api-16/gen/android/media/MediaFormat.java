package android.media;
public final class MediaFormat
{
public  MediaFormat() { throw new RuntimeException("Stub!"); }
public final  boolean containsKey(java.lang.String name) { throw new RuntimeException("Stub!"); }
public final  int getInteger(java.lang.String name) { throw new RuntimeException("Stub!"); }
public final  long getLong(java.lang.String name) { throw new RuntimeException("Stub!"); }
public final  float getFloat(java.lang.String name) { throw new RuntimeException("Stub!"); }
public final  java.lang.String getString(java.lang.String name) { throw new RuntimeException("Stub!"); }
public final  java.nio.ByteBuffer getByteBuffer(java.lang.String name) { throw new RuntimeException("Stub!"); }
public final  void setInteger(java.lang.String name, int value) { throw new RuntimeException("Stub!"); }
public final  void setLong(java.lang.String name, long value) { throw new RuntimeException("Stub!"); }
public final  void setFloat(java.lang.String name, float value) { throw new RuntimeException("Stub!"); }
public final  void setString(java.lang.String name, java.lang.String value) { throw new RuntimeException("Stub!"); }
public final  void setByteBuffer(java.lang.String name, java.nio.ByteBuffer bytes) { throw new RuntimeException("Stub!"); }
public static final  android.media.MediaFormat createAudioFormat(java.lang.String mime, int sampleRate, int channelCount) { throw new RuntimeException("Stub!"); }
public static final  android.media.MediaFormat createVideoFormat(java.lang.String mime, int width, int height) { throw new RuntimeException("Stub!"); }
public  java.lang.String toString() { throw new RuntimeException("Stub!"); }
public static final java.lang.String KEY_MIME = "mime";
public static final java.lang.String KEY_SAMPLE_RATE = "sample-rate";
public static final java.lang.String KEY_CHANNEL_COUNT = "channel-count";
public static final java.lang.String KEY_WIDTH = "width";
public static final java.lang.String KEY_HEIGHT = "height";
public static final java.lang.String KEY_MAX_INPUT_SIZE = "max-input-size";
public static final java.lang.String KEY_BIT_RATE = "bitrate";
public static final java.lang.String KEY_COLOR_FORMAT = "color-format";
public static final java.lang.String KEY_FRAME_RATE = "frame-rate";
public static final java.lang.String KEY_I_FRAME_INTERVAL = "i-frame-interval";
public static final java.lang.String KEY_DURATION = "durationUs";
public static final java.lang.String KEY_IS_ADTS = "is-adts";
public static final java.lang.String KEY_CHANNEL_MASK = "channel-mask";
public static final java.lang.String KEY_AAC_PROFILE = "aac-profile";
public static final java.lang.String KEY_FLAC_COMPRESSION_LEVEL = "flac-compression-level";
}
