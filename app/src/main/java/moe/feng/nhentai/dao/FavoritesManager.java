package moe.feng.nhentai.dao;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import moe.feng.nhentai.model.Book;
import sumimakito.android.quickkv.QuickKV;
import sumimakito.android.quickkv.database.KeyValueDatabase;

public class FavoritesManager {

	private ArrayList<Book> books;

	private QuickKV mQKV;
	private KeyValueDatabase mKVDB;

	public static final String TAG = FavoritesManager.class.getSimpleName();

	private static FavoritesManager sInstance;

	public static FavoritesManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new FavoritesManager(context);
			sInstance.reload();
		}
		return sInstance;
	}

	private FavoritesManager(Context context) {
		mQKV = new QuickKV(context);
		books = new ArrayList<>();
	}

	public void reload() {
		mKVDB = mQKV.getDatabase("favorites_manager");

		books = mKVDB.containsKey("books") ? (ArrayList<Book>) mKVDB.get("books") : new ArrayList<Book>();

		Log.i(TAG, "array size:" + books.size());
	}

	public Book get(int position) {
		return books.get(position);
	}

	public void remove(int position) {
		books.remove(position);
	}

	public void remove(Book book) {
		books.remove(book);
	}

	public void add(Book book) {
		books.add(book);
	}

	public void add(int position, Book book) {
		books.add(position, book);
	}

	public void set(int position, Book book) {
		books.set(position, book);
	}

	public int size() {
		return books.size();
	}

	public ArrayList<Book> toArray() {
		return books;
	}

	public int find(Book book) {
		Log.i(TAG, "array size:" + books.size());
		for (int i = 0; i < books.size(); i++) {
			if (book.bookId == books.get(i).bookId) {
				return i;
			}
		}
		return -1;
	}

	public boolean contains(Book book) {
		return books.contains(book);
	}

	public void save() {
		mKVDB.put("books", books);
		mKVDB.persist();
	}

}
