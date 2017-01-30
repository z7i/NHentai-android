package moe.feng.nhentai.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.HomeActivity;

import static moe.feng.nhentai.cache.common.Constants.CACHE_PAGE_IMG;

public class PageApi {

	public static final String TAG = PageApi.class.getSimpleName();

    public static BaseMessage getPageBook(String url) {
        BaseMessage result = new BaseMessage();
        JSONObject tagInspect;
        boolean english =false;
        boolean chinese = false;
        Book book = new Book();
        try {
            String json = Jsoup.connect(url).userAgent(HomeActivity.myContext.getString(R.string.user_agent)).ignoreContentType(true).execute().body();

            Log.d(TAG, "getPageListAPI: "+ json );
            try {
                JSONObject inspect = new JSONObject(json);
                JSONObject titles = inspect.getJSONObject("title");
                book.title = titles.getString("english");
                book.titleJP = titles.getString("japanese");
                book.titlePretty =titles.getString("pretty");
                book.galleryId = inspect.getString("media_id");
                book.bookId = inspect.getString("id");
                book.bigCoverImageUrl = NHentaiUrl.getBigCoverUrl(inspect.getString("media_id"));
                book.previewImageUrl = NHentaiUrl.getThumbUrl(inspect.getString("media_id"));
                book.uploadTime = inspect.getString("upload_date");
                book.uploadTimeText = inspect.getString("upload_date");
                JSONObject images = inspect.getJSONObject("images");
                JSONArray pages = images.getJSONArray("pages");
                book.pageCount =pages.length();

                JSONArray tags = inspect.getJSONArray("tags");

                for (int y =0; y< tags.length();y++){
                    tagInspect = tags.getJSONObject(y);
                    if (tagInspect.getString("type").equals("tag")){
                        book.tags.add(tagInspect.getString("name"));
                        book.tagID.add(tagInspect.getString("id"));
                    }
                    else if (tagInspect.getString("type").equals("artist")){
                        book.artists.add(tagInspect.getString("name"));
                        book.artistsID.add(tagInspect.getString("id"));
                    }

                    else if (tagInspect.getString("type").equals("character")){
                        book.characters.add(tagInspect.getString("name"));
                        book.charactersID.add(tagInspect.getString("id"));
                    }

                    else if (tagInspect.getString("type").equals("group")){
                        book.group = tagInspect.getString("name");
                        book.groupID = tagInspect.getString("id");
                    }

                    else if (tagInspect.getString("type").equals("parody")){
                        book.parodies = tagInspect.getString("name");
                        book.parodiesID = tagInspect.getString("id");
                    }

                    if (tagInspect.getString("name").equals("chinese")){
                        chinese= true;
                        book.language ="chinese";
                        book.langField=Book.LANG_CN;


                    }
                    else if(tagInspect.getString("name").equals("english")){
                        english = true;
                        book.language="english";
                        book.langField=Book.LANG_GB;
                    }
                }

                if(!chinese && !english){
                    book.language="japanese";
                    book.langField = Book.LANG_JP;
                }

            }

            catch (JSONException e){
                e.printStackTrace();
                Log.d(TAG, "getPageListAPI: ERROR 1");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "getPageListAPI: ERROR 2");
        }

        result.setData(book);
        result.setCode(0);

        return  result;
    }

    public static BaseMessage getPageList(String url) {
        BaseMessage result = new BaseMessage();
        result.setCode(0);
        try {
            String json = Jsoup.connect(url).userAgent(HomeActivity.myContext.getString(R.string.user_agent)).ignoreContentType(true).execute().body();
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray books = obj.getJSONArray("result");
                result.setData(getBooksFromJson(books));
            }
            catch (JSONException e){
                e.printStackTrace();
                Log.d(TAG, "getPageListAPI: ERROR 1");
                result.setCode(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "getPageListAPI: ERROR 2");
            result.setCode(1);
        }


        return  result;
    }

