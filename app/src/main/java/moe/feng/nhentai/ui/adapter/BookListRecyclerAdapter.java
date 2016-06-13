package moe.feng.nhentai.ui.adapter;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialimageloading.MaterialImageLoading;
import com.lid.lib.LabelView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.BookApi;
import moe.feng.nhentai.dao.FavoritesManager;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.ColorGenerator;
import moe.feng.nhentai.util.Settings;
import moe.feng.nhentai.util.TextDrawable;
import moe.feng.nhentai.util.Utility;

public class BookListRecyclerAdapter extends AbsRecyclerViewAdapter {

	private ArrayList<Book> data;
	private FavoritesManager fm;
	private Settings sets;

	private ColorGenerator mColorGenerator;

	public static final String TAG = BookListRecyclerAdapter.class.getSimpleName();

	public BookListRecyclerAdapter(RecyclerView recyclerView, FavoritesManager fm, Settings sets) {
		this(recyclerView, null, fm, sets);
	}

	public BookListRecyclerAdapter(RecyclerView recyclerView, ArrayList<Book> data, FavoritesManager fm, Settings sets) {
		super(recyclerView);
		this.data = data;
		this.fm = fm;
		this.sets = sets;
		mColorGenerator = ColorGenerator.MATERIAL;
	}

	@Override
	public void onViewRecycled(ClickableViewHolder holder) {
		super.onViewRecycled(holder);
		((ViewHolder) holder).labelView.remove();
	}

	@Override
	public ClickableViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		bindContext(viewGroup.getContext());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_book_card, viewGroup, false);
		return new ViewHolder(view, new LabelView(getContext()));
	}
	
	@Override
	public void onBindViewHolder(ClickableViewHolder holder, final int position) {
		super.onBindViewHolder(holder, position);
		if (holder instanceof ViewHolder) {
			ArrayList<Book> data = this.data == null ? fm.toArray() : this.data;
			final ViewHolder mHolder = (ViewHolder) holder;
			mHolder.mTitleTextView.setText("       " + data.get(position).title);
			String previewImageUrl = data.get(position).previewImageUrl;

            switch (data.get(position).langField) {
                case Book.LANG_GB:
                    mHolder.mLangFieldView.setImageResource(R.drawable.ic_lang_gb);
                    break;
                case Book.LANG_CN:
                    mHolder.mLangFieldView.setImageResource(R.drawable.ic_lang_cn);
                    break;
                case Book.LANG_JP:
                default:
                    mHolder.mLangFieldView.setImageResource(R.drawable.ic_lang_jp);
                    break;
            }

			int color = mColorGenerator.getColor(data.get(position).title);
			TextDrawable drawable = TextDrawable.builder().buildRect(Utility.getFirstCharacter(data.get(position).title), color);
			mHolder.mPreviewImageView.setImageDrawable(drawable);
			mHolder.mImagePlaceholder = drawable;

			if (previewImageUrl != null) {
				ViewTreeObserver vto = mHolder.mPreviewImageView.getViewTreeObserver();
				vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						ArrayList<Book> data = BookListRecyclerAdapter.this.data == null ?
								fm.toArray() : BookListRecyclerAdapter.this.data;
						if (data.size() < position + 1) return;
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

			if (fm.contains(mHolder.book.bookId)) {
				Log.i(TAG, "Find favorite: " + mHolder.book.bookId);
				mHolder.labelView.setText(R.string.label_added_to_favorite);
				mHolder.labelView.setBackgroundResource(R.color.blue_500);
				mHolder.labelView.setTargetView(mHolder.mPreviewImageView, 10, LabelView.Gravity.RIGHT_TOP);
			}
		}
	}

	@Override
	public int getItemCount() {
		ArrayList<Book> data = this.data == null ? fm.toArray() : this.data;
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

				boolean useHdImage = sets != null && sets.getBoolean(Settings.KEY_LIST_HD_IMAGE, false);
				File img = useHdImage ? BookApi.getCoverFile(getContext(), book) : BookApi.getThumbFile(getContext(), book);

				if (img != null) {
					publishProgress(new Object[]{v, img, imgView, book});
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(final Object[] values) {
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

		public ImageView mPreviewImageView, mLangFieldView;
		public TextView mTitleTextView;

		Drawable mImagePlaceholder;

		public Book book;

		LabelView labelView;

		public ViewHolder(View itemView, LabelView labelView) {
			super(itemView);
			mPreviewImageView = (ImageView) itemView.findViewById(R.id.book_preview);
            mLangFieldView = (ImageView) itemView.findViewById(R.id.book_lang_field);
			mTitleTextView = (TextView) itemView.findViewById(R.id.book_title);

			this.labelView = labelView;

			itemView.setTag(this);
		}

	}

}
