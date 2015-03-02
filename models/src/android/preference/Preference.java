import edu.stanford.stamp.annotation.Inline;

class Preference
{
	@Inline
	public  void setOnPreferenceChangeListener(final android.preference.Preference.OnPreferenceChangeListener onPreferenceChangeListener) {
		onPreferenceChangeListener.onPreferenceChange(Preference.this, null);
	}

	@Inline
	public  void setOnPreferenceClickListener(final android.preference.Preference.OnPreferenceClickListener onPreferenceClickListener) {
		onPreferenceClickListener.onPreferenceClick(Preference.this);
	}
}
