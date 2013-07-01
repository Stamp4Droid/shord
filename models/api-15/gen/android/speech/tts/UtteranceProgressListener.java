package android.speech.tts;
public abstract class UtteranceProgressListener
{
public  UtteranceProgressListener() { throw new RuntimeException("Stub!"); }
public abstract  void onStart(java.lang.String utteranceId);
public abstract  void onDone(java.lang.String utteranceId);
public abstract  void onError(java.lang.String utteranceId);
}
