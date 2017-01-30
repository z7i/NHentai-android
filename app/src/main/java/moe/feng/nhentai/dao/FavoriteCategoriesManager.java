package moe.feng.nhentai.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Category;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.Utility;

public class FavoriteCategoriesManager {

	private MyArray categories;
	private Context context;

	public static final String TAG = FavoriteCategoriesManager.class.getSimpleName();

	private static final String FILE_NAME = "favorite_categories.json";

	private static FavoriteCategoriesManager sInstance;

	public static FavoriteCategoriesManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new FavoriteCategoriesManager(context);
			sInstance.reload();
		}
		return sInstance;
	}

	private FavoriteCategoriesManager(Context context) {
		this.context = context;
		this.categories = new MyArray();
	}

	public void reload() {
		String json;
		try {
			json = Utility.readStringFromFile(context, FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
			json = "{\"data\":[]}";
		}

		categories = new Gson().fromJson(json, MyArray.class);
		save();
	}

	public Category get(int position) {
		return categories.get(position);
	}

	public void remove(int position) {
		categories.remove(position);
	}

	public void add(Category category) {
		categories.add(category);
	}

	public void add(int position, Category category) {
		categories.add(position, category);
	}

	public void set(int position, Category category) {
		categories.set(position, category);
	}

	public int size() {
		return categories.size();
	}

	@SuppressWarnings("unused")
	public ArrayList<Category> toArray() {
		return categories.data;
	}

	public int find(Category category) {
		for (int i = 0; i < categories.size(); i++) {
			if (category.type.equals(categories.get(i).type) && category.name.equals(categories.get(i).name)) {
				return i;
			}
		}
		return -1;
	}

	public boolean contains(Category category) {
		return find(category) != -1;
	}

	public boolean contains(String type, String name, String id) {
		return find(new Category(type, name, id)) != -1;
	}

	public void save() {
		try {
			Utility.saveStringToFile(context, FILE_NAME, categories.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class MyArray {

		public ArrayList<Category> data = new ArrayList<>();

		public String toJSONString() {
			return new Gson().toJson(this);
		}

		public Category get(int position) {
			return data.get(position);
		}

		public int size() {
			return data.size();
		}

		public Category set(int position, Category category) {
			return data.set(position, category);
		}

		public boolean add(Category category) {
			return data.add(category);
		}

		public void add(int position, Category category) {
			data.add(position, category);
		}

		public void remove(int position) {
			data.remove(position);
		}

	}

}
