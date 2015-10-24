package moe.feng.nhentai.ui.fragment.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import moe.feng.nhentai.ui.SettingsActivity;
import moe.feng.nhentai.util.Settings;

public class PreferenceFragment extends android.preference.PreferenceFragment {

	protected Settings mSets;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSets = Settings.getInstance(getActivity());
	}

	public SettingsActivity getParentActivity() {
		return (SettingsActivity) getActivity();
	}

	public void showRestartTips() {
		getParentActivity().showRestartTips();
	}

	public void openWebUrl(String url) {
		Uri uri = Uri.parse(url);
		startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}

}
