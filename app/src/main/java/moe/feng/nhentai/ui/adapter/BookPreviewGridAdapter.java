package moe.feng.nhentai.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.BookApi;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.util.AsyncTask;

public class BookPreviewGridAdapter extends AbsRecyclerViewAdapter {

	private Book book;

	public BookPreviewGridAdapter(RecyclerView rv, Book book) {
		super(rv);
		this.book = book;
	}

	@Override
	public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		bindContext(parent.getContext());
		return new ViewHolder(
				LayoutInflater
						.from(getContext())
						.inflate(R.layout.list_item_book_picture_thumb, parent, false)
		);
	}

	@Override
	public void onBindViewHolder(ClickableViewHolder cvh, int position) {
		super.onBindViewHolder(cvh, position);
		if (cvh instanceof ViewHolder) {
			ViewHolder holder = (ViewHolder) cvh;

			holder.mImageView.setVisibility(View.INVISIBLE);
			holder.mNumberText.setText(Integer.toString(position + 1));

			new ImageDownloader().execute(holder.getParentView(), position + 1);
		}
	}

	@Override
	public int getItemCount() {
		return book.pageCount;
	}

	public class ViewHolder extends AbsRecyclerViewAdapter.ClickableViewHolder {

		ImageView mImageView;
		TextView mNumberText;

		public ViewHolder(View itemView) {
			super(itemView);
			this.mImageView = (ImageView) itemView.findViewById(R.id.image_view);
			this.mNumberText = (TextView) itemView.findViewById(R.id.number_text);

			itemView.setTag(this);
		}

	}

	private class ImageDownloader extends AsyncTask<Object, Object, Void> {

		@Override
		protected Void doInBackground(Object[] params) {
			View v = (View) params[0];
			ViewHolder h = (ViewHolder) v.getTag();

			if (v != null) {
				ImageView imgView = h.mImageView;

				File img = BookApi.getPageThumbFile(getContext(), book, (int) params[1]);

				if (img != null) {
					publishProgress(new Object[]{v, img, imgView, book, params[1]});
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);

			View v = (View) values[0];

			if (!(v.getTag() instanceof ViewHolder)) {
				return;
			}

			ViewHolder vh = (ViewHolder) v.getTag();

			if (!vh.mNumberText.getText().equals(Integer.toString((int) values[4]))) {
				return;
			}

			File img = (File) values[1];
			final ImageView iv = (ImageView) values[2];
			iv.setVisibility(View.VISIBLE);
			iv.setTag(false);

			Picasso.with(getContext())
					.load(img)
					.into(iv, new Callback() {
						@Override
						public void onSuccess() {
						}

						@Override
						public void onError() {

						}
					});
		}


	}

}
