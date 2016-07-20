package moe.feng.nhentai.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;

import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import static moe.feng.nhentai.cache.common.Constants.CACHE_COVER;
import static moe.feng.nhentai.cache.common.Constants.CACHE_PAGE_THUMB;
import static moe.feng.nhentai.cache.common.Constants.CACHE_THUMB;

public class BookApi {

	public static final String TAG = BookApi.class.getSimpleName();

	public static BaseMessage getBook(Context context, String id) {
		BaseMessage result = new BaseMessage();
		FileCacheManager m = FileCacheManager.getInstance(context);
		Book book = m.getExternalBook(id);
		if (book == null){
			BaseMessage ms = PageApi.getBookDetailList(id);
			m.createCacheFromBook((Book) ms.getData());

			Log.d(TAG, "getBook 2: "+((Book) ms.getData()).titlePretty);

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
		FileCacheManager m = FileCacheManager.getInstance(context);
		
		if (!m.cacheExistsUrl(CACHE_COVER, url, book.title) && !m.createCacheFromNetwork(CACHE_COVER, url, book.title)) {
			return null;
		}

		return m.getBitmapUrl(CACHE_COVER, url, book.title);
	}

	public static Bitmap getThumb(Context context, Book book) {
		String url = book.previewImageUrl;
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_THUMB, url, book.title) && !m.createCacheFromNetwork(CACHE_THUMB, url, book.title)) {
			return null;
		}

		return m.getBitmapUrl(CACHE_THUMB, url, book.title);
	}

	public static Bitmap getPageThumb(Context context, Book book, int position) {
		String url = NHentaiUrl.getThumbPictureUrl(book.galleryId, Integer.toString(position));
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_PAGE_THUMB, url, book.title) && !m.createCacheFromNetwork(CACHE_PAGE_THUMB, url, book.title)) {
			return null;
		}

		return m.getBitmapUrl(CACHE_PAGE_THUMB, url, book.title);
	}

	public static File getCoverFile(Context context, Book book) {
		String url = book.bigCoverImageUrl;
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_COVER, url, book.title) && !m.createCacheFromNetwork(CACHE_COVER, url, book.title)) {
			return null;
		}

		return m.getBitmapUrlFile(CACHE_COVER, url, book.title);
	}

	public static File getThumbFile(Context context, Book book) {
		String url = book.previewImageUrl;
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_THUMB, url, book.title) && !m.createCacheFromNetwork(CACHE_THUMB, url, book.title)) {
			return null;
		}

		return m.getBitmapUrlFile(CACHE_THUMB, url, book.title);
	}

	public static File getPageThumbFile(Context context, Book book, int position) {
		String url = NHentaiUrl.getThumbPictureUrl(book.galleryId, Integer.toString(position));
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_PAGE_THUMB, url, book.title) && !m.createCacheFromNetwork(CACHE_PAGE_THUMB, url, book.title)) {
			return null;
		}

		return m.getBitmapUrlFile(CACHE_PAGE_THUMB, url,book.title);
	}

}
