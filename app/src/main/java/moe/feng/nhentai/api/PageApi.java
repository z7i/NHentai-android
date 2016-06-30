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
import java.net.URL;
import java.util.ArrayList;

import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;

import static moe.feng.nhentai.cache.common.Constants.CACHE_PAGE_IMG;

public class PageApi {

	public static final String TAG = PageApi.class.getSimpleName();

	public static BaseMessage getPageList(String url) {
		BaseMessage result = new BaseMessage();

		Document doc;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			result.setCode(403);
			e.printStackTrace();
			return result;
		}

		Elements container = doc.getElementsByClass("gallery");

		result.setCode(0);
		result.setData(getBooksFromGalleryElements(container));

		return result;
	}

    public static ArrayList<Book> getBooksFromGalleryElements(Elements container) {
        ArrayList<Book> books = new ArrayList<>();

        for (Element e : container) {
            Book book = new Book();

            String tagIds = e.attributes().get("data-tags");
            if (tagIds.contains(Book.LANG_CN)) {
                book.langField = Book.LANG_CN;
            } else if (tagIds.contains(Book.LANG_JP)) {
                book.langField = Book.LANG_JP;
            } else {
                book.langField = Book.LANG_GB;
            }

            Element bookIdElement = e.getElementsByClass("cover").get(0);
            String bookId = bookIdElement.attr("href");
            bookId = bookId.substring(0, bookId.lastIndexOf("/"));
            bookId = bookId.substring(bookId.lastIndexOf("/") + 1, bookId.length());

            Element caption = e.getElementsByClass("caption").get(0);

            book.bookId = bookId;
            book.title = caption.text();
            book.titleJP = caption.text();

            Elements imgs = e.getElementsByTag("img");
            for (Element imge : imgs) {
                if (imge.hasAttr("src")) {
                    String thumbUrl = imge.attr("src");
                    thumbUrl = thumbUrl.substring(0, thumbUrl.lastIndexOf("/"));
                    String galleryId = thumbUrl.substring(thumbUrl.lastIndexOf("/") + 1, thumbUrl.length());
                    book.galleryId = galleryId;
                    book.bigCoverImageUrl = NHentaiUrl.getBigCoverUrl(galleryId);
                    book.previewImageUrl = NHentaiUrl.getThumbUrl(galleryId);
                    try {
                        book.thumbHeight = Integer.valueOf(imge.attr("height"));
                        book.thumbWidth = Integer.valueOf(imge.attr("width"));
                    } catch (Exception ex) {

                    }
                }
            }

            if (book.bookId != null && !book.bookId.isEmpty()) {
                books.add(book);
            }

            Log.i(TAG, "Get book: " + book.toJSONString());
        }

        return books;
    }

	public static BaseMessage getHomePageList(int number) {
		return getPageList(NHentaiUrl.getHomePageUrl(number));
	}

	public static BaseMessage getSearchPageList(String keyword, int number) {
		return getPageList(NHentaiUrl.getSearchUrl(keyword, number));
	}

	public static Bitmap getPageOriginImage(Context context, Book book, int page_num) {
		String url = NHentaiUrl.getOriginPictureUrl(book.galleryId, String.valueOf(page_num));


        if (FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_PAGE_IMG, url)){
            Log.d(TAG, "getPageOriginImage: Loaded from cache");
            return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_PAGE_IMG, url);
        }
        else if(FileCacheManager.getInstance(context).createCacheFromNetwork(CACHE_PAGE_IMG, url)) {
            Log.d(TAG, "getPageOriginImage: Downloaded from web");
			return  FileCacheManager.getInstance(context).getBitmapUrl(CACHE_PAGE_IMG, url);
		}

        else{
            return null;
        }

	}

	public static File getPageOriginImageFile(Context context, Book book, int page_num) {
		String url = NHentaiUrl.getOriginPictureUrl(book.galleryId, String.valueOf(page_num));

		if (FileCacheManager.getInstance(context).externalPageExists(book, page_num)){
            Log.d(TAG, "getPageOriginImage: Loaded from external");
           return FileCacheManager.getInstance(context).getBitmapAllowingExternalPic(book, page_num);
        }

        else if (FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_PAGE_IMG, url)){
            Log.d(TAG, "getPageOriginImage: Loaded from cache");
            return FileCacheManager.getInstance(context).getBitmapAllowingExternalPic(book, page_num);
        }
        else if (FileCacheManager.getInstance(context).createCacheFromNetwork(CACHE_PAGE_IMG, url)){
            Log.d(TAG, "getPageOriginImage: Downloaded from web");
            return FileCacheManager.getInstance(context).getBitmapAllowingExternalPic(book, page_num);
		}
        else {
            return null;
        }
	}

    public static String getPageOriginImageURL(Context context, Book book, int page_num){
        return NHentaiUrl.getOriginPictureUrl(book.galleryId, String.valueOf(page_num));
    }

	public static boolean isPageOriginImageLocalFileExist(Context context, Book book, int page_num) {
		String url = NHentaiUrl.getOriginPictureUrl(book.galleryId, String.valueOf(page_num));

		return FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_PAGE_IMG, url);
	}

}
