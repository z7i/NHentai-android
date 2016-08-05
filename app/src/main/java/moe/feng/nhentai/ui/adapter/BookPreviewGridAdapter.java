package moe.feng.nhentai.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
	public void onViewRecycled(ClickableViewHolder holder) {
		super.onViewRecycled(holder);

		((ViewHolder) holder).mBookImageView.setImageBitmap(null);
		((ViewHolder) holder).mBookImageView.invalidate();
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

			holder.mBookImageView.setVisibility(View.INVISIBLE);
			holder.mNumberText.setText(Integer.toString(position + 1));

			new ImageDownloader().execute(holder.getParentView(), position + 1);
		}
	}

	@Override
	public int getItemCount() {
		return book.pageCount;
	}

	public class ViewHolder extends AbsRecyclerViewAdapter.ClickableViewHolder {

		ImageView mBookImageView;
		TextView mNumberText;

		public ViewHolder(View itemView) {
			super(itemView);
			this.mBookImageView = (ImageView) itemView.findViewById(R.id.image_view);
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
				ImageView imageView = h.mBookImageView;
				Bitmap img = BookApi.getPageThumb(getContext(), book, (int) params[1]);

				if (img != null) {
					publishProgress(new Object[]{v, img, imageView, book, params[1]});

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

			Bitmap img = (Bitmap) values[1];
			ImageView imageView = (ImageView) values [2];
			imageView.setVisibility(View.VISIBLE);
			imageView.setTag(false);
			imageView.setImageBitmap(img);
			imageView.invalidate();
		}


	}

}
