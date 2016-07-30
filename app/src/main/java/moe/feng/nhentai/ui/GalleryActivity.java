package moe.feng.nhentai.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatTextView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.google.gson.Gson;

import moe.feng.nhentai.R;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.adapter.GalleryPagerAdapter;
import moe.feng.nhentai.ui.common.AbsActivity;
import moe.feng.nhentai.util.FullScreenHelper;
import moe.feng.nhentai.util.Utility;
import moe.feng.nhentai.util.task.PageDownloader;

public class GalleryActivity extends AbsActivity implements OnTouchListener {
	public static Context mContext;
	private Book book;
	private int page_num;
	private ViewPager mPager;
	private GalleryPagerAdapter mPagerAdpater;
	private View mAppBar, mBottomBar;
	private AppCompatSeekBar mSeekBar;
	private AppCompatTextView mTotalPagesText;
	private int orientation;
	private int lastOrientation;
	private int lastpositon;
	private int gPosition;
	private FullScreenHelper mFullScreenHelper;
	private PageDownloader mDownloader;
	private int scrolled;
	private boolean gRight;
	private boolean button;
	private static final String EXTRA_BOOK_DATA = "book_data", EXTRA_FISRT_PAGE = "first_page";

	@Override
	public boolean onTouch(View view, MotionEvent me) {
		return true;
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		gRight=false;
		button=true;
		lastpositon =gPosition;

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			if (gPosition< book.pageCount-1)
				gPosition++;
		}

		else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
				if(gPosition>0) {
					gRight = true;
					gPosition--;
				}
		}

		mSeekBar.setProgress(gPosition);
		mDownloader.setCurrentPosition(gPosition);
		mPager.setCurrentItem(gPosition, false);

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !Utility.isChrome()) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			statusBarHeight = Utility.getStatusBarHeight(getApplicationContext());
		}

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
		mDownloader.start();

		scrolled =0;
		setContentView(R.layout.activity_gallery);
		mContext =getApplicationContext();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();
		mDownloader.stop();
		Runtime.getRuntime().gc();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.menu_gallery, menu);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_save) {
			if (mDownloader.isDownloaded(mPager.getCurrentItem())) {
				FileCacheManager m = FileCacheManager.getInstance(getApplicationContext());
				String externalTarget = m.getExternalPagePath(book, mPager.getCurrentItem() + 1);
				if (m.saveToExternalPath(book, mPager.getCurrentItem() + 1)){
					Snackbar.make($(R.id.space_layout), String.format(
							getString(R.string.action_save_succeed),
							externalTarget
					), Snackbar.LENGTH_SHORT).show();
					return true;
				}
				Snackbar.make($(R.id.space_layout), R.string.action_save_unknown, Snackbar.LENGTH_SHORT).show();
			} else {
				Snackbar.make($(R.id.space_layout), R.string.action_save_failed, Snackbar.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void setUpViews() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(book.getAvailableTitle());
		lastpositon =0;
		gPosition =0;
		button=false;
		gRight=false;
		orientation = Configuration.ORIENTATION_PORTRAIT;
		lastOrientation = Configuration.ORIENTATION_PORTRAIT;
		mAppBar = $(R.id.my_app_bar);
		mBottomBar = $(R.id.bottom_bar);
		mPager = $(R.id.pager);
		mSeekBar = $(R.id.seekbar);
		mTotalPagesText = $(R.id.total_pages_text);
		mPagerAdpater = new GalleryPagerAdapter(getFragmentManager(), book);
		mPager.setAdapter(mPagerAdpater);
		mPager.setOffscreenPageLimit(1);
		mPager.setCurrentItem(page_num, false);
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				lastpositon =position;
		}

			@Override
			public void onPageSelected(int position) {
				boolean right =true;
				gPosition =position;

				if (orientation != lastOrientation){
					lastOrientation = orientation;
					return;
				}

				else if (lastpositon==position){
					lastpositon++;
					right=false;
				}

				else if(button && gRight){
					right=false;
					button =false;
				}

				if (right){
					if(mPagerAdpater.getItem(lastpositon-1)!=null){
						mPagerAdpater.getItem(lastpositon-1).onPause();
						mPagerAdpater.eraseItem(lastpositon-1);
					}


					if(mPagerAdpater.getItem(position+1)!=null)
						mPagerAdpater.getItem(position+1).onResume();
				}
				else{
					if(mPagerAdpater.getItem(lastpositon+1)!=null){
						mPagerAdpater.getItem(lastpositon+1).onPause();
						mPagerAdpater.eraseItem(lastpositon+1);
					}


					if(mPagerAdpater.getItem(position-1)!=null)
						mPagerAdpater.getItem(position-1).onResume();
				}

				mSeekBar.setProgress(position);
				mDownloader.setCurrentPosition(position);

				if(scrolled++ == 5){
					Runtime.getRuntime().gc();
					scrolled=0;
				}

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mTotalPagesText.setText(String.format(getString(R.string.info_total_pages), book.pageCount));
		mSeekBar.setKeyProgressIncrement(1);
		mSeekBar.setMax(book.pageCount - 1);
		mSeekBar.setProgress(page_num);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			int progress = 0;

			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				progress = i;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mPager.setCurrentItem(progress, false);
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		orientation =newConfig.orientation;
	}


	public PageDownloader getPageDownloader() {
		return mDownloader;
	}

	private class GalleryDownloaderListener implements PageDownloader.OnDownloadListener {

		@Override
		public void onFinish(int position, int progress) {
			if (mPagerAdpater != null) {
				mPagerAdpater.notifyPageImageLoaded(position, true);
				mSeekBar.setSecondaryProgress(position);
			}
		}

		@Override
		public void onError(int position, int errorCode) {
			if (mPagerAdpater != null) {
				mPagerAdpater.notifyPageImageLoaded(position, false);
			}
		}

		@Override
		public void onStateChange(int state, int progress) {

		}

	}


}
