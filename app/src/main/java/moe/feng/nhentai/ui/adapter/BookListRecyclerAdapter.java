package moe.feng.nhentai.ui.adapter;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialimageloading.MaterialImageLoading;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.BookApi;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.ColorGenerator;
import moe.feng.nhentai.util.TextDrawable;
import moe.feng.nhentai.util.Utility;

public class BookListRecyclerAdapter extends AbsRecyclerViewAdapter {

	private ArrayList<Book> data;

	private ColorGenerator mColorGenerator;

	public static final String TAG = BookListRecyclerAdapter.class.getSimpleName();

	public BookListRecyclerAdapter(RecyclerView recyclerView, ArrayList<Book> data) {
		super(recyclerView);
		this.data = data;
		mColorGenerator = ColorGenerator.MATERIAL;
	}

	@Override
	public ClickableViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		bindContext(viewGroup.getContext());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_book_card, viewGroup, false);
		return new ViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(ClickableViewHolder holder, final int position) {
		super.onBindViewHolder(holder, position);
		if (holder instanceof ViewHolder) {
			final ViewHolder mHolder = (ViewHolder) holder;
			mHolder.mTitleTextView.setText(data.get(position).title);
			String previewImageUrl = data.get(position).previewImageUrl;

			int color = mColorGenerator.getColor(data.get(position).title);
			TextDrawable drawable = TextDrawable.builder().buildRect(Utility.getFirstCharacter(data.get(position).title), color);
			mHolder.mPreviewImageView.setImageDrawable(drawable);
			mHolder.mImagePlaceholder = drawable;

			if (previewImageUrl != null) {
				ViewTreeObserver vto = mHolder.mPreviewImageView.getViewTreeObserver();
				vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						int thumbWidth = data.get(position).thumbWidth;
						int thumbHeight = data.get(position).thumbHeight;
						if (thumbWidth > 0 && thumbHeight > 0) {
							int width = mHolder.mPreviewImageView.getMeasuredWidth();
							int height = Math.round(width * ((float) thumbHeight / thumbWidth));
							mHolder.mPreviewImageView.getLayoutParams().height = height;
							mHolder.mPreviewImageView.setMinimumHeight(height);
						}
						mHolder.mPreviewImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

					}
				});
				new ImageDownloader().execute(mHolder.getParentView());
			}

			mHolder.book = data.get(position);
		}
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	private class ImageDownloader extends AsyncTask<Object, Object, Void> {

		@Override
		protected Void doInBackground(Object[] params) {
			View v = (View) params[0];
			ViewHolder h = (ViewHolder) v.getTag();
			Book book = h.book;

			if (v != null && !TextUtils.isEmpty(book.previewImageUrl)) {
				ImageView imgView = h.mPreviewImageView;

				File img = BookApi.getThumbFile(getContext(), book);

				if (img != null) {
					publishProgress(new Object[]{v, img, imgView, book});
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);

			View v = (View) values[0];

			if (!(v.getTag() instanceof ViewHolder) || (((ViewHolder) v.getTag()).book != null &&
					((ViewHolder) v.getTag()).book.bookId != ((Book) values[3]).bookId)) {
				return;
			}

			File img = (File) values[1];
			final ImageView iv = (ImageView) values[2];
			iv.setVisibility(View.VISIBLE);
			iv.setTag(false);

			Picasso.with(getContext())
					.load(img)
					.placeholder(((ViewHolder) v.getTag()).mImagePlaceholder)
					.into(iv, new Callback() {
						@Override
						public void onSuccess() {
							MaterialImageLoading.animate(iv).setDuration(1500).start();
						}

						@Override
						public void onError() {

						}
					});
		}


	}

	public class ViewHolder extends ClickableViewHolder {

		public ImageView mPreviewImageView;
		public TextView mTitleTextView;

		public Drawable mImagePlaceholder;

		public Book book;

		public ViewHolder(View itemView) {
			super(itemView);
			mPreviewImageView = (ImageView) itemView.findViewById(R.id.book_preview);
			mTitleTextView = (TextView) itemView.findViewById(R.id.book_title);

			itemView.setTag(this);
		}

	}

}
