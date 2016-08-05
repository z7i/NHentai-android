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

import moe.feng.nhentai.api.BookApi;
import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.common.Constants;
import moe.feng.nhentai.model.Book;

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
			conn = (HttpURLConnection) u.openConnection();
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
		String path = mCacheDir.getAbsolutePath() + "/Books/" + book.title;

		File d = new File(path);

		if (!d.isDirectory()) {
			d.delete();
			d.mkdirs();
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
		f.getParentFile().mkdirs();
		f.getParentFile().mkdir();

		if (f.exists()) {
			f.delete();
		}

		try {
			f.createNewFile();
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
		int len = 0;

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

		}

		f.renameTo(new File(getCachePath(type, name, title)));

		return true;
	}

	private boolean copy(File src, File dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			// Transfer bytes from in to out
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

	// True if the cache downloaded from url exists
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
		return new File(mExternalDir.getAbsolutePath()+ "/Books/" + book.title + "/book.json").isFile();
	}

	public boolean externalBookExists(String bookId) {
		return new File(mExternalDir.getAbsolutePath()+ "/Books/" + getExternalBook(bookId).title + "/book.json").isFile();
	}

	public boolean isExternalBookAllDownloaded(String bid) {
		Book book = getExternalBook(bid);
		if (book != null) {
			return getExternalBookDownloadedCount(book.bookId) == book.pageCount;
		} else {
			return false;
		}
	}

	public boolean deleteCacheUrl(String type, String url, String title) {
		return deleteCache(type, getCacheName(url), title);
	}

	public boolean deleteCache(String type, String name,String title) {
		if (cacheExists(type, name, title)) {
			return new File(getCachePath(type, name, title)).delete();
		} else {
			return false;
		}
	}

	public FileInputStream openCacheStream(String type, String name, String title) {
		try {
			return new FileInputStream(new File(getCachePath(type, name, title)));
		} catch (IOException e) {
			return null;
		}
	}

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

		bounds.inSampleSize = calculateInSampleSize(bounds,480, 640);

		Log.d(TAG, "getBitmap: " + bounds.inSampleSize);
		FileInputStream iptF =null;
		try {
			iptF = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		bounds.inJustDecodeBounds=false;
		result=BitmapFactory.decodeStream(iptF,null, bounds);

		try {
			ipt.close();
			iptF.close();
			iptF=null;
			ipt=null;
			bounds = null;
		} catch (IOException e) {
			Log.d(TAG, "getBitmap: Cleaning error");
			e.printStackTrace();

		}

		return result;
	}


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
			iptF=null;
			ipt=null;
			bounds = null;
		} catch (IOException e) {
			Log.d(TAG, "getBitmap: Cleaning error");
			e.printStackTrace();

		}

		return result;
	}


	public File getBitmapAllowingExternalPic(Book book, int page) {
		File cache = new File(getCachePath(Constants.CACHE_PAGE_IMG,
				NHentaiUrl.getOriginPictureUrl(book.galleryId, Integer.toString(page)), book.title));
		File external = new File(getExternalPagePath(book, page));
		return external.isFile() ? external : cache;
	}

	public Bitmap getBitmapUrl(String type, String url, String title) {
		return getBitmap(type, getCacheName(url), title);
	}

	public File getBitmapFile(String type, String name, String title) {
		return new File(getCachePath(type, name, title));
	}

	public File getBitmapUrlFile(String type, String url, String title) {
		return getBitmapFile(type, getCacheName(url), title);
	}

	public Book getExternalBook(String bid) {
		File parentDir = new File(mExternalDir.getAbsolutePath()+ "/Books/");

		if (parentDir.isDirectory()) {
			File[] files = parentDir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					File bookFile = new File(file.getAbsolutePath() + "/book.json");
					if (bookFile.isFile()) {
						try {
							InputStream ins = new FileInputStream(bookFile);

							byte b[] = new byte[(int) bookFile.length()];
							ins.read(b);
							ins.close();

							Book book = new Gson().fromJson(new String(b), Book.class);
							if (book!=null)
							if (book.bookId.equals(bid)) {
								Log.i(TAG, "Found bookId Cache: " + book.bookId);
								return book;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return null;
		} else {
			return null;
		}

	}

	public boolean updateSaved(Context context){
		File parentDir = new File(mExternalDir.getAbsolutePath()+ "/Books/");

		if (parentDir.isDirectory()) {
			File[] files = parentDir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					File bookFile = new File(file.getAbsolutePath() + "/book.json");
					if (bookFile.isFile()) {
						try {
							InputStream ins = new FileInputStream(bookFile);

							byte b[] = new byte[(int) bookFile.length()];
							ins.read(b);
							ins.close();

							Book book = Book.toBookFromJson(new String(b));
							String id = book.bookId;

							if (bookFile.delete()){
								Log.d(TAG, "updateSaved: Erased correctly");
							}

							book = (Book) BookApi.getBook(context, id).getData();
							Log.i(TAG, "Updated external: " + book.bookId);
							saveBookDataToExternalPath(book);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			return false;
		}

		parentDir = new File(mCacheDir.getAbsolutePath()+ "/pages/");
		if (parentDir.exists()) {
			File[] files = parentDir.listFiles();
			for (File file : files) {
				file.delete();
			}
			parentDir.delete();
		}

		parentDir = new File(mCacheDir.getAbsolutePath()+ "/thumb/");
		if (parentDir.exists()) {
			File[] files = parentDir.listFiles();
			for (File file : files) {
				file.delete();
			}
			parentDir.delete();
		}

		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updated.txt");
		try {
			OutputStream out = new FileOutputStream(updateFile);
			out.write(String.valueOf("updated").getBytes());
			out.close();
			Log.d(TAG, "Version Updated Wroted");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean checkUpdate (){
		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updated.txt");
		return updateFile.exists();
	}

	public boolean checkUpdateFavorites (){
		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updateFab.txt");
		return updateFile.exists();
	}

	public boolean checkUpdateLatest(){
		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updateLat.txt");
		return updateFile.exists();
	}

	public boolean checkUpdateCategories(){
		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updateCat.txt");
		return updateFile.exists();
	}

	public boolean UpdateFavoriteCategories(){
		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updateCat.txt");
		try {
			OutputStream out = new FileOutputStream(updateFile);
			out.write(String.valueOf("updated").getBytes());
			out.close();
			Log.d(TAG, "Categories Updated Wroted");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean UpdateFavorites(){
		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updateFab.txt");
		try {
			OutputStream out = new FileOutputStream(updateFile);
			out.write(String.valueOf("updated").getBytes());
			out.close();
			Log.d(TAG, "Favorites Updated Wroted");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean UpdateLatest(){
		File updateFile = new File(mCacheDir.getAbsolutePath()+ "/Books/updateLat.txt");
		try {
			OutputStream out = new FileOutputStream(updateFile);
			out.write(String.valueOf("updated").getBytes());
			out.close();
			Log.d(TAG, "Latest Updated Wroted");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<Book> getExternalBooks() {
		File parentDir = new File(mExternalDir.getAbsolutePath()+ "/Books/");

		if (parentDir.isDirectory()) {
			File[] files = parentDir.listFiles();
			ArrayList<Book> result = new ArrayList<>();
			for (File file : files) {
				if (file.isDirectory()) {
					File bookFile = new File(file.getAbsolutePath() + "/book.json");
					if (bookFile.isFile()) {
						try {
							InputStream ins = new FileInputStream(bookFile);

							byte b[] = new byte[(int) bookFile.length()];
							ins.read(b);
							ins.close();

							Book book = Book.toBookFromJson(new String(b));
							Log.i(TAG, "Found external bookId: " + book.bookId);
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

	public int getExternalBookDownloadedCount(String bookId) {
		Book book = getExternalBook(bookId);
		if (book != null && externalBookExists(book)) {
			File parentDir = new File(mExternalDir.getAbsolutePath()+ "/Books/" + book.title);
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
		return mExternalDir.getAbsolutePath() + "/Books/" + book.title;
	}

	public String getExternalPagePath(Book book, int page) {
		return getExternalPath(book) + "/" + String.format("%03d", page) + ".png";
	}

	public boolean saveToExternalPath(Book book, int page) {
		String path = getExternalPagePath(book, page);
		String src = getCachePath(Constants.CACHE_PAGE_IMG, getCacheName(NHentaiUrl.getOriginPictureUrl(book.galleryId, Integer.toString(page))), book.title);
		File target = new File(path);
		File srcFile = new File(src);

		File targetParent = new File(mExternalDir.getAbsolutePath() + "/Books/" + book.title);
		if (targetParent.isFile()) {
			targetParent.delete();
		}
		if (!targetParent.isDirectory()) {
			targetParent.mkdirs();
		}

		if (target.exists()) {
			target.delete();
		}

		return  srcFile.isFile() && copy(srcFile, target);
	}

	public boolean saveBookDataToExternalPath(Book book) {
		String path = getExternalPath(book);
		File d = new File(path);

		if (!d.isDirectory()) {
			d.delete();
			d.mkdirs();
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
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

}
