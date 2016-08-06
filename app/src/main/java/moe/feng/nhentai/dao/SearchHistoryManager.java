package moe.feng.nhentai.dao;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;

import sumimakito.android.quickkv.QuickKV;
import sumimakito.android.quickkv.database.KeyValueDatabase;

public class SearchHistoryManager {

	private QuickKV mQuickKV;
	private KeyValueDatabase mDB;
	private String mSectionName;

	private ArrayList<String> array;

	private static ArrayList<Instance> sInstances = new ArrayList<>();

	private static final String DATABASE_NAME = "search_history";
	private static final int MAX_VOLUME = 15;

	public static SearchHistoryManager getInstance(Context context, String sectionName) {
		SearchHistoryManager sInstance = null;
		for (Instance i : sInstances) {
			if (i.sectionName.equals(sectionName)) {
				sInstance = i.manager;
				break;
			}
		}
		if (sInstance == null) {
			sInstance = new SearchHistoryManager(context, sectionName);
			sInstances.add(new Instance(sInstance, sectionName));
		}
		return sInstance;
	}

	public SearchHistoryManager(Context context, String sectionName) {
		this.mQuickKV = new QuickKV(context);
		this.mSectionName = sectionName;
		reloadDatabase();
	}

	public void reloadDatabase() {
		mDB = mQuickKV.getDatabase(DATABASE_NAME + "_" + mSectionName);
		array = new ArrayList<>();
		for (int i = 0; i < MAX_VOLUME; i++) {
			String s = (String) mDB.get("history_" + i);
			if (!TextUtils.isEmpty(s)) {
				array.add(s);
			}
		}
	}

	public void add(String keyword) {
		int pos = find(keyword);
		if (pos < 0) {
			pos = MAX_VOLUME - 1;
		}
		moveArrayToNext(pos - 1);
		mDB.put("history_0", keyword);
		mDB.persist();
		array.add(0, keyword);
	}

	/** Slow! */
	public String get(int pos) {
		return array.get(pos);
	}

	public int find(String keyword) {
		for (int i = MAX_VOLUME - 1; i >= 0; i--) {
			if (mDB.containsKey("history_" + i)) {
				if (mDB.get("history_" + i).equals(keyword)){
					return i;
				}
			}
		}
		return -1;
	}

	private void moveArrayToNext(int end) {
		for (int i = end; i >= 0; i--) {
			if (mDB.containsKey("history_" + i)) {
				mDB.put("history_" + (i + 1), mDB.get("history_" + i));
			}
		}
	}

	@SuppressWarnings("unused")
	public void cleanAll() {
		mDB.clear();
		mDB.persist();
	}

	public ArrayList<String> getAll() {
		return array;
	}

	private static class Instance {

		SearchHistoryManager manager;
		String sectionName;

		public Instance(SearchHistoryManager manager, String sectionName) {
			this.manager = manager;
			this.sectionName = sectionName;
		}

	}

}
