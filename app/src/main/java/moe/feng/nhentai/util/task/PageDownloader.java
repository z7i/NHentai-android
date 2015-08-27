package moe.feng.nhentai.util.task;

import android.content.Context;
import android.util.Log;

import java.io.File;

import moe.feng.nhentai.api.PageApi;
import moe.feng.nhentai.model.Book;

public class PageDownloader {

	private Context context;
	private Book book;
	private int currentPosition, downloadingPosition = -1;
	private OnDownloadListener listener;
	private DownloadThread mDownloadThread;
	private int state;
	private boolean[] isDownloaded;

	public static final int STATE_START = 100, STATE_PAUSE = 101, STATE_STOP = 102, STATE_ALL_OK = 103;

	public static final String TAG = PageDownloader.class.getSimpleName();

	public PageDownloader(Context context, Book book) {
		this.context = context;
		this.book = book;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	public int getCurrentPosition() {
		return this.currentPosition;
	}

	private int nextToDownloadPosition() {
		int pos = findFirstUndownloadedPosition(getCurrentPosition());
		if (pos == book.pageCount - 1) {
			pos = findFirstUndownloadedPosition(0);
		}
		return pos;
	}

	private int findFirstUndownloadedPosition(int start) {
		for (int i = start; i < book.pageCount; i++) {
			if (!isDownloaded[i]) {
				Log.i(TAG, i + " is undownloaded.");
				return i;
			}
		}
		return book.pageCount - 1;
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

	public void pause() {
		Log.i(TAG, "download pause");
		state = STATE_PAUSE;
	}

	public void stop() {
		Log.i(TAG, "download stop");
		state = STATE_STOP;
	}

	public boolean isDownloaded(int position) {
		return isDownloaded[position];
	}

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

	public boolean isDownloading() {
		return mDownloadThread != null && mDownloadThread.isRunning;
	}

	public OnDownloadListener getOnDownloadListener() {
		return listener;
	}

	public void setOnDownloadListener(OnDownloadListener listener) {
		this.listener = listener;
	}

	public interface OnDownloadListener {

		void onFinish(int position);
		void onError(int position, int errorCode);
		void onStateChange(int state);

	}

	private class DownloadThread extends Thread {

		public boolean isRunning = true;

		@Override
		public void run() {
			Log.i(TAG, "download thread start");
			if (listener != null) listener.onStateChange(STATE_START);
			while (isRunning && !isAllDownloaded()) {
				downloadingPosition = nextToDownloadPosition();
				Log.i(TAG, "downloadingPosition:" + downloadingPosition);
				if (state == STATE_PAUSE) {
					Log.i(TAG, "download paused");
					if (listener != null) listener.onStateChange(STATE_PAUSE);
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
					if (listener != null) listener.onStateChange(STATE_STOP);
					isRunning = false;
					return;
				}
				File tempFile = null;
				try {
					tempFile = PageApi.getPageOriginImageFile(context, book, downloadingPosition + 1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (tempFile != null) {
					Log.i(TAG, "download finish");
					isDownloaded[downloadingPosition] = true;
					if (listener != null) listener.onFinish(currentPosition);
				} else {
					Log.i(TAG, "download error");
					if (listener != null) listener.onError(currentPosition, -1);
				}
			}
			Log.i(TAG, "all downloaded");
			if (listener != null) listener.onStateChange(STATE_ALL_OK);
			isRunning = false;
		}

	}

}
