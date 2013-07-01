package android.view.textservice;
public class SpellCheckerSession
{
public static interface SpellCheckerSessionListener
{
public abstract  void onGetSuggestions(android.view.textservice.SuggestionsInfo[] results);
public abstract  void onGetSentenceSuggestions(android.view.textservice.SentenceSuggestionsInfo[] results);
}
SpellCheckerSession() { throw new RuntimeException("Stub!"); }
public  boolean isSessionDisconnected() { throw new RuntimeException("Stub!"); }
public  android.view.textservice.SpellCheckerInfo getSpellChecker() { throw new RuntimeException("Stub!"); }
public  void cancel() { throw new RuntimeException("Stub!"); }
public  void close() { throw new RuntimeException("Stub!"); }
public  void getSentenceSuggestions(android.view.textservice.TextInfo[] textInfos, int suggestionsLimit) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  void getSuggestions(android.view.textservice.TextInfo textInfo, int suggestionsLimit) { throw new RuntimeException("Stub!"); }
@java.lang.Deprecated()
public  void getSuggestions(android.view.textservice.TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) { throw new RuntimeException("Stub!"); }
protected  void finalize() throws java.lang.Throwable { throw new RuntimeException("Stub!"); }
public static final java.lang.String SERVICE_META_DATA = "android.view.textservice.scs";
}
