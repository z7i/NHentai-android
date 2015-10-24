package moe.feng.nhentai.util;

import android.content.Context;

import moe.feng.nhentai.dao.CommonPreferences;

public class Settings {

	public static final String PREFERENCES_NAME = "settings";

	public static final String KEY_CELEBRATE = "celebrate", KEY_CARDS_COUNT = "cards_count",
			KEY_LIST_HD_IMAGE = "list_hd_image", KEY_FULL_IMAGE_PREVIEW = "full_image_preview";

	private static Settings sInstance;

	private CommonPreferences mPrefs;

	public static Settings getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Settings(context);
		}
		return sInstance;
	}

	private Settings(Context context) {
		mPrefs = CommonPreferences.getInstance(context, PREFERENCES_NAME);
	}

	public Settings putBoolean(String key, boolean value) {
		mPrefs.edit().putBoolean(key, value).commit();
		return this;
	}

	public boolean getBoolean(String key, boolean def) {
		return mPrefs.getBoolean(key, def);
	}

	public Settings putInt(String key, int value) {
		mPrefs.edit().putInt(key, value).commit();
		return this;
	}

	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}


	public Settings putString(String key, String value) {
		mPrefs.edit().putString(key, value).commit();
		return this;
	}

	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}

}
