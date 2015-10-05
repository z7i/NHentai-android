package moe.feng.nhentai.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.ArrayList;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.PageApi;
import moe.feng.nhentai.dao.FavoritesManager;
import moe.feng.nhentai.dao.SearchHistoryManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.adapter.BookListRecyclerAdapter;
import moe.feng.nhentai.ui.adapter.HistoryRecyclerAdapter;
import moe.feng.nhentai.ui.common.AbsActivity;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.util.AsyncTask;

public class SearchActivity extends AbsActivity {

	private CardView mTopBar;
	private AppCompatEditText mEditText;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	private RecyclerView mHistoryList, mResultList;
	private BookListRecyclerAdapter mAdapter;
	private HistoryRecyclerAdapter mHistoryAdapter;
	private StaggeredGridLayoutManager mLayoutManager;
	private LinearLayoutManager mHistoryLayoutManager;

	private ArrayList<Book> mBooks;
	private int mNowPage = 1;
	private String keyword;

	private FavoritesManager mFM;
	private SearchHistoryManager mHM;

	private InputMethodManager imm;

	private static final String TRANSITION_NAME_CARD = "card_view";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFM = FavoritesManager.getInstance(getApplicationContext());
		mHM = SearchHistoryManager.getInstance(getApplicationContext(), "all");
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		setContentView(R.layout.activity_search);

		mActionBar.setDisplayHomeAsUpEnabled(true);

		ViewCompat.setTransitionName(mTopBar, TRANSITION_NAME_CARD);

