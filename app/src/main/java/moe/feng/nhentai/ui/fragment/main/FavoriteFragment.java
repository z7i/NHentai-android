package moe.feng.nhentai.ui.fragment.main;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import moe.feng.nhentai.R;
import moe.feng.nhentai.dao.FavoritesManager;
import moe.feng.nhentai.ui.BookDetailsActivity;
import moe.feng.nhentai.ui.HomeActivity;
import moe.feng.nhentai.ui.adapter.BookListRecyclerAdapter;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.ui.common.LazyFragment;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.Settings;
import moe.feng.nhentai.util.Utility;

public class FavoriteFragment extends LazyFragment {

	private RecyclerView mRecyclerView;
	private BookListRecyclerAdapter mAdapter;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	public static final String TAG = FavoriteFragment.class.getSimpleName();

	@Override
	public int getLayoutResId() {
		return R.layout.fragment_home_recycler;
	}

	@Override
	public void finishCreateView(Bundle state) {
		mSwipeRefreshLayout = $(R.id.swipe_refresh_layout);
		mRecyclerView = $(R.id.recycler_view);

		int mHorCardCount;
		if ((mHorCardCount = Settings.getInstance(getApplicationContext()).getInt(Settings.KEY_CARDS_COUNT, -1)) < 1) {
			mHorCardCount = Utility.getHorizontalCardCountInScreen(getActivity());
		}

		mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(mHorCardCount, StaggeredGridLayoutManager.VERTICAL));
		mRecyclerView.setHasFixedSize(false);

		mAdapter = new BookListRecyclerAdapter(mRecyclerView, getFavoritesManager(), mSets);
		setRecyclerViewAdapter(mAdapter);

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
				new FavoritesRefreshTask().execute();
			}
		});
	}

	private void setRecyclerViewAdapter(BookListRecyclerAdapter adapter) {
		mRecyclerView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder viewHolder) {
				BookListRecyclerAdapter.ViewHolder holder = (BookListRecyclerAdapter.ViewHolder) viewHolder;
				BookDetailsActivity.launch(getActivity(), holder.mPreviewImageView, holder.book, position);
			}
		});
	}

	public void scrollToTop() {
		if (mAdapter.getItemCount() > 0) {
			mRecyclerView.smoothScrollToPosition(0);
		}
	}

	private class FavoritesRefreshTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			getFavoritesManager().reload(getApplicationContext());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mSwipeRefreshLayout.setRefreshing(false);
			mAdapter.notifyDataSetChanged();
		}

	}

	private FavoritesManager getFavoritesManager() {
		if (getActivity() != null && getActivity() instanceof HomeActivity) {
			return ((HomeActivity) getActivity()).getFavoritesManager();
		} else {
			return FavoritesManager.getInstance(getApplicationContext());
		}
	}

}
