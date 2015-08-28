package moe.feng.nhentai.ui.fragment.main;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.PageApi;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.dao.FavoritesManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.BookDetailsActivity;
import moe.feng.nhentai.ui.MainActivity;
import moe.feng.nhentai.ui.adapter.BookListRecyclerAdapter;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.ui.common.LazyFragment;
import moe.feng.nhentai.util.AsyncTask;

public class DownloadManagerFragment extends LazyFragment {

	private RecyclerView mRecyclerView;
	private BookListRecyclerAdapter mAdapter;
	private StaggeredGridLayoutManager mLayoutManager;

	private SwipeRefreshLayout mSwipeRefreshLayout;

	private ArrayList<Book> mBooks;

	public static final String TAG = DownloadManagerFragment.class.getSimpleName();

	@Override
	public int getLayoutResId() {
		return R.layout.fragment_home;
	}

	@Override
	public void finishCreateView(Bundle state) {
		mSwipeRefreshLayout = $(R.id.swipe_refresh_layout);
		mRecyclerView = $(R.id.recycler_view);
		mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setHasFixedSize(true);

		mBooks = new ArrayList<>();
		mAdapter = new BookListRecyclerAdapter(mRecyclerView, mBooks, getFavoritesManager());
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

				new BooksGetTask().execute();
			}
		});

		mSwipeRefreshLayout.setRefreshing(true);
		new BooksGetTask().execute();
	}

	private void setRecyclerViewAdapter(BookListRecyclerAdapter adapter) {
		mRecyclerView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder viewHolder) {
				BookListRecyclerAdapter.ViewHolder holder = (BookListRecyclerAdapter.ViewHolder) viewHolder;
				Log.i(TAG, "You clicked position no." + position + " item, " +
						"its name is " + holder.mTitleTextView.getText().toString());
				BookDetailsActivity.launch(getActivity(), holder.mPreviewImageView, holder.book, position);
			}
		});
	}

	public void onDataUpdate() {
		mAdapter.notifyDataSetChanged();
	}


	private class BooksGetTask extends AsyncTask<Void, Void, ArrayList<Book>> {

		@Override
		protected ArrayList<Book> doInBackground(Void... params) {
			getFavoritesManager().reload();
			return FileCacheManager.getInstance(getApplicationContext()).getExternalBooks();
		}

		@Override
		protected void onPostExecute(ArrayList<Book> books) {
			mSwipeRefreshLayout.setRefreshing(false);
			mBooks = books;
			if (!mBooks.isEmpty()) {
				mAdapter = new BookListRecyclerAdapter(mRecyclerView, mBooks, getFavoritesManager());
				setRecyclerViewAdapter(mAdapter);
			}
		}

	}

	private FavoritesManager getFavoritesManager() {
		if (getActivity() != null && getActivity() instanceof MainActivity) {
			return ((MainActivity) getActivity()).getFavoritesManager();
		} else {
			return FavoritesManager.getInstance(getApplicationContext());
		}
	}

}
