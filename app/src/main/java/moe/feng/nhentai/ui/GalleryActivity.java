package moe.feng.nhentai.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;

import moe.feng.nhentai.R;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.adapter.GalleryPagerAdapter;
import moe.feng.nhentai.ui.common.AbsActivity;
import moe.feng.nhentai.util.FullScreenHelper;
import moe.feng.nhentai.util.task.PageDownloader;
import moe.feng.nhentai.view.SeekBar;

public class GalleryActivity extends AbsActivity {

	private Book book;
	private int page_num;

	private ViewPager mPager;
	private GalleryPagerAdapter mPagerAdpater;
	private View mAppBar, mBottomBar;
	private SeekBar mSeekBar;
	private AppCompatTextView mTotalPagesText;

	private FullScreenHelper mFullScreenHelper;

	private PageDownloader mDownloader;

	private static final String EXTRA_BOOK_DATA = "book_data", EXTRA_FISRT_PAGE = "first_page";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= 21) {
			getWindow().setStatusBarColor(Color.TRANSPARENT);
			getWindow().setNavigationBarColor(Color.TRANSPARENT);
		}

		mFullScreenHelper = new FullScreenHelper(this);
		// 别问我为什么这么干 让我先冷静一下→_→
		mFullScreenHelper.setFullScreen(true);
		mFullScreenHelper.setFullScreen(false);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent intent = getIntent();
		book = new Gson().fromJson(intent.getStringExtra(EXTRA_BOOK_DATA), Book.class);
		page_num = intent.getIntExtra(EXTRA_FISRT_PAGE, 0);
		mDownloader = new PageDownloader(getApplicationContext(), book);
		mDownloader.setCurrentPosition(page_num);
		mDownloader.setOnDownloadListener(new GalleryDownloaderListener());

		setContentView(R.layout.activity_gallery);
	}

	@Override
	public void onResume() {
		super.onResume();
		mDownloader.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		mDownloader.pause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mDownloader.stop();
	}

	@Override
	protected void setUpViews() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(book.titleJP != null ? book.titleJP : book.title);

		mAppBar = $(R.id.my_app_bar);
		mBottomBar = $(R.id.bottom_bar);
		mPager = $(R.id.pager);
		mSeekBar = $(R.id.seekbar);
		mTotalPagesText = $(R.id.total_pages_text);
		mPagerAdpater = new GalleryPagerAdapter(getFragmentManager(), book);
		mPager.setAdapter(mPagerAdpater);
		mPager.setCurrentItem(page_num, false);
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				mSeekBar.setValue(position + 1, true);
				mDownloader.setCurrentPosition(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mTotalPagesText.setText(String.format(getString(R.string.info_total_pages), book.pageCount));
		mSeekBar.setValueRange(1, book.pageCount, false);
		mSeekBar.setValue(page_num + 1, false);
		mSeekBar.setOnTouchListener(new View.OnTouchListener() {
			// TODO Maybe it will cause some problems if using OnPositionChangedListener.
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					mPager.setCurrentItem(mSeekBar.getValue() - 1, false);
				}
				return view.onTouchEvent(motionEvent);
			}
		});
	}


	public static void launch(Activity activity, Book book, int firstPageNum) {
		Intent intent = new Intent(activity, GalleryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.putExtra(EXTRA_BOOK_DATA, book.toJSONString());
		intent.putExtra(EXTRA_FISRT_PAGE, firstPageNum);
		activity.startActivity(intent);
	}

	public void toggleControlBar() {
		if (mAppBar.getAlpha() != 0f) {
			ViewCompat.animate(mAppBar).alpha(0f).start();
			ViewCompat.animate(mBottomBar).alpha(0f).start();
			mFullScreenHelper.setFullScreen(true);
		} else if (mAppBar.getAlpha() != 1f) {
			ViewCompat.animate(mAppBar).alpha(1f).start();
			ViewCompat.animate(mBottomBar).alpha(1f).start();
			mFullScreenHelper.setFullScreen(false);
		}
	}

	@Override
	public void onBackPressed() {
		if (mAppBar.getAlpha() != 1f) {
			toggleControlBar();
		} else {
			super.onBackPressed();
		}
	}

	public PageDownloader getPageDownloader() {
		return mDownloader;
	}

	private class GalleryDownloaderListener implements PageDownloader.OnDownloadListener {

		@Override
		public void onFinish(int position) {
			if (mPagerAdpater != null) {
				mPagerAdpater.notifyPageImageLoaded(position, true);
			}
		}

		@Override
		public void onError(int position, int errorCode) {
			if (mPagerAdpater != null) {
				mPagerAdpater.notifyPageImageLoaded(position, false);
			}
		}

		@Override
		public void onStateChange(int state) {

		}

	}

}
