package moe.feng.nhentai.util.task;

import android.content.Context;
import android.util.Log;

import java.io.File;

import moe.feng.nhentai.api.PageApi;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.Book;

public class BookDownloader {

	private Context context;
	private Book book;
	private int downloadingPosition = -1;
	private OnDownloadListener listener;
	private DownloadThread mDownloadThread;
	private FileCacheManager mFCM;
	private int state;
	private boolean[] isDownloaded;

	public static final int STATE_START = 100, STATE_PAUSE = 101, STATE_STOP = 102, STATE_ALL_OK = 103;

	public static final String TAG = BookDownloader.class.getSimpleName();

	public Book getBook() {
		return book;
	}

	public BookDownloader(Context context, Book book) {
		this.context = context;
		this.book = book;
		this.mFCM = FileCacheManager.getInstance(context);
	}

	public void start() {
		Log.i(TAG, "download start");
		if (mDownloadThread != null) {
			mDownloadThread.isRunning = false;
			try {
				mDownloadThread.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mDownloadThread = new DownloadThread();
		downloadingPosition = -1;
		isDownloaded = new boolean[book.pageCount];
		for (int i = 0; i < book.pageCount; i++) {
			isDownloaded[i] = PageApi.isPageOriginImageLocalFileExist(context, book, i + 1);
		}
		state = STATE_START;
		mDownloadThread.start();
	}

	public void continueDownload() {
		Log.i(TAG, "download continue");
		if (mDownloadThread != null && mDownloadThread.isRunning) {
			state = STATE_START;
		} else {
			this.start();
		}
	}

	public void pause() {
		Log.i(TAG, "download pause");
		state = STATE_PAUSE;
	}

	public void stop() {
		Log.i(TAG, "download stop");
		state = STATE_STOP;
	}

	@SuppressWarnings("unused")
	public boolean isDownloaded(int position) {
		return isDownloaded[position];
	}

	@SuppressWarnings("unused")
	public void setDownloaded(int position, boolean bool) {
		isDownloaded[position] = bool;
	}

	public boolean isAllDownloaded() {
		boolean b = true;
		for (int i = 0; i < book.pageCount && b; i++) {
			b = isDownloaded[i];
		}
		return b;
	}

	public int getDownloadedCount() {
		int i = 0;
		for (boolean b : isDownloaded) {
			if (b) {
				i++;
			}
		}
		return i;
	}

	public boolean isDownloading() {
		return mDownloadThread != null && mDownloadThread.isRunning;
	}

	@SuppressWarnings("unused")
	public boolean isStop() {
		return state == STATE_STOP;
	}

	public boolean isPause() {
		return state == STATE_PAUSE;
	}

	public boolean isThreadAllOk() {
		return state == STATE_ALL_OK;
	}

	@SuppressWarnings("unused")
	public OnDownloadListener getOnDownloadListener() {
		return listener;
	}

	public void setOnDownloadListener(OnDownloadListener listener) {
		this.listener = listener;
	}

	public interface OnDownloadListener {

		void onFinish(int position, int progress);
		void onError(int position, int errorCode);
		void onStateChange(int state, int progress);

	}

	private class DownloadThread extends Thread {

		public boolean isRunning = true;

		@Override
		public void run() {
			Log.i(TAG, "download thread start");
			if (listener != null) listener.onStateChange(STATE_START, getDownloadedCount());
			downloadingPosition = -1;
			while (isRunning && !isAllDownloaded()) {
				downloadingPosition++;
				if (state == STATE_PAUSE) {
					Log.i(TAG, "download paused");
					if (listener != null) listener.onStateChange(STATE_PAUSE, getDownloadedCount());
					while (state == STATE_PAUSE) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if (state == STATE_STOP) {
					Log.i(TAG, "download stopped");
					if (listener != null) listener.onStateChange(STATE_STOP, getDownloadedCount());
					isRunning = false;
					return;
				}
				File tempFile = null;
				try {
					tempFile = PageApi.getPageOriginImageFile(context, book, downloadingPosition + 1);
					mFCM.saveToExternalPath(book, downloadingPosition + 1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (tempFile != null) {
					Log.i(TAG, "Finished downloading page " + downloadingPosition+1);
					isDownloaded[downloadingPosition] = true;
					if (listener != null) listener.onFinish(book.pageCount,getDownloadedCount());
				} else {
					Log.i(TAG, "download error");
					if (listener != null) listener.onError(book.pageCount, -1);
				}
			}
			Log.i(TAG, "all downloaded");
			if (listener != null) listener.onStateChange(STATE_ALL_OK, getDownloadedCount());
			isRunning = false;
		}

	}

}
