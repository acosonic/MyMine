package net.bicou.redmine.app.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import net.bicou.redmine.R;
import net.bicou.redmine.app.ga.TrackedPreferenceFragment;

/**
 * This fragment shows general preferences only. It is used when the activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends TrackedPreferenceFragment {
	public GeneralPreferenceFragment() {
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);
		//
		// // Bind the summaries of EditText/List/Dialog/Ringtone preferences
		// // to their values. When their values change, their summaries are
		// // updated to reflect the new value, per the Android Design
		// // guidelines.
		// bindPreferenceSummaryToValue(findPreference("example_text"));
		// bindPreferenceSummaryToValue(findPreference("example_list"));
	}
}
