package moe.feng.nhentai.ui.fragment.settings;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatTextView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import moe.feng.nhentai.R;
import moe.feng.nhentai.util.Settings;
import moe.feng.nhentai.view.pref.Preference;
import moe.feng.nhentai.view.pref.SwitchPreference;

public class SettingsAppearance extends PreferenceFragment implements Preference.OnPreferenceClickListener, android.preference.Preference.OnPreferenceChangeListener {

	private Preference mCardCountPref;
	private SwitchPreference mHDImagePref, mFullHDPreviewPref, mAllowStandaloneTaskPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_ui);

		getActivity().setTitle(R.string.category_ui);

		mCardCountPref = (Preference) findPreference("card_count");
		mHDImagePref = (SwitchPreference) findPreference("hd_image");
		mFullHDPreviewPref = (SwitchPreference) findPreference("full_image_preview");
		mAllowStandaloneTaskPref = (SwitchPreference) findPreference("allow_standalone_task");

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			mAllowStandaloneTaskPref.setEnabled(false);
			mAllowStandaloneTaskPref.setSummary(R.string.set_title_android_too_old_desc);
		}

		int cardCount = mSets.getInt(Settings.KEY_CARDS_COUNT, -1);
		mCardCountPref.setSummary(
				getString(
						R.string.set_title_cards_count_summary,
						cardCount == -1 ?
								getResources().getStringArray(R.array.set_cards_count_options)[0] :
								String.valueOf(cardCount)
				)
		);
		mHDImagePref.setChecked(mSets.getBoolean(Settings.KEY_LIST_HD_IMAGE, false));
		mFullHDPreviewPref.setChecked(mSets.getBoolean(Settings.KEY_FULL_IMAGE_PREVIEW, false));
		mAllowStandaloneTaskPref.setChecked(mSets.getBoolean(Settings.KEY_ALLOW_STANDALONE_TASK, true) && mAllowStandaloneTaskPref.isEnabled());

		mCardCountPref.setOnPreferenceClickListener(this);
		mHDImagePref.setOnPreferenceChangeListener(this);
		mFullHDPreviewPref.setOnPreferenceChangeListener(this);
		mAllowStandaloneTaskPref.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(android.preference.Preference pref) {
		if (pref == mCardCountPref) {
			showCardCountDialog();
			return true;
		}
		return false;
	}

	private void showCardCountDialog() {
		new AlertDialog.Builder(getActivity())
				.setItems(R.array.set_cards_count_options, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						if (i == 0) {
							mSets.putInt(Settings.KEY_CARDS_COUNT, -1);
							mCardCountPref.setSummary(
									getString(
											R.string.set_title_cards_count_summary,
											getResources().getStringArray(R.array.set_cards_count_options)[0]
									)
							);
							showRestartTips();
						} else {
							showCardCountCustomDialog();
						}
					}
				})
				.setTitle(R.string.set_title_cards_count)
				.show();
	}

	private void showCardCountCustomDialog() {
		ContextThemeWrapper wrapper = new ContextThemeWrapper(getActivity().getApplicationContext(), R.style.Theme_NHBooks_Light);
		View view = LayoutInflater.from(wrapper)
				.inflate(R.layout.dialog_set_card_count,(ViewGroup) getView());
		final AppCompatTextView numberText = (AppCompatTextView) view.findViewById(R.id.number_text);
		final AppCompatSeekBar seekBar = (AppCompatSeekBar) view.findViewById(R.id.seekbar);
		int cardCount = mSets.getInt(Settings.KEY_CARDS_COUNT, 2);
		if (cardCount < 2) {
			cardCount = 2;
		}
		numberText.setText(String.valueOf(cardCount));
		seekBar.setKeyProgressIncrement(1);
		seekBar.setMax(8);
		seekBar.setProgress(cardCount - 2);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				numberText.setText(String.valueOf(i + 2));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		new AlertDialog.Builder(getActivity())
				.setView(view)
				.setTitle(R.string.set_title_cards_count)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						mSets.putInt(Settings.KEY_CARDS_COUNT, seekBar.getProgress() + 2);
						mCardCountPref.setSummary(
								getString(
										R.string.set_title_cards_count_summary,
										String.valueOf(seekBar.getProgress() + 2)
								)
						);
						showRestartTips();
					}
				})
				.show();
	}

	@Override
	public boolean onPreferenceChange(android.preference.Preference pref, Object o) {
		if (pref == mHDImagePref) {
			Boolean b = (Boolean) o;
			mSets.putBoolean(Settings.KEY_LIST_HD_IMAGE, b);
			mHDImagePref.setChecked(b);
			showRestartTips();
			return true;
		}
		if (pref == mFullHDPreviewPref) {
			Boolean b = (Boolean) o;
			mSets.putBoolean(Settings.KEY_FULL_IMAGE_PREVIEW, b);
			mFullHDPreviewPref.setChecked(b);
			return true;
		}
		if (pref == mAllowStandaloneTaskPref) {
			Boolean b = (Boolean) o;
			mSets.putBoolean(Settings.KEY_ALLOW_STANDALONE_TASK, b);
			mAllowStandaloneTaskPref.setChecked(b);
			return true;
		}
		return false;
	}

}
