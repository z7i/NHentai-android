package moe.feng.nhentai.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import static moe.feng.nhentai.cache.common.Constants.CACHE_COVER;
import static moe.feng.nhentai.cache.common.Constants.CACHE_PAGE_THUMB;
import static moe.feng.nhentai.cache.common.Constants.CACHE_THUMB;

public class BookApi {

	public static final String TAG = BookApi.class.getSimpleName();

	public static BaseMessage getBook(String id) {
		BaseMessage result = new BaseMessage();

		String url = NHentaiUrl.getBookDetailsUrl(id);

		Document doc;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			result.setCode(403);
			e.printStackTrace();
			return result;
		}

		Book book = new Book();

		Elements info = doc.getElementsByAttributeValue("id", "info");
		Element element = info.get(0);

		/** Get basic info */
		book.title = element.getElementsByTag("h1").get(0).text();
		try {
			book.titleJP = element.getElementsByTag("h2").get(0).text();
		} catch (Exception e) {
			book.titleJP =book.title;
		}

		book.bookId = id;

		/** Get tags */
		Elements tags = doc.getElementsByClass("field-name");
		for (Element r : tags) {
			try {
				if (r.text().contains("Parodies")) {
					String st = analysisTags(r.html()).get(0);
					if (st!=null)
						book.parodies = st;
				}
				if (r.text().contains("Tags")) {
					String st = analysisTags(r.html()).get(0);
					if (st!=null)
						book.tags.addAll(analysisTags(r.html()));
				}
				if (r.text().contains("Language")) {

					book.language = analysisTags(r.html()).get(0);

					if(book.language.equals("japanese")){
						book.langField = Book.LANG_JP;
					}
					else if (book.language.equals("english")){
						book.langField =Book.LANG_GB;
					}
					else{
						book.langField=Book.LANG_CN;
					}
				}

				if (r.text().contains("Groups")) {
					String st = analysisTags(r.html()).get(0);
					if (st!=null)
						book.group = st;
				}
				if (r.text().contains("Artists")) {
					String st = analysisTags(r.html()).get(0);
					if (st!=null)
						book.artists.addAll(analysisTags(r.html()));
				}
				if (r.text().contains("Characters")) {
					String st = analysisTags(r.html()).get(0);
					if (st!=null)
						book.characters.addAll(analysisTags(r.html()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Failed to analysis \"" + r.html() + "\"");
			}
		}

		/** Get page count */
		String htmlSrc = element.html();
		try {
			int position = htmlSrc.indexOf("pages");
			String s = htmlSrc.substring(0, position);
			//System.out.println(s);
			s = s.substring(s.lastIndexOf("<div>") + "<div>".length(), s.length()).trim();
			//System.out.println(s);
			book.pageCount = Integer.valueOf(s);
		} catch (Exception e) {

		}

		/** Get uploaded time */
		try {
			Element timeElement = doc.getElementsByTag("time").get(0);
			book.uploadTime = timeElement.attr("datetime");
			book.uploadTimeText = timeElement.text();
		} catch (Exception e) {

		}

		/** Get gallery id and preview image url */
		Element coverDiv = doc.getElementById("cover").getElementsByTag("a").get(0);
		for (Element e : coverDiv.getElementsByTag("img")) {
			try {
				//Log.i(TAG, coverDiv.html());
				String coverUrl;
				if (e.hasAttr("src")){
					coverUrl = e.attr("src");
				} else {
					coverUrl = e.attr("data-cfsrc");
				}
				//Log.i(TAG, coverUrl);
				coverUrl = coverUrl.substring(0, coverUrl.lastIndexOf("/"));
				String galleryId = coverUrl.substring(coverUrl.lastIndexOf("/") + 1, coverUrl.length());
				book.galleryId = galleryId;
				book.previewImageUrl = NHentaiUrl.getThumbUrl(galleryId);
				book.bigCoverImageUrl = NHentaiUrl.getBigCoverUrl(galleryId);
				break;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		/** Get more like this */
		Elements likesContainer = doc.getElementsByAttributeValue("id", "related-container")
                .get(0)
                .getElementsByClass("gallery");
        //Log.i(TAG, "Likes:" + likesContainer.html());
        book.likes = PageApi.getBooksFromGalleryElements(likesContainer);

        result.setCode(0);
        result.setData(book);

		return result;
	}

	private static ArrayList<String> analysisTags(String originHtml) {
		ArrayList<String> temp = new ArrayList<>();
		if(!originHtml.contains("a href=")){
			temp.add(0,null);
		}

		while (originHtml.contains("a href=")) {
			//Log.i(TAG, "origin=" + originHtml);
			String href = originHtml.substring(originHtml.indexOf("href=\"") + 7, originHtml.indexOf("\" class") - 1);
			//Log.i(TAG, "href=" + href);
			originHtml = originHtml.substring(originHtml.indexOf("\" class") + 7, originHtml.length());
			temp.add(href.substring(href.lastIndexOf("/") + 1).replaceAll("-", " "));
		}
		return temp;
	}
	
	public static Bitmap getCover(Context context, Book book) {
		String url = book.bigCoverImageUrl;
		FileCacheManager m = FileCacheManager.getInstance(context);
		
		if (!m.cacheExistsUrl(CACHE_COVER, url) && !m.createCacheFromNetwork(CACHE_COVER, url)) {
			return null;
		}

		return m.getBitmapUrl(CACHE_COVER, url);
	}

	public static Bitmap getThumb(Context context, Book book) {
		String url = book.previewImageUrl;
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_THUMB, url) && !m.createCacheFromNetwork(CACHE_THUMB, url)) {
			return null;
		}

		return m.getBitmapUrl(CACHE_THUMB, url);
	}

	public static Bitmap getPageThumb(Context context, Book book, int position) {
		String url = NHentaiUrl.getThumbPictureUrl(book.galleryId, Integer.toString(position));
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_PAGE_THUMB, url) && !m.createCacheFromNetwork(CACHE_PAGE_THUMB, url)) {
			return null;
		}

		return m.getBitmapUrl(CACHE_PAGE_THUMB, url);
	}

	public static File getCoverFile(Context context, Book book) {
		String url = book.bigCoverImageUrl;
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_COVER, url) && !m.createCacheFromNetwork(CACHE_COVER, url)) {
			return null;
		}

		return m.getBitmapUrlFile(CACHE_COVER, url);
	}

	public static File getThumbFile(Context context, Book book) {
		String url = book.previewImageUrl;
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_THUMB, url) && !m.createCacheFromNetwork(CACHE_THUMB, url)) {
			return null;
		}

		return m.getBitmapUrlFile(CACHE_THUMB, url);
	}

	public static File getPageThumbFile(Context context, Book book, int position) {
		String url = NHentaiUrl.getThumbPictureUrl(book.galleryId, Integer.toString(position));
		FileCacheManager m = FileCacheManager.getInstance(context);

		if (!m.cacheExistsUrl(CACHE_PAGE_THUMB, url) && !m.createCacheFromNetwork(CACHE_PAGE_THUMB, url)) {
			return null;
		}

		return m.getBitmapUrlFile(CACHE_PAGE_THUMB, url);
	}

}
