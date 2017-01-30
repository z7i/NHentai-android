package moe.feng.nhentai.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import static moe.feng.nhentai.cache.common.Constants.CACHE_COVER;
import static moe.feng.nhentai.cache.common.Constants.CACHE_PAGE_THUMB;
import static moe.feng.nhentai.cache.common.Constants.CACHE_THUMB;

public class BookApi {

	public static final String TAG = BookApi.class.getSimpleName();

	public static BaseMessage getBook(Context context, Book source) {
		BaseMessage result = new BaseMessage();
		FileCacheManager m = FileCacheManager.getInstance(context);
		Book book = m.getExternalBook(source);

		if (book == null)
			book = m.getCacheBook(source);

		if (book == null){
			BaseMessage ms = PageApi.getBookDetailList(source.bookId);
			m.createCacheFromBook((Book) ms.getData());

			Log.d(TAG, "getBook: "+((Book) ms.getData()).titlePretty);

			return ms;

		}
		else{
			result.setCode(0);
			result.setData(book);
			return result;
		}

	}

	public static Bitmap getCover(Context context, Book book) {
		String url = book.bigCoverImageUrl;

		if (FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_COVER, url, book.bookId)){
			Log.i(TAG, "Cover: Loaded from cache");
			return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_COVER, url, book.bookId);
		}
		else if (FileCacheManager.getInstance(context).createCacheFromNetwork(CACHE_COVER, url, book.bookId)){
			Log.i(TAG, "Cover: Downloaded from web");
			return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_COVER, url, book.bookId);
		}
		else {
			return null;
		}

	}

	public static Bitmap getThumb(Context context, Book book) {
		String url = book.previewImageUrl;

		if (FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_THUMB, url, book.bookId)){
			Log.i(TAG, "Thumb: Loaded from cache");
			return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_THUMB, url, book.bookId);
		}
		else if (FileCacheManager.getInstance(context).createCacheFromNetwork(CACHE_THUMB, url, book.bookId)){
			Log.i(TAG, "Thumb: Downloaded from web");
			return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_THUMB, url, book.bookId);
		}
		else {
			return null;
		}

	}

	public static Bitmap getPageThumb(Context context, Book book, int position) {
		String url = NHentaiUrl.getThumbPictureUrl(book.galleryId, Integer.toString(position));
		if (FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_PAGE_THUMB, url, book.bookId)){
			Log.i(TAG, "Page Thumb: Loaded from cache");
			return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_PAGE_THUMB, url, book.bookId);
		}
		else if (FileCacheManager.getInstance(context).createCacheFromNetwork(CACHE_PAGE_THUMB, url, book.bookId)){
			Log.i(TAG, "Page Thumb: Downloaded from web");
			return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_PAGE_THUMB, url, book.bookId);
		}
		else {
			return null;
		}
	}

}
