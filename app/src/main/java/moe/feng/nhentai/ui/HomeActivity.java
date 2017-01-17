package moe.feng.nhentai.ui;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DimenRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import io.codetail.animation.ViewAnimationUtils;
import moe.feng.nhentai.R;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.dao.FavoriteCategoriesManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.ui.fragment.main.DownloadManagerFragment;
import moe.feng.nhentai.ui.fragment.main.FavoriteCategoryFragment;
import moe.feng.nhentai.ui.fragment.main.FavoriteFragment;
import moe.feng.nhentai.ui.fragment.main.HistoryFragment;
import moe.feng.nhentai.ui.fragment.main.HomeFragment;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.CrashHandler;
import moe.feng.nhentai.util.FilesUtil;
import moe.feng.nhentai.util.Settings;
import moe.feng.nhentai.util.Updates;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	// View states
	private static final int SECTION_LATEST = 0, SECTION_FAV_TAB = 1, SECTION_FOLLOWING_ARTISTS = 2;

	public static Context myContext;

	// Drawer
	private DrawerLayout mDrawerLayout;
	private NavigationView mNavigationView;
	private ActionBarDrawerToggle mDrawerToggle;

	// Views
	private FrameLayout mFragmentLayout;
	private Toolbar mToolbar;
	private ActionBar mActionBar;


	// Fragments
	private DownloadManagerFragment mFragmentDownload;
	private HomeFragment mFragmentHome;
	private FavoriteFragment mFragmentFavBooks;
	private HistoryFragment mFragmentHistory;
	private FavoriteCategoryFragment mFragmentFavCategory;

	private Settings mSets;

	private Handler mHandler = new Handler();
	public static final String TAG = HomeActivity.class.getSimpleName();
	public FileCacheManager mFileCacheManager;
	private static final int REQUEST_CODE_PERMISSION_GET = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSets = Settings.getInstance(getApplicationContext());
		CrashHandler.init(getApplicationContext());
		CrashHandler.register();
		myContext = getApplicationContext();
		/** Set up translucent status bar */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setStatusBarColor(Color.TRANSPARENT);
				getWindow().setNavigationBarColor(ContextCompat.getColor(this,R.color.deep_purple_800));
			}
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_home);

		initViews();

		getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background_material_light)));

		if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			if (mSets.getBoolean(Settings.KEY_NO_MEDIA, true)) {
				FilesUtil.createNewFile(FilesUtil.NOMEDIA_FILE);
			}
			onLoadMain();
            Updates.check(this);
		} else {
			new AlertDialog.Builder(this)
					.setTitle(R.string.dialog_permission_title)
					.setMessage(R.string.dialog_permission_msg)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							ActivityCompat.requestPermissions(HomeActivity.this,
									new String[]{
											Manifest.permission.WRITE_EXTERNAL_STORAGE,
											Manifest.permission.READ_EXTERNAL_STORAGE
									},
									REQUEST_CODE_PERMISSION_GET);
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							onLoadMain();
						}
					})
					.show();
		}

		mFileCacheManager = FileCacheManager.getInstance(getApplicationContext());
		if(!mFileCacheManager.checkUpdate()){
			new UpdateData().execute(1);
		}

	}

	private void onLoadMain() {
		mActionBar.setTitle(R.string.app_name);
		if (mFragmentHome == null) mFragmentHome = new HomeFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment_layout, mFragmentHome)
				.commit();
		mToolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFragmentHome.scrollToTop();
			}
		});
		mToolbar.setTranslationY(getResources().getDimension(R.dimen.widget_fade_in_translation_y_s));
		mToolbar.animate()
				.translationY(0f)
				.alpha(1.0f)
				.setDuration(0)
				.setStartDelay(0)
				.start();

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mSets.getInt(Settings.KEY_CELEBRATE, -1) != 2) {
					Snackbar.make(
							mDrawerLayout, R.string.celebrate_2016, Snackbar.LENGTH_INDEFINITE
					).setAction(R.string.snack_action_get_it, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							mSets.putInt(Settings.KEY_CELEBRATE, 2);
						}
					}).show();
				}
			}
		}, 1000);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume(){
		super.onResume();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == REQUEST_CODE_PERMISSION_GET) {
			if (grantResults.length > 0) {
				boolean b = true;
				for (int i : grantResults) {
					if (i == PackageManager.PERMISSION_DENIED) {
						showPermissionDeniedSnackbar();
						b = false;
						break;
					}
				}
				if (b && mSets.getBoolean(Settings.KEY_NO_MEDIA, true)) {
					FilesUtil.createNewFile(FilesUtil.NOMEDIA_FILE);
				}
			} else {
				showPermissionDeniedSnackbar();
			}
			onLoadMain();
		}
	}

	private void showPermissionDeniedSnackbar() {
		Snackbar.make(mFragmentLayout,
				R.string.snack_permission_failed,
				Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.snack_action_try_again, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ActivityCompat.requestPermissions(HomeActivity.this,
								new String[]{
										Manifest.permission.WRITE_EXTERNAL_STORAGE,
										Manifest.permission.READ_EXTERNAL_STORAGE
								},
								REQUEST_CODE_PERMISSION_GET);
					}
				})
				.show();
	}


	private void initViews() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			$(R.id.status_bar_header).setVisibility(View.VISIBLE);
		}

		mToolbar = $(R.id.toolbar);
		setSupportActionBar(mToolbar);
		mActionBar = getSupportActionBar();

		mDrawerLayout = $(R.id.drawer_layout);
		mNavigationView = $(R.id.navigation_view);
		mDrawerToggle = new MyDrawerToggle();
		mNavigationView.setNavigationItemSelectedListener(this);
		mNavigationView.setBackgroundResource(R.color.background_material_light);
		mDrawerLayout.addDrawerListener(new MyDrawerListener());
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});
		mDrawerLayout.addDrawerListener(mDrawerToggle);

		mFragmentLayout = $(R.id.fragment_layout);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		mDrawerLayout.closeDrawers();

		int id = item.getItemId();
		switch (id) {
			case R.id.navigation_item_home:

				mActionBar.setTitle(R.string.app_name);
				ViewCompat.setElevation(mToolbar, calcDimens(R.dimen.appbar_elevation));
				if (mFragmentHome == null) mFragmentHome = new HomeFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.fragment_layout, mFragmentHome)
						.commit();
				mToolbar.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
							mFragmentHome.scrollToTop();
					}
				});
				return true;

			case R.id.navigation_item_download:

				mActionBar.setTitle(R.string.item_download);
				ViewCompat.setElevation(mToolbar, calcDimens(R.dimen.appbar_elevation));
				if (mFragmentDownload == null) mFragmentDownload = new DownloadManagerFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.fragment_layout, mFragmentDownload)
						.commit();
				mToolbar.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
							mFragmentDownload.scrollToTop();
					}
				});
				return true;

			case R.id.navigation_item_fav_books:

				mActionBar.setTitle(R.string.item_favorite_books);
				ViewCompat.setElevation(mToolbar, calcDimens(R.dimen.appbar_elevation));
				if (mFragmentFavBooks == null) mFragmentFavBooks = new FavoriteFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.fragment_layout, mFragmentFavBooks)
						.commit();
				mToolbar.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
							mFragmentFavBooks.scrollToTop();
					}
				});
				return true;

			case R.id.navigation_item_recent:

				mActionBar.setTitle(R.string.item_recent);
				mFragmentLayout.setVisibility(View.VISIBLE);
				ViewCompat.setElevation(mToolbar, calcDimens(R.dimen.appbar_elevation));
				if (mFragmentHistory == null) mFragmentHistory = new HistoryFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.fragment_layout, mFragmentHistory)
						.commit();
				mToolbar.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
							mFragmentHistory.scrollToTop();
					}
				});
				return true;

			case R.id.navigation_item_fav_categories:

				FavoriteCategoriesManager.getInstance(getApplicationContext()).reload();
				mActionBar.setTitle(R.string.item_favorite_categories);
				mFragmentLayout.setVisibility(View.VISIBLE);
				ViewCompat.setElevation(mToolbar, calcDimens(R.dimen.appbar_elevation));
				if (mFragmentFavCategory == null)
					mFragmentFavCategory = new FavoriteCategoryFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.fragment_layout, mFragmentFavCategory)
						.commit();
				mToolbar.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
							mFragmentFavCategory.scrollToTop();
					}
				});
				return true;

			case R.id.navigation_item_settings:

				SettingsActivity.launchActivity(this, SettingsActivity.FLAG_MAIN);
				return true;

			case R.id.navigation_item_donate:

				new AlertDialog.Builder(this)
						.setTitle(R.string.dialog_donate_title)
						.setMessage(R.string.dialog_donate_message)
						.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

							}
						})
						.show();
				return true;
			case R.id.navigation_item_open_nhentai:
				Uri uri = Uri.parse("https://nhentai.net");
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
				return true;
		}

		return false;
	}

	private int calcDimens(@DimenRes int... dimenIds) {
		int result = 0;
		for (int dimenId : dimenIds) {
			result += getResources().getDimensionPixelSize(dimenId);
		}
		return result;
	}

	private <T extends View> T $(int id) {
		return (T) findViewById(id);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private class UpdateData extends AsyncTask<Integer, Void, BaseMessage> {
		@Override
		protected BaseMessage doInBackground(Integer... params) {
			mFileCacheManager.updateSaved(getApplicationContext());

			return null;
		}

		@Override
		protected void onPostExecute(BaseMessage msg) {
			Log.d(TAG, "Update Data Complete ");
		}

	}

	private class MyDrawerListener implements DrawerLayout.DrawerListener {

		@Override
		public void onDrawerOpened(View drawerView) {
			mDrawerToggle.onDrawerOpened(drawerView);
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			mDrawerToggle.onDrawerClosed(drawerView);
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			mDrawerToggle.onDrawerStateChanged(newState);
		}

	}

	private class MyDrawerToggle extends ActionBarDrawerToggle {

		public MyDrawerToggle() {
			super(HomeActivity.this, mDrawerLayout,
					mToolbar,
					R.string.abc_action_bar_home_description,
					R.string.abc_action_bar_home_description
			);
		}

		public MyDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
			super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			super.onDrawerClosed(drawerView);
			supportInvalidateOptionsMenu();
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			super.onDrawerOpened(drawerView);
			supportInvalidateOptionsMenu();
		}

	}

}
