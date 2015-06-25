package moe.feng.nhentai.ui.fragment.main;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

import java.util.ArrayList;

import moe.feng.nhentai.R;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.BookDetailsActivity;
import moe.feng.nhentai.ui.adapter.BookListRecyclerAdapter;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.ui.common.LazyFragment;

public class DownloadManagerFragment extends LazyFragment {

	private RecyclerView mRecyclerView;
	private BookListRecyclerAdapter mAdapter;

	public static final String TAG = DownloadManagerFragment.class.getSimpleName();

	@Override
	public int getLayoutResId() {
		return R.layout.fragment_home;
	}

	@Override
	public void finishCreateView(Bundle state) {
		mRecyclerView = $(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
		mRecyclerView.setHasFixedSize(false);

		ArrayList<Book> books = new ArrayList<>();

		mAdapter = new BookListRecyclerAdapter(mRecyclerView, books);
		mAdapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder viewHolder) {
				if (viewHolder instanceof BookListRecyclerAdapter.ViewHolder) {
					BookListRecyclerAdapter.ViewHolder holder = (BookListRecyclerAdapter.ViewHolder) viewHolder;
					Log.i(TAG, "You clicked position no." + position + " item, " +
							"its name is " + holder.mTitleTextView.getText().toString());
				}
			}
		});
		setRecyclerViewAdapter(mAdapter);
	}

	private void setRecyclerViewAdapter(BookListRecyclerAdapter adapter) {
		mRecyclerView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder viewHolder) {
				BookListRecyclerAdapter.ViewHolder holder = (BookListRecyclerAdapter.ViewHolder) viewHolder;
				Log.i(TAG, "You clicked position no." + position + " item, " +
						"its name is " + holder.mTitleTextView.getText().toString());
				BookDetailsActivity.launch(getActivity(), holder.mPreviewImageView, holder.book);
			}
		});
	}

}
