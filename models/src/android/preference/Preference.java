import edu.stanford.stamp.harness.ApplicationDriver;
import edu.stanford.stamp.harness.Callback;

class Preference
{
	public  void setOnPreferenceChangeListener(final android.preference.Preference.OnPreferenceChangeListener onPreferenceChangeListener) { 
		onPreferenceChangeListener.onPreferenceChange(Preference.this, null);
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						onPreferenceChangeListener.onPreferenceChange(Preference.this, null);
					}
				});*/ 
	}


	public  void setOnPreferenceClickListener(final android.preference.Preference.OnPreferenceClickListener onPreferenceClickListener) { 
		onPreferenceClickListener.onPreferenceClick(Preference.this);
		/*ApplicationDriver.getInstance().
			registerCallback(new Callback(){
					public void run() {
						onPreferenceClickListener.onPreferenceClick(Preference.this);
					}
				}); */
	}

}
