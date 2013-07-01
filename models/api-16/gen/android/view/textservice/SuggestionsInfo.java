package android.view.textservice;
public final class SuggestionsInfo
  implements android.os.Parcelable
{
public  SuggestionsInfo(int suggestionsAttributes, java.lang.String[] suggestions) { throw new RuntimeException("Stub!"); }
public  SuggestionsInfo(int suggestionsAttributes, java.lang.String[] suggestions, int cookie, int sequence) { throw new RuntimeException("Stub!"); }
public  SuggestionsInfo(android.os.Parcel source) { throw new RuntimeException("Stub!"); }
public  void writeToParcel(android.os.Parcel dest, int flags) { throw new RuntimeException("Stub!"); }
public  void setCookieAndSequence(int cookie, int sequence) { throw new RuntimeException("Stub!"); }
public  int getCookie() { throw new RuntimeException("Stub!"); }
public  int getSequence() { throw new RuntimeException("Stub!"); }
public  int getSuggestionsAttributes() { throw new RuntimeException("Stub!"); }
public  int getSuggestionsCount() { throw new RuntimeException("Stub!"); }
public  java.lang.String getSuggestionAt(int i) { throw new RuntimeException("Stub!"); }
public  int describeContents() { throw new RuntimeException("Stub!"); }
public static final int RESULT_ATTR_IN_THE_DICTIONARY = 1;
public static final int RESULT_ATTR_LOOKS_LIKE_TYPO = 2;
public static final int RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS = 4;
public static final android.os.Parcelable.Creator<android.view.textservice.SuggestionsInfo> CREATOR;
static { CREATOR = null; }
}
