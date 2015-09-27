package moe.feng.nhentai.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;
import moe.feng.nhentai.R;
import moe.feng.nhentai.api.PageApi;
import moe.feng.nhentai.dao.FavoritesManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.adapter.BookListRecyclerAdapter;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.Settings;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	// List
	private RecyclerView mRecyclerView;
	private BookListRecyclerAdapter mAdapter;
	private StaggeredGridLayoutManager mLayoutManager;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ArrayList<Book> mBooks;
	private int mNowPage = 1;
	private boolean isFirstLoad = true;

	// Search Bar
	private RevealFrameLayout mSearchBar;
	private CardView mSearchBarCard;
	private ImageView mSearchBarOtherBtn;

	// Title Bar
	private LinearLayout mTitleBarLayout;
	private AppCompatTextView mTitleMain, mTitleSub;

	// Splash Screen
	private ImageView mSplashIvLogo;
	private AppCompatTextView mSplashTvLogo;
	private FrameLayout mSplashLayout;

	// Drawer
	private DrawerLayout mDrawerLayout;
	private NavigationView mNavigationView;

	private View mBackgroundView;
	private FrameLayout mParentLayout, mMainLayout;
	private Toolbar mToolbar;
	private ActionBar mActionBar;

	private FavoritesManager mFM;

	private Settings mSets;

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSets = Settings.getInstance(getApplicationContext());

		/** Set up translucent status bar */
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.deep_purple_800));
		}

		super.onCreate(savedInstanceState);

		mFM = FavoritesManager.getInstance(getApplicationContext());

		setContentView(R.layout.activity_new_home);
		initViews();

		getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background_material_light)));

		new PageGetTask().execute(mNowPage);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startSplashFinishToMain();
			}
		}, 1000);
	}

	private void startSplashFinishToMain() {
		mSplashIvLogo.animate()
				.translationY(-getResources().getDimension(R.dimen.logo_fade_out_translation_y))
				.alpha(0.0f)
				.setDuration(300)
				.start();
		mSplashTvLogo.animate()
				.alpha(0.0f)
				.setDuration(300)
				.start();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mSplashLayout.setVisibility(View.GONE);

				mBackgroundView.animate()
						.translationY(-(mParentLayout.getHeight() - getResources().getDimension(R.dimen.background_min_height)))
						.setDuration(700)
						.setInterpolator(new OvershootInterpolator(0.6f))
						.start();
				mMainLayout.setVisibility(View.VISIBLE);

				mToolbar.setTranslationY(getResources().getDimension(R.dimen.widget_fade_in_translation_y_s));
				mToolbar.animate()
						.translationY(0f)
						.alpha(1.0f)
						.setDuration(200)
						.start();

				mSearchBar.setTranslationY(getResources().getDimension(R.dimen.widget_fade_in_translation_y_s));
				mSearchBar.animate()
						.translationY(0f).alpha(1.0f)
						.setDuration(200)
						.setStartDelay(100)
						.start();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						int cx = (mSearchBarCard.getLeft() + mSearchBarCard.getRight()) / 2;
						int cy = (mSearchBarCard.getTop() + mSearchBarCard.getBottom()) / 2;
						revealFrom(cx, cy, mSearchBarCard);
						if (!isFirstLoad) {
							mRecyclerView.setVisibility(View.VISIBLE);
							mRecyclerView.setAlpha(0.75f);
							mRecyclerView.setTranslationY(getResources().getDimension(R.dimen.logo_fade_out_translation_y));
							mRecyclerView.animate()
									.translationY(0f)
									.alpha(1.0f)
									.setDuration(150)
									.setStartDelay(200)
									.start();
						}
					}
				}, 100);

				mTitleBarLayout.setTranslationY(getResources().getDimension(R.dimen.widget_fade_in_translation_y_m));
				mTitleBarLayout.animate()
						.translationY(0f)
						.alpha(1.0f)
						.setDuration(150)
						.setStartDelay(200)
						.start();
			}
		}, 300);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mFM.save();
	}

	private void initViews() {
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			$(R.id.status_bar_header).setVisibility(View.VISIBLE);
		}

		mToolbar = $(R.id.toolbar);
		setSupportActionBar(mToolbar);
		mActionBar = getSupportActionBar();

		mDrawerLayout = $(R.id.drawer_layout);
		mNavigationView = $(R.id.navigation_view);
		mNavigationView.setNavigationItemSelectedListener(this);
		mNavigationView.setBackgroundResource(R.color.background_material_light);

		mBackgroundView = $(R.id.bg_view);
		mParentLayout = $(R.id.parent_layout);
		mMainLayout = $(R.id.main_layout);
		mSplashIvLogo = $(R.id.iv_nh_logo);
		mSplashTvLogo = $(R.id.tv_nh_logo);
		mSplashLayout = $(R.id.splash_layout);
		mSearchBar = $(R.id.search_bar);
		mSearchBarCard = $(R.id.card_view);
		mSearchBarOtherBtn = $(R.id.btn_search_bar_other);
		mTitleBarLayout = $(R.id.title_bar_layout);
		mTitleMain = $(R.id.tv_title_main);
		mTitleSub = $(R.id.tv_title_sub);
		mSwipeRefreshLayout = $(R.id.swipe_refresh_layout);
		mRecyclerView = $(R.id.recycler_view);
		mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setHasFixedSize(true);

		mBooks = new ArrayList<>();
		mAdapter = new BookListRecyclerAdapter(mRecyclerView, mBooks, mFM);
		setRecyclerAdapter(mAdapter);

		mSwipeRefreshLayout.setColorSchemeResources(
				R.color.deep_purple_500, R.color.pink_500, R.color.orange_500, R.color.brown_500,
				R.color.indigo_500, R.color.blue_500, R.color.teal_500, R.color.green_500
		);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (!mSwipeRefreshLayout.isRefreshing()) {
					mSwipeRefreshLayout.setRefreshing(true);
				}

				mBooks = new ArrayList<>();
				mAdapter = new BookListRecyclerAdapter(mRecyclerView, mBooks, mFM);
				setRecyclerAdapter(mAdapter);
				new PageGetTask().execute(mNowPage = 1);
			}
		});

		mSearchBarCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO Start search activity
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == BookDetailsActivity.REQUEST_MAIN) {
			if (resultCode == BookDetailsActivity.RESULT_HAVE_FAV) {
				mAdapter.notifyItemChanged(intent.getIntExtra("position", 0));
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void setRecyclerAdapter(BookListRecyclerAdapter adapter) {
		adapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder viewHolder) {
				BookListRecyclerAdapter.ViewHolder holder = (BookListRecyclerAdapter.ViewHolder) viewHolder;
				BookDetailsActivity.launch(HomeActivity.this, holder.mPreviewImageView, holder.book, position);
			}
		});
		adapter.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView rv, int dx, int dy) {
				if (!mSwipeRefreshLayout.isRefreshing() && mLayoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] >= mAdapter.getItemCount() - 2) {
					mSwipeRefreshLayout.setRefreshing(true);
					new PageGetTask().execute(++mNowPage);
				}
			}
		});

		mRecyclerView.setAdapter(adapter);
	}

	private void revealFrom(int cx, int cy, View root) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, r.getDisplayMetrics());

		int finalRadius = (int) Math.max(root.getWidth(), px);

		SupportAnimator animator = ViewAnimationUtils.createCircularReveal(root, cx, cy, 0, finalRadius);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(200);
		animator.start();
	}

	private <T extends View> T $(int id) {
		return (T) findViewById(id);
	}

	private class PageGetTask extends AsyncTask<Integer, Void, BaseMessage> {

		@Override
		protected BaseMessage doInBackground(Integer... params) {
			mFM.reload();
			return PageApi.getHomePageList(params[0]);
		}

		@Override
		protected void onPostExecute(BaseMessage msg) {
			mSwipeRefreshLayout.setRefreshing(false);
			if (msg != null) {
				if (msg.getCode() == 0 && msg.getData() != null) {
					if (!((ArrayList<Book>) msg.getData()).isEmpty()) {
						mBooks.addAll((ArrayList<Book>) msg.getData());
						mAdapter.notifyDataSetChanged();
						if (!isFirstLoad) {
							mRecyclerView.setVisibility(View.VISIBLE);
						}
						isFirstLoad = false;
					}
				} else if (mNowPage == 1) {
					Snackbar.make(
							mRecyclerView,
							R.string.tips_network_error,
							Snackbar.LENGTH_LONG
					).setAction(
							R.string.snack_action_try_again,
							new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									mSwipeRefreshLayout.setRefreshing(true);
									new PageGetTask().execute(mNowPage);
								}
							}
					).show();
				}
			}
		}

	}

}