		startKeywordEnter();
	}

	@Override
	protected void setUpViews() {
		mTopBar = $(R.id.top_card_layout);
		mEditText = new AppCompatEditText(this);
		mResultList = $(R.id.rv_result);
		mHistoryList = $(R.id.rv_history);
		mSwipeRefreshLayout = $(R.id.swipe_refresh_layout);

		/** Init History List */
		mHistoryLayoutManager = new LinearLayoutManager(this);
		mHistoryList.setLayoutManager(mHistoryLayoutManager);
		mHistoryList.setHasFixedSize(true);

		mHistoryAdapter = new HistoryRecyclerAdapter(mHistoryList, mHM);
		mHistoryAdapter.setAddTextListener(new HistoryRecyclerAdapter.AddTextListener() {
			@Override
			public void onTextAdd(String string) {
				mEditText.setText(string);
				mEditText.setSelection(string.length());
			}
		});
		setHistoryListAdapter(mHistoryAdapter);

		/** Init Result List */
		mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		mResultList.setLayoutManager(mLayoutManager);
		mResultList.setHasFixedSize(true);

		mBooks = new ArrayList<>();
		mAdapter = new BookListRecyclerAdapter(mResultList, mBooks, mFM);
		setResultListAdapter(mAdapter);

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
				mAdapter = new BookListRecyclerAdapter(mResultList, mBooks, mFM);
				setResultListAdapter(mAdapter);
				new PageGetTask().execute(mNowPage = 1);
			}
		});

		mEditText.setTextAppearance(this, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
		mEditText.setSingleLine(true);
		mEditText.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mEditText.setHint(R.string.search_text_hint);
		mEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == EditorInfo.IME_ACTION_SEARCH) {
					if (mEditText.getText() == null) {
						Snackbar.make(
								$(R.id.root_layout),
								R.string.tips_search_text_empty,
								Snackbar.LENGTH_SHORT
						).show();
						return false;
					}
					if (TextUtils.isEmpty(mEditText.getText().toString())) {
						Snackbar.make(
								$(R.id.root_layout),
								R.string.tips_search_text_empty,
								Snackbar.LENGTH_SHORT
						).show();
						return false;
					}
					startSearchTask(mEditText.getText().toString());
					return true;
				}
				return false;
			}
		});

		/** Set up custom view on ActionBar */
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mEditText.setLayoutParams(lp);

		ActionBar.LayoutParams lp2 = new ActionBar.LayoutParams(lp);
		mActionBar.setCustomView(mEditText, lp2);

		mToolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mHistoryList.getVisibility() != View.VISIBLE) {
					startKeywordEnter();
				}
			}
		});
	}

	private void setResultListAdapter(BookListRecyclerAdapter adapter) {
		adapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder viewHolder) {
				BookListRecyclerAdapter.ViewHolder holder = (BookListRecyclerAdapter.ViewHolder) viewHolder;
				BookDetailsActivity.launch(SearchActivity.this, holder.mPreviewImageView, holder.book, position);
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

		mResultList.setAdapter(adapter);
	}

	private void setHistoryListAdapter(HistoryRecyclerAdapter adapter) {
		adapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder viewHolder) {
				startSearchTask(mHM.get(position));
			}
		});

		mHistoryList.setAdapter(adapter);
	}

	private void startSearchTask(String k){
		this.keyword = k;

		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		mActionBar.setTitle(keyword);
		mActionBar.setDisplayShowCustomEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(true);

		mHM.add(keyword);
		mHistoryAdapter.notifyDataSetChanged();

		mHistoryList.setVisibility(View.GONE);
		mSwipeRefreshLayout.setVisibility(View.VISIBLE);
		mResultList.setVisibility(View.VISIBLE);

		mSwipeRefreshLayout.setRefreshing(true);
		mBooks = new ArrayList<>();
		mAdapter = new BookListRecyclerAdapter(mResultList, mBooks, mFM);
		setResultListAdapter(mAdapter);
		new PageGetTask().execute(mNowPage = 1);

		invalidateOptionsMenu();
	}

	private void startKeywordEnter() {
		mEditText.setText("");

		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);

		imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);

		mHM.reloadDatabase();
		mHistoryAdapter.notifyDataSetChanged();

		mHistoryList.setVisibility(View.VISIBLE);
		mSwipeRefreshLayout.setVisibility(View.GONE);
		mResultList.setVisibility(View.GONE);

		invalidateOptionsMenu();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.menu_search, menu);

		boolean state = mResultList.getVisibility() == View.VISIBLE;
		menu.findItem(R.id.action_clear).setVisible(!state);
		menu.findItem(R.id.action_load_next_page).setVisible(state);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_clear) {
			mEditText.setText("");
			return true;
		} else if (id == R.id.action_load_next_page) {
			mSwipeRefreshLayout.setRefreshing(true);
			new PageGetTask().execute(++mNowPage);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private class PageGetTask extends AsyncTask<Integer, Void, BaseMessage> {

		@Override
		protected BaseMessage doInBackground(Integer... params) {
			mFM.reload();
			return PageApi.getSearchPageList(keyword, params[0]);
		}

		@Override
		protected void onPostExecute(BaseMessage msg) {
			mSwipeRefreshLayout.setRefreshing(false);
			if (msg != null) {
				if (msg.getCode() == 0 && msg.getData() != null) {
					if (!((ArrayList<Book>) msg.getData()).isEmpty()) {
						mBooks.addAll((ArrayList<Book>) msg.getData());
						mAdapter.notifyDataSetChanged();
						if (mNowPage == 1) {
							mResultList.setAdapter(mAdapter);
						}
					} else {
						Snackbar.make($(R.id.root_layout), R.string.tips_no_result, Snackbar.LENGTH_LONG).show();
					}
				} else if (mNowPage == 1) {
					Snackbar.make($(R.id.root_layout), R.string.tips_no_result, Snackbar.LENGTH_LONG).show();
				}
			}
		}

	}

	public static void launch(AppCompatActivity activity, View sharedCardView) {
		ActivityOptionsCompat options = ActivityOptionsCompat
				.makeSceneTransitionAnimation(activity, sharedCardView, TRANSITION_NAME_CARD);
		Intent intent = new Intent(activity, SearchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		activity.startActivity(intent, options.toBundle());
	}

}
