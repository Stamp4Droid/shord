package android.media;
public class RemoteControlClient
{
public class MetadataEditor
{
MetadataEditor() { throw new RuntimeException("Stub!"); }
public synchronized  android.media.RemoteControlClient.MetadataEditor putString(int key, java.lang.String value) throws java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
public synchronized  android.media.RemoteControlClient.MetadataEditor putLong(int key, long value) throws java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
public synchronized  android.media.RemoteControlClient.MetadataEditor putBitmap(int key, android.graphics.Bitmap bitmap) throws java.lang.IllegalArgumentException { throw new RuntimeException("Stub!"); }
public synchronized  void clear() { throw new RuntimeException("Stub!"); }
public synchronized  void apply() { throw new RuntimeException("Stub!"); }
public static final int BITMAP_KEY_ARTWORK = 100;
}
public  RemoteControlClient(android.app.PendingIntent mediaButtonIntent) { throw new RuntimeException("Stub!"); }
public  RemoteControlClient(android.app.PendingIntent mediaButtonIntent, android.os.Looper looper) { throw new RuntimeException("Stub!"); }
public  android.media.RemoteControlClient.MetadataEditor editMetadata(boolean startEmpty) { throw new RuntimeException("Stub!"); }
public  void setPlaybackState(int state) { throw new RuntimeException("Stub!"); }
public  void setTransportControlFlags(int transportControlFlags) { throw new RuntimeException("Stub!"); }
public static final int PLAYSTATE_STOPPED = 1;
public static final int PLAYSTATE_PAUSED = 2;
public static final int PLAYSTATE_PLAYING = 3;
public static final int PLAYSTATE_FAST_FORWARDING = 4;
public static final int PLAYSTATE_REWINDING = 5;
public static final int PLAYSTATE_SKIPPING_FORWARDS = 6;
public static final int PLAYSTATE_SKIPPING_BACKWARDS = 7;
public static final int PLAYSTATE_BUFFERING = 8;
public static final int PLAYSTATE_ERROR = 9;
public static final int FLAG_KEY_MEDIA_PREVIOUS = 1;
public static final int FLAG_KEY_MEDIA_REWIND = 2;
public static final int FLAG_KEY_MEDIA_PLAY = 4;
public static final int FLAG_KEY_MEDIA_PLAY_PAUSE = 8;
public static final int FLAG_KEY_MEDIA_PAUSE = 16;
public static final int FLAG_KEY_MEDIA_STOP = 32;
public static final int FLAG_KEY_MEDIA_FAST_FORWARD = 64;
public static final int FLAG_KEY_MEDIA_NEXT = 128;
}
