package moe.feng.nhentai.ui.fragment.settings;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import moe.feng.nhentai.R;
import moe.feng.nhentai.dao.SearchHistoryManager;
import moe.feng.nhentai.ui.SettingsActivity;
import moe.feng.nhentai.util.Updates;
import moe.feng.nhentai.view.pref.Preference;

import static android.R.attr.duration;

public class SettingsMain extends PreferenceFragment implements Preference.OnPreferenceClickListener {

	private Preference mLicensePref;
	private Preference mGooglePlusPref;
	private Preference mGithubPref;
	private Preference mTelegreamPref;
	private Preference mGoogleGroupPref;

	private Preference mAppearancePref;
	private Preference mStoragePref;
	private Preference mSearchPref;

	private Preference mVersionPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_main);
		mSearchPref = (Preference) findPreference("search");
		mVersionPref = (Preference) findPreference("version");
		mLicensePref = (Preference) findPreference("license");
		mGooglePlusPref = (Preference) findPreference("google_plus");
		mGithubPref = (Preference) findPreference("github");
		mTelegreamPref = (Preference) findPreference("telegram");
		mAppearancePref = (Preference) findPreference("ui");
		mStoragePref = (Preference) findPreference("storage");
		mGoogleGroupPref = (Preference) findPreference("google_plus_group");

		String version = "Unknown";
		try {
			version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			version += " (" + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode + ")";
		} catch (Exception e) {
			Log.d(SettingsMain.class.getSimpleName(), "onCreate: Error getting version");
		}
		mVersionPref.setSummary(version);
		mVersionPref.setOnPreferenceClickListener(this);
		mSearchPref.setOnPreferenceClickListener(this);
		mLicensePref.setOnPreferenceClickListener(this);
		mGooglePlusPref.setOnPreferenceClickListener(this);
		mGithubPref.setOnPreferenceClickListener(this);
		mTelegreamPref.setOnPreferenceClickListener(this);
		mAppearancePref.setOnPreferenceClickListener(this);
		mStoragePref.setOnPreferenceClickListener(this);
		mGoogleGroupPref.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(android.preference.Preference pref) {
		if (pref == mLicensePref) {
			SettingsActivity.launchActivity(getActivity(), SettingsActivity.FLAG_LICENSE);
			return true;
		}
		if (pref == mGooglePlusPref) {
			openWebUrl("https://plus.google.com/+FungJichun");
			return true;
		}
		if (pref == mGithubPref) {
			openWebUrl(getString(R.string.set_title_github_website));
			return true;
		}
		if (pref == mTelegreamPref) {
			openWebUrl(getString(R.string.set_title_telegram_link));
			return true;
		}
		if (pref == mAppearancePref) {
			SettingsActivity.launchActivity(getActivity(), SettingsActivity.FLAG_GUI);
			return true;
		}
		if (pref == mStoragePref) {
			SettingsActivity.launchActivity(getActivity(), SettingsActivity.FLAG_STORAGE);
			return true;
		}
		if (pref == mGoogleGroupPref) {
			openWebUrl(getString(R.string.set_title_gpgroup_link));
		}
		if (pref == mSearchPref) {
			SearchHistoryManager.getInstance(getParentActivity().getBaseContext(), "all").cleanAll();
			Toast.makeText(getParentActivity().getBaseContext(), R.string.search_cleared, Toast.LENGTH_LONG).show();
			return true;
		}
		if (pref == mVersionPref) {
            final ProgressDialog progressDialog=ProgressDialog.show(getActivity(),null,getActivity().getResources().getString(R.string.checking),true,false);
			Updates.check(getActivity(), new Updates.UpdateInterface() {
                @Override
                public void onChecked(boolean isUpdated) {
                    progressDialog.dismiss();
                    if (isUpdated){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),R.string.no_update,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
		}
		return false;
	}

}
