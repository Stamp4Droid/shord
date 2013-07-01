package android.media;
public final class MediaCodec
{
public static final class BufferInfo
{
public  BufferInfo() { throw new RuntimeException("Stub!"); }
public  void set(int newOffset, int newSize, long newTimeUs, int newFlags) { throw new RuntimeException("Stub!"); }
public int offset;
public int size;
public long presentationTimeUs;
public int flags;
}
public static final class CryptoException
  extends java.lang.RuntimeException
{
public  CryptoException(int errorCode, java.lang.String detailMessage) { throw new RuntimeException("Stub!"); }
public  int getErrorCode() { throw new RuntimeException("Stub!"); }
}
public static final class CryptoInfo
{
public  CryptoInfo() { throw new RuntimeException("Stub!"); }
public  void set(int newNumSubSamples, int[] newNumBytesOfClearData, int[] newNumBytesOfEncryptedData, byte[] newKey, byte[] newIV, int newMode) { throw new RuntimeException("Stub!"); }
public int numSubSamples;
public int[] numBytesOfClearData = null;
public int[] numBytesOfEncryptedData = null;
public byte[] key = null;
public byte[] iv = null;
public int mode;
}
MediaCodec() { throw new RuntimeException("Stub!"); }
public static  android.media.MediaCodec createDecoderByType(java.lang.String type) { throw new RuntimeException("Stub!"); }
public static  android.media.MediaCodec createEncoderByType(java.lang.String type) { throw new RuntimeException("Stub!"); }
public static  android.media.MediaCodec createByCodecName(java.lang.String name) { throw new RuntimeException("Stub!"); }
protected  void finalize() { throw new RuntimeException("Stub!"); }
public final native  void release();
public  void configure(android.media.MediaFormat format, android.view.Surface surface, android.media.MediaCrypto crypto, int flags) { throw new RuntimeException("Stub!"); }
public final native  void start();
public final native  void stop();
public final native  void flush();
public final native  void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) throws android.media.MediaCodec.CryptoException;
public final native  void queueSecureInputBuffer(int index, int offset, android.media.MediaCodec.CryptoInfo info, long presentationTimeUs, int flags) throws android.media.MediaCodec.CryptoException;
public final native  int dequeueInputBuffer(long timeoutUs);
public final native  int dequeueOutputBuffer(android.media.MediaCodec.BufferInfo info, long timeoutUs);
public final native  void releaseOutputBuffer(int index, boolean render);
public final  android.media.MediaFormat getOutputFormat() { throw new RuntimeException("Stub!"); }
public  java.nio.ByteBuffer[] getInputBuffers() { throw new RuntimeException("Stub!"); }
public  java.nio.ByteBuffer[] getOutputBuffers() { throw new RuntimeException("Stub!"); }
public final native  void setVideoScalingMode(int mode);
public static final int BUFFER_FLAG_SYNC_FRAME = 1;
public static final int BUFFER_FLAG_CODEC_CONFIG = 2;
public static final int BUFFER_FLAG_END_OF_STREAM = 4;
public static final int CONFIGURE_FLAG_ENCODE = 1;
public static final int CRYPTO_MODE_UNENCRYPTED = 0;
public static final int CRYPTO_MODE_AES_CTR = 1;
public static final int INFO_TRY_AGAIN_LATER = -1;
public static final int INFO_OUTPUT_FORMAT_CHANGED = -2;
public static final int INFO_OUTPUT_BUFFERS_CHANGED = -3;
public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
}