    public static ArrayList<Book> getBooksFromJson(JSONArray array) {
        ArrayList<Book> books = new ArrayList<>();
        JSONObject inspect;
        JSONObject tagInspect;
        boolean english;
        boolean chinese;

        for (int x =0; x<array.length();x++){
            Book book = new Book();
            english=false;
            chinese=false;
            try {
                inspect = array.getJSONObject(x);

                JSONObject titles = inspect.getJSONObject("title");
                book.title = titles.getString("english");
                book.titleJP = titles.getString("japanese");
                book.titlePretty =titles.getString("pretty");
                book.galleryId = inspect.getString("media_id");
                book.bookId = inspect.getString("id");
                book.bigCoverImageUrl = NHentaiUrl.getBigCoverUrl(inspect.getString("media_id"));
                book.previewImageUrl = NHentaiUrl.getThumbUrl(inspect.getString("media_id"));
                book.uploadTime = inspect.getString("upload_date");
                book.uploadTimeText = inspect.getString("upload_date");
                JSONObject images = inspect.getJSONObject("images");
                JSONArray pages = images.getJSONArray("pages");
                book.pageCount =pages.length();

                JSONArray tags = inspect.getJSONArray("tags");

                for (int y =0; y< tags.length();y++){
                    tagInspect = tags.getJSONObject(y);
                    if (tagInspect.getString("type").equals("tag")){
                        book.tags.add(tagInspect.getString("name"));
                        book.tagID.add(tagInspect.getString("id"));
                    }
                    else if (tagInspect.getString("type").equals("artist")){
                        book.artists.add(tagInspect.getString("name"));
                        book.artistsID.add(tagInspect.getString("id"));
                    }

                    else if (tagInspect.getString("type").equals("character")){
                        book.characters.add(tagInspect.getString("name"));
                        book.charactersID.add(tagInspect.getString("id"));
                    }

                    else if (tagInspect.getString("type").equals("group")){
                        book.group = tagInspect.getString("name");
                        book.groupID = tagInspect.getString("id");
                    }

                    else if (tagInspect.getString("type").equals("parody")){
                        book.parodies = tagInspect.getString("name");
                        book.parodiesID = tagInspect.getString("id");
                    }

                    if (tagInspect.getString("name").equals("chinese")){
                        chinese= true;
                        book.language ="chinese";
                        book.langField=Book.LANG_CN;


                    }
                    else if(tagInspect.getString("name").equals("english")){
                        english = true;
                        book.language="english";
                        book.langField=Book.LANG_GB;
                    }
                }

                if(!chinese && !english){
                    book.language="japanese";
                    book.langField = Book.LANG_JP;
                }

                books.add(book);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return books;
    }

	public static BaseMessage getHomePageList(int number) {
		return getPageList(NHentaiUrl.getHomePageUrl(number));
	}

    public static BaseMessage getBookDetailList(String id){
        return getPageBook(NHentaiUrl.getBookDetailsUrl(id));
    }

	public static BaseMessage getSearchPageList(String keyword, int number) {
		return getPageList(NHentaiUrl.getSearchUrl(keyword, number));
	}

	public static Bitmap getPageOriginImage(Context context, Book book, int page_num) {
		String url = NHentaiUrl.getOriginPictureUrl(book.galleryId, String.valueOf(page_num));

        if (FileCacheManager.getInstance(context).externalPageExists(book, page_num)){
            Log.i(TAG, "getPageOriginImage: Loaded from external");
            return FileCacheManager.getInstance(context).getBitmapFile(FileCacheManager.getInstance(context).getBitmapAllowingExternalPic(book, page_num));
        }
        if (FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_PAGE_IMG, url, book.bookId)){
            Log.i(TAG, "getPageOriginImage: Loaded from cache");
            return FileCacheManager.getInstance(context).getBitmapUrl(CACHE_PAGE_IMG, url, book.bookId);
        }
        else if(FileCacheManager.getInstance(context).createCacheFromNetwork(CACHE_PAGE_IMG, url, book.bookId)) {
            Log.i(TAG, "getPageOriginImage: Downloaded from web");
			return  FileCacheManager.getInstance(context).getBitmapUrl(CACHE_PAGE_IMG, url, book.bookId);
		}

        else{
            return null;
        }

	}

	public static File getPageOriginImageFile(Context context, Book book, int page_num) {
		String url = NHentaiUrl.getOriginPictureUrl(book.galleryId, String.valueOf(page_num));

		if (FileCacheManager.getInstance(context).externalPageExists(book, page_num)){
            Log.i(TAG, "getPageOriginImageFile: Loaded from external");
           return FileCacheManager.getInstance(context).getBitmapAllowingExternalPic(book, page_num);
        }

        else if (FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_PAGE_IMG, url, book.bookId)){
            Log.i(TAG, "getPageOriginImageFile: Loaded from cache");
            return FileCacheManager.getInstance(context).getBitmapAllowingExternalPic(book, page_num);
        }
        else if (FileCacheManager.getInstance(context).createCacheFromNetwork(CACHE_PAGE_IMG, url, book.bookId)){
            Log.i(TAG, "getPageOriginImageFile: Downloaded from web");
            return FileCacheManager.getInstance(context).getBitmapAllowingExternalPic(book, page_num);
		}
        else {
            return null;
        }
	}

	public static boolean isPageOriginImageLocalFileExist(Context context, Book book, int page_num) {
        return FileCacheManager.getInstance(context).externalPageExists(book,page_num);
    }

    public static boolean isPageOriginImageCacheFileExist(Context context, Book book, int page_num) {
        String url = NHentaiUrl.getOriginPictureUrl(book.galleryId, String.valueOf(page_num));
        return FileCacheManager.getInstance(context).cacheExistsUrl(CACHE_PAGE_IMG, url, book.bookId);
    }

}
