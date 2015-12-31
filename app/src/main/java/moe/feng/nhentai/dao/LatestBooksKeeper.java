package moe.feng.nhentai.dao;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import moe.feng.nhentai.model.Book;
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
			sInstance.reload();
		}
		return sInstance;
	}

	private LatestBooksKeeper(Context context) {
		this.context = context;
		this.data = new MyData();
	}

	public void reload() {
		String json;
		try {
			json = Utility.readStringFromFile(context, FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
			json = "{\"books\":[]}";
		}

		data = new Gson().fromJson(json, MyData.class);
		data.updateBooksData();
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

		public void updateBooksData() {
			if (books != null) {
				for (Book book : books) {
					book.updateDataFromOldData();
				}
			}
		}

	}

}
