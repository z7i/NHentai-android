package moe.feng.nhentai.ui.fragment.settings;

import android.os.Bundle;

import moe.feng.nhentai.R;
import moe.feng.nhentai.util.FilesUtil;
import moe.feng.nhentai.util.Settings;
import moe.feng.nhentai.view.pref.Preference;
import moe.feng.nhentai.view.pref.SwitchPreference;

public class SettingsStorage extends PreferenceFragment implements Preference.OnPreferenceClickListener, android.preference.Preference.OnPreferenceChangeListener {

	private SwitchPreference mNoMediaPref;

	private Settings mSets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_storage);

		mSets = Settings.getInstance(getContext());

		getActivity().setTitle(R.string.settings_storage);

		mNoMediaPref = (SwitchPreference) findPreference("no_media");

		mNoMediaPref.setChecked(mSets.getBoolean(Settings.KEY_NO_MEDIA, true));

		mNoMediaPref.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(android.preference.Preference pref) {
		return false;
	}

	@Override
	public boolean onPreferenceChange(android.preference.Preference pref, Object o) {
		if (pref == mNoMediaPref) {
			Boolean b = (Boolean) o;
			mSets.putBoolean(Settings.KEY_NO_MEDIA, b);
			mNoMediaPref.setChecked(b);
			if (b) {
				FilesUtil.createNewFile(FilesUtil.NOMEDIA_FILE);
			} else {
				FilesUtil.delete(FilesUtil.NOMEDIA_FILE);
			}
			return true;
		}
		return false;
	}

}
