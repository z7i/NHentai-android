package moe.feng.nhentai.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.Utility;

public class LatestBooksKeeper {

	private MyData data;
	private Context context;

	public static final String TAG = LatestBooksKeeper.class.getSimpleName();

	private static final String FILE_NAME = "latest_books.json";

	private static LatestBooksKeeper sInstance;

	public static LatestBooksKeeper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new LatestBooksKeeper(context);
			sInstance.reload(context);
		}
		return sInstance;
	}

	private LatestBooksKeeper(Context context) {
		this.context = context;
		this.data = new MyData();
	}

	public void reload(Context context) {
		String json;
		try {
			json = Utility.readStringFromFile(context, FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
			json = "{\"books\":[]}";
		}

		data = new Gson().fromJson(json, MyData.class);
		if(!FileCacheManager.getInstance(context).checkUpdateLatest()){
			new UpdateLatest().execute(context);
			save();
		}

	}

	public ArrayList<Book> getData() {
		return data.books;
	}

	public void setData(ArrayList<Book> books) {
		this.data.books = books;
	}

	public long getUpdatedMiles() {
		return this.data.updatedMiles;
	}

	public void setUpdatedMiles(long updatedMiles) {
		this.data.updatedMiles = updatedMiles;
	}

	public int getNowPage() {
		return this.data.nowPage;
	}

	public void setNowPage(int nowPage) {
		this.data.nowPage = nowPage;
	}

	public void save() {
		try {
			Utility.saveStringToFile(context, FILE_NAME, data.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class MyData {

		public ArrayList<Book> books = new ArrayList<>();
		public long updatedMiles = -1;
		public int nowPage = 1;

		public String toJSONString() {
			return new Gson().toJson(this);
		}

		public void updateBooksData(Context context) {
			if (books != null) {
				books = new ArrayList<>();
			}
		}

	}

	private class UpdateLatest extends AsyncTask<Context, Void, BaseMessage> {
		@Override
		protected BaseMessage doInBackground(Context... params) {
			data.updateBooksData(params[0]);
			FileCacheManager.getInstance(params[0]).UpdateLatest();
			return null;
		}

		@Override
		protected void onPostExecute(BaseMessage msg) {
			Log.d(TAG, "latest Books Update Complete ");
		}

	}

}
