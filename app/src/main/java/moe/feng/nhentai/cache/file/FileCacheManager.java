package moe.feng.nhentai.cache.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.BookApi;
import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.common.Constants;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.HomeActivity;

import static moe.feng.nhentai.BuildConfig.DEBUG;

public class FileCacheManager {

	private static final String TAG = FileCacheManager.class.getSimpleName();

	private static FileCacheManager sInstance;

	private File mCacheDir, mExternalDir;

	private FileCacheManager(Context context) {
		try {
			mCacheDir = context.getExternalCacheDir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCacheDir == null) {
			String cacheAbsDir = "/Android/data" + context.getPackageName() + "/cache/";
			mCacheDir = new File(Environment.getExternalStorageDirectory().getPath() + cacheAbsDir);
		}
		if (mExternalDir == null) {
			String externalAbsDir = "/NHBooks/";
			mExternalDir = new File(Environment.getExternalStorageDirectory().getPath() + externalAbsDir);
		}
	}

	public static FileCacheManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new FileCacheManager(context);
		}

		return sInstance;
	}

	public boolean createCacheFromNetwork(String type, String url, String title) {
		if (DEBUG) {
			Log.d(TAG, "requesting cache from " + url);
		}

		URL u;

		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}

		HttpURLConnection conn;
		try {
			conn =  (HttpURLConnection)  u.openConnection();
			conn.setRequestProperty("User-Agent",HomeActivity.myContext.getString(R.string.user_agent));
			conn.connect();
		} catch (IOException e) {
			return false;
		}

		conn.setConnectTimeout(5000);
		try {
			if (conn.getResponseCode() != 200) {
				if (url.contains("jpg")) {
					try {
						u = new URL(url.replace("jpg", "png"));
					} catch (MalformedURLException ex) {
						return false;
					}
					try {
						conn = (HttpURLConnection) u.openConnection();
						conn.setRequestProperty("User-Agent",HomeActivity.myContext.getString(R.string.user_agent));
					} catch (IOException ex) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			return createCacheFromStrem(type, getCacheName(url),title, conn.getInputStream());
		} catch (IOException e) {
			return false;
		}
	}

	public boolean createCacheFromBook(Book book){
		String path = mCacheDir.getAbsolutePath() + "/Books/" + book.bookId;
		File d = new File(path);
		if (!d.exists()){
			if(!d.mkdirs()){
				Log.i(TAG, "createCacheFromBook: Error Creating Cache Directory");
			}
			else{
				Log.i(TAG, "createCacheFromBook: Cache Directory created succesfully");
			}
		}

		else if(!d.isDirectory()) {
			if(!d.mkdirs()){
				Log.i(TAG, "createCacheFromBook: Error Creating Cache Directory");
			}
		}

		File f = new File(path + "/book.json");

		if(!f.exists()){
			try {
				OutputStream out = new FileOutputStream(f);
				out.write(book.toJSONString().getBytes());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	public boolean createCacheFromStrem(String type, String name, String title, InputStream stream) {
		File f = new File(getCachePath(type, name, title) + "_downloading");

		if(f.getParentFile().mkdirs()){
			Log.i(TAG, "createCacheFromStrem: Direcotry created correctly");
		}

		if (f.exists()) {
			if(f.delete()){
				Log.i(TAG, "File Deleted Correctly ");
			}
		}

		try {
			if(!f.createNewFile()){
				Log.i(TAG, "createCacheFromStrem: Error creating new file");
			}
		} catch (IOException e) {
			return false;
		}

		FileOutputStream opt;

		try {
			opt = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			return false;
		}

		byte[] buf = new byte[512];
		int len;

		try {
			while ((len = stream.read(buf)) != -1) {
				opt.write(buf, 0, len);
			}
		} catch (IOException e) {
			return false;
		}

		try {
			stream.close();
			opt.close();
		} catch (IOException e) {
			Log.d(TAG, "createCacheFromStrem: Cache couldnt be created");
		}

		if(!f.renameTo(new File(getCachePath(type, name, title)))){
			Log.d(TAG, "createCacheFromStrem: Error Renaming File");
		}

		return true;
	}

	private boolean copy(File src, File dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean cacheExistsUrl(String type, String url,String title) {
		return cacheExists(type, getCacheName(url), title);
	}

	public boolean cacheExists(String type, String name, String title) {
		return new File(getCachePath(type, name, title)).isFile();
	}

	public boolean externalPageExists(Book book, int page) {
		return new File(getExternalPagePath(book, page)).isFile();

	}

	public boolean externalBookExists(Book book) {
		return new File(mExternalDir.getAbsolutePath()+ "/Books/" + book.bookId + "/book.json").isFile();
	}


	public boolean isExternalBookAllDownloaded(Book source) {
		return getExternalBookDownloadedCount(source) == source.pageCount;

	}
	@SuppressWarnings("unused")
	public boolean deleteCacheUrl(String type, String url, String title) {
		return deleteCache(type, getCacheName(url), title);
	}

	public boolean deleteCache(String type, String name,String title) {
		return  cacheExists(type, name, title) && new File(getCachePath(type, name, title)).delete();
	}

	public FileInputStream openCacheStream(String type, String name, String title) {
		try {
			return new FileInputStream(new File(getCachePath(type, name, title)));
		} catch (IOException e) {
			return null;
		}
	}
	@SuppressWarnings("unused")
	public FileInputStream openCacheStreamUrl(String type, String url, String title) {
		return openCacheStream(type, getCacheName(url), title);
	}

	public Bitmap getBitmapFile(File f) {
		Bitmap result;
		FileInputStream ipt= null;

		try {
			ipt = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (ipt == null) return null;

		BitmapFactory.Options bounds = new BitmapFactory.Options();
		bounds.inJustDecodeBounds=true;
		BitmapFactory.decodeStream(ipt,null,bounds);

		bounds.inSampleSize = calculateInSampleSize(bounds,720, 1280);

		Log.d(TAG, "getBitmap: " + bounds.inSampleSize);
		FileInputStream iptF;
		try {
			iptF = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			iptF=null;
		}

		bounds.inJustDecodeBounds=false;
		result=BitmapFactory.decodeStream(iptF,null, bounds);

		try {
			ipt.close();
			if (iptF!=null) iptF.close();
		} catch (IOException e) {
			Log.d(TAG, "getBitmap: Cleaning error");
			e.printStackTrace();

		}

		return result;
	}

	@SuppressWarnings("unused")
	public Bitmap getBitmap(String type, String name, String title) {
		Bitmap result;

		FileInputStream ipt= openCacheStream(type, name, title);
		if (ipt == null) return null;

		BitmapFactory.Options bounds = new BitmapFactory.Options();
		bounds.inJustDecodeBounds=true;
		BitmapFactory.decodeStream(ipt,null,bounds);

		bounds.inSampleSize = calculateInSampleSize(bounds,480, 640);

		FileInputStream iptF=openCacheStream(type,name, title);

		bounds.inJustDecodeBounds=false;
		result=BitmapFactory.decodeStream(iptF,null, bounds);

		try {
			ipt.close();
			iptF.close();
		} catch (IOException e) {
			Log.d(TAG, "getBitmap: Cleaning error");
			e.printStackTrace();

		}

		return result;
	}


	public File getBitmapAllowingExternalPic(Book book, int page) {
		File cache = new File(getCachePath(Constants.CACHE_PAGE_IMG,
				NHentaiUrl.getOriginPictureUrl(book.galleryId, Integer.toString(page)), book.bookId));
		File external = new File(getExternalPagePath(book, page));
		return external.isFile() ? external : cache;
	}

	public Bitmap getBitmapUrl(String type, String url, String title) {
		return getBitmap(type, getCacheName(url), title);
	}

	public Book getExternalBook(Book source) {
		File destination = new File(mExternalDir.getAbsolutePath()+ "/Books/"+ source.title +"/book.json");

		if (destination.exists() && destination.isFile()){
			try {
				FileInputStream ins = new FileInputStream(destination);

				byte b[] = new byte[(int) destination.length()];
				if(ins.read(b)==-1){
					Log.d(TAG, "getExternalBook: Error Reading External Book");
				}
				ins.close();

				Book book = new Gson().fromJson(new String(b), Book.class);
				if (book!=null)
					if (book.bookId.equals(source.bookId)) {
						Log.d(TAG, "Found bookId External: " + book.bookId);
						return book;
					}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		else {
			return null;
		}

		return null;
	}

	public Book getCacheBook(Book source) {
		File destination = new File(mCacheDir.getAbsolutePath()+ "/Books/"+ source.title +"/book.json");

		if (destination.exists() && destination.isFile()){
			try {
				FileInputStream ins = new FileInputStream(destination);

				byte b[] = new byte[(int) destination.length()];
				if(ins.read(b)==-1){
					Log.d(TAG, "getExternalBook: Error Reading Cache Book");
				}
				ins.close();

				Book book = new Gson().fromJson(new String(b), Book.class);
				if (book!=null)
					if (book.bookId.equals(source.bookId)) {
						Log.d(TAG, "Found bookId Cache: " + book.bookId);
						return book;
					}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		else {
			return null;
		}

		return null;
	}

	public boolean checkUpdate (){
		File updateFile = new File(mExternalDir.getAbsolutePath()+ "/update.txt");
		return updateFile.exists();
	}

	public void deleteCache() {
		try {
			File dir = new File(mCacheDir.getAbsolutePath());
			deleteDir(dir);
		} catch (Exception e) {}
	}

	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
			return dir.delete();
		} else if(dir!= null && dir.isFile()) {
			return dir.delete();
		} else {
			return false;
		}
	}

	public ArrayList<Book> getExternalBooks() {
		File parentDir = new File(mExternalDir.getAbsolutePath()+ "/Books/");

		if (parentDir.isDirectory()) {
			File[] files = parentDir.listFiles();
			ArrayList<Book> result = new ArrayList<>();
			for (File file : files) {
				if (file.isDirectory()  && !file.getName().equals("null")) {
					File bookFile = new File(file.getAbsolutePath() + "/book.json");
					if (bookFile.exists() && bookFile.isFile()) {
						try {
							FileInputStream ins = new FileInputStream(bookFile);

							byte b[] = new byte[(int) bookFile.length()];
							if(ins.read(b)==-1){
								Log.d(TAG, "getExternalBooks: Error reading file");
							}
							ins.close();

							Book book = Book.toBookFromJson(new String(b));
							Log.d(TAG, "Found external bookId: " + book.bookId);
							result.add(book);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return result;
		} else {
			return new ArrayList<>();
		}
	}

	public void updateExternalBooks() {
		File parentDir = new File(mExternalDir.getAbsolutePath()+ "/Books/");
		File newFolder = new File(mExternalDir.getAbsolutePath()+ "/Books/");;
		if (parentDir.isDirectory()) {
			File[] files = parentDir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					File bookFile = new File(file.getAbsolutePath() + "/book.json");
					if (bookFile.exists() && bookFile.isFile()) {
						try {
							FileInputStream ins = new FileInputStream(bookFile);

							byte b[] = new byte[(int) bookFile.length()];
							if (ins.read(b) == -1) {
								Log.d(TAG, "getExternalBooks: Error reading file");
							}
							ins.close();

							Book book = Book.toBookFromJson(new String(b));
							String id = book.bookId;
							newFolder = new File(mExternalDir.getAbsolutePath()+ "/Books/" + id);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					Boolean result = file.renameTo(newFolder);
					Log.d(TAG, "updateExternalBooks: "+ result);
				}
			}
			File updateFile = new File(mExternalDir.getAbsolutePath()+ "/update.txt");
			try {
				OutputStream out = new FileOutputStream(updateFile);
				out.write(String.valueOf("updated").getBytes());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int getExternalBookDownloadedCount(Book source) {
		Book book = getExternalBook(source);
		if (book != null && externalBookExists(book)) {
			File parentDir = new File(mExternalDir.getAbsolutePath()+ "/Books/" + book.bookId);
			String[] pngs = parentDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File file, String s) {
					return s.endsWith(".png");
				}
			});
			return pngs != null ? pngs.length : 0;
		} else {
			return -1;
		}
	}

	public String getCacheName(String url) {
		return url.replaceAll("/", ".").replaceAll(":", "");
	}

	public String getCachePath(String type, String name, String title) {
		return mCacheDir.getAbsolutePath() + "/Books/" +title +"/"+ type + "/" + name + ".cache";
	}

	public String getExternalPath(Book book) {
		return mExternalDir.getAbsolutePath() + "/Books/" + book.bookId;
	}

	public String getExternalPagePath(Book book, int page) {
		return getExternalPath(book) + "/" + String.format(Locale.getDefault(),"%03d", page) + ".png";
	}

	public boolean saveToExternalPath(Book book, int page) {
		String path = getExternalPagePath(book, page);
		String src = getCachePath(Constants.CACHE_PAGE_IMG, getCacheName(NHentaiUrl.getOriginPictureUrl(book.galleryId, Integer.toString(page))), book.bookId);
		File target = new File(path);
		File srcFile = new File(src);

		if (target.exists()&& target.isFile()){
			Log.d(TAG, "saveToExternalPath: File Alredy Downloaded");
			return true;
		}

		File targetParent = new File(mExternalDir.getAbsolutePath() + "/Books/" + book.bookId);
		if (targetParent.isFile()) {
			if(!targetParent.delete()){
				Log.i(TAG, "saveToExternalPath: Error Removing Past File");
			}
		}
		if (!targetParent.isDirectory()) {
			if(!targetParent.mkdirs()){
				Log.i(TAG, "saveToExternalPath: Error create Parent Dir");
			}
		}

		if (target.exists()) {
			if(!target.delete()){
				Log.i(TAG, "saveToExternalPath: Error Deleting target");
			}
		}

		return  srcFile.isFile() && copy(srcFile, target);
	}

	public boolean saveBookDataToExternalPath(Book book) {
		String path = getExternalPath(book);
		File d = new File(path);

		if (!d.isDirectory()) {
			if(!d.delete()){
				Log.i(TAG, "saveBookDataToExternalPath: Error deleting old file");
			}
			if(!d.mkdirs()){
				Log.i(TAG, "saveBookDataToExternalPath: Error creating New Folder");
			}
		}

		File f = new File(path + "/book.json");


		if(!f.exists()){
			try {

				OutputStream out = new FileOutputStream(f);

				out.write(book.toJSONString().getBytes());

				out.close();
				Log.d(TAG, "saveBookDataToExternalPath: Wroted");
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}


		return true;
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

}
