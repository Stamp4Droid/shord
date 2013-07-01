package android.preference;
public abstract class PreferenceFragment
  extends android.app.Fragment
{
public static interface OnPreferenceStartFragmentCallback
{
public abstract  boolean onPreferenceStartFragment(android.preference.PreferenceFragment caller, android.preference.Preference pref);
}
public  PreferenceFragment() { throw new RuntimeException("Stub!"); }
public  void onCreate(android.os.Bundle savedInstanceState) { throw new RuntimeException("Stub!"); }
public  android.view.View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) { throw new RuntimeException("Stub!"); }
public  void onActivityCreated(android.os.Bundle savedInstanceState) { throw new RuntimeException("Stub!"); }
public  void onStart() { throw new RuntimeException("Stub!"); }
public  void onStop() { throw new RuntimeException("Stub!"); }
public  void onDestroyView() { throw new RuntimeException("Stub!"); }
public  void onDestroy() { throw new RuntimeException("Stub!"); }
public  void onSaveInstanceState(android.os.Bundle outState) { throw new RuntimeException("Stub!"); }
public  void onActivityResult(int requestCode, int resultCode, android.content.Intent data) { throw new RuntimeException("Stub!"); }
public  android.preference.PreferenceManager getPreferenceManager() { throw new RuntimeException("Stub!"); }
public  void setPreferenceScreen(android.preference.PreferenceScreen preferenceScreen) { throw new RuntimeException("Stub!"); }
public  android.preference.PreferenceScreen getPreferenceScreen() { throw new RuntimeException("Stub!"); }
public  void addPreferencesFromIntent(android.content.Intent intent) { throw new RuntimeException("Stub!"); }
public  void addPreferencesFromResource(int preferencesResId) { throw new RuntimeException("Stub!"); }
public  boolean onPreferenceTreeClick(android.preference.PreferenceScreen preferenceScreen, android.preference.Preference preference) { throw new RuntimeException("Stub!"); }
public  android.preference.Preference findPreference(java.lang.CharSequence key) { throw new RuntimeException("Stub!"); }
}
