package android.speech.tts;
public abstract class TextToSpeechService
  extends android.app.Service
{
public  TextToSpeechService() { throw new RuntimeException("Stub!"); }
public  void onCreate() { throw new RuntimeException("Stub!"); }
public  void onDestroy() { throw new RuntimeException("Stub!"); }
protected abstract  int onIsLanguageAvailable(java.lang.String lang, java.lang.String country, java.lang.String variant);
protected abstract  java.lang.String[] onGetLanguage();
protected abstract  int onLoadLanguage(java.lang.String lang, java.lang.String country, java.lang.String variant);
protected abstract  void onStop();
protected abstract  void onSynthesizeText(android.speech.tts.SynthesisRequest request, android.speech.tts.SynthesisCallback callback);
protected  java.util.Set<java.lang.String> onGetFeaturesForLanguage(java.lang.String lang, java.lang.String country, java.lang.String variant) { throw new RuntimeException("Stub!"); }
public  android.os.IBinder onBind(android.content.Intent intent) { throw new RuntimeException("Stub!"); }
}
