package moe.feng.nhentai.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.BookApi;
import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.cache.common.Constants;
import moe.feng.nhentai.cache.file.FileCacheManager;
import moe.feng.nhentai.dao.FavoritesManager;
import moe.feng.nhentai.model.BaseMessage;
import moe.feng.nhentai.model.Book;
import moe.feng.nhentai.model.Category;
import moe.feng.nhentai.ui.adapter.BookPreviewGridAdapter;
import moe.feng.nhentai.ui.common.AbsActivity;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.util.AsyncTask;
import moe.feng.nhentai.util.ColorGenerator;
import moe.feng.nhentai.util.TextDrawable;
import moe.feng.nhentai.view.AutoWrapLayout;
import moe.feng.nhentai.view.WheelProgressView;

public class BookDetailsActivity extends AbsActivity {

	private ImageView mImageView;
	private CollapsingToolbarLayout collapsingToolbar;
	private FloatingActionButton mFAB;
	private TextView mTitleText;
	private LinearLayout mTagsLayout;
	private LinearLayout mContentView;
	private WheelProgressView mProgressWheel;
	private RecyclerView mPreviewList;

	private ShareActionProvider mShareActionProvider;

	private Book book;
	private int fromPosition;

	private boolean isFavorite = false, originFavorite = false, isFromExternal = false;

	private final static String EXTRA_BOOK_DATA = "book_data", EXTRA_POSITION = "item_position";
	private final static String TRANSITION_NAME_IMAGE = "BookDetailsActivity:image";

	public final static int REQUEST_MAIN = 1001, RESULT_HAVE_FAV = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_details);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		collapsingToolbar = $(R.id.collapsing_toolbar);

		Intent intent = getIntent();
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			book = new Book();
			String url = intent.getData().toString();
			if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
			String bid = url.substring(url.lastIndexOf("/") + 1, url.length());
			Log.i("TAG", "url get id:" + bid);
			book.bookId = bid;
			fromPosition = -1;
			isFromExternal = true;
		} else {
			book = new Gson().fromJson(intent.getStringExtra(EXTRA_BOOK_DATA), Book.class);
			fromPosition = intent.getIntExtra(EXTRA_POSITION, 0);

			collapsingToolbar.setTitle(book.title);
		}

		isFavorite = originFavorite = FavoritesManager.getInstance(getApplicationContext()).contains(book.bookId);

		mImageView = $(R.id.app_bar_background);
		ViewCompat.setTransitionName(mImageView, TRANSITION_NAME_IMAGE);

		mFAB = $(R.id.fab);
		mTitleText = $(R.id.tv_title);
		mTagsLayout = $(R.id.book_tags_layout);
		mContentView = $(R.id.book_content);
		mProgressWheel = $(R.id.wheel_progress);
		mPreviewList = $(R.id.preview_list);

		mPreviewList.setHasFixedSize(true);
		mPreviewList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2, LinearLayoutManager.HORIZONTAL, false));

		FileCacheManager cm = FileCacheManager.getInstance(getApplicationContext());
		if (book.galleryId != null) {
			if (cm.cacheExistsUrl(Constants.CACHE_COVER, book.bigCoverImageUrl)) {
				if (cm.cacheExistsUrl(Constants.CACHE_PAGE_IMG,
						NHentaiUrl.getOriginPictureUrl(book.galleryId, "1"))) {
					Picasso.with(getApplicationContext())
							.load(cm.getBitmapUrlFile(Constants.CACHE_PAGE_IMG, NHentaiUrl.getOriginPictureUrl(book.galleryId, "1")))
							.fit()
							.centerCrop()
							.into(mImageView);
				} else {
					Picasso.with(getApplicationContext())
							.load(cm.getBitmapUrlFile(Constants.CACHE_COVER, book.bigCoverImageUrl))
							.fit()
							.centerCrop()
							.into(mImageView);
				}
			} else {
				if (cm.cacheExistsUrl(Constants.CACHE_THUMB, book.previewImageUrl)) {
					Picasso.with(getApplicationContext())
							.load(cm.getBitmapUrlFile(Constants.CACHE_THUMB, book.previewImageUrl))
							.fit()
							.centerCrop()
							.into(mImageView);
				} else {
					int color = ColorGenerator.MATERIAL.getColor(book.title);
					TextDrawable drawable = TextDrawable.builder().buildRect(book.title.substring(0, 1), color);
					mImageView.setImageDrawable(drawable);
				}
				new CoverTask().execute(book);
			}
		}

		if (book.pageCount != 0) {
			mContentView.setVisibility(View.GONE);
			mProgressWheel.setVisibility(View.VISIBLE);
			mProgressWheel.spin();
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							updateUIContent();
						}
					});
				}
			}.start();
		} else {
			startBookGet();
		}
	}

	@Override
	protected void setUpViews() {

	}

	public static void launch(Activity activity, ImageView imageView, Book book, int fromPosition) {
		ActivityOptionsCompat options = ActivityOptionsCompat
				.makeSceneTransitionAnimation(activity, imageView, TRANSITION_NAME_IMAGE);
		Intent intent = new Intent(activity, BookDetailsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.putExtra(EXTRA_BOOK_DATA, book.toJSONString());
		intent.putExtra(EXTRA_POSITION, fromPosition);
		ActivityCompat.startActivityForResult(activity, intent, REQUEST_MAIN, options.toBundle());
	}

	private void updateUIContent() {
		collapsingToolbar.setTitle(book.title);
		collapsingToolbar.invalidate();
		$(R.id.toolbar).invalidate();
		$(R.id.appbar).invalidate();

		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				GalleryActivity.launch(BookDetailsActivity.this, book, 0);
			}
		});

		updateDetailsContent();
	}

	private void updateDetailsContent() {
		mProgressWheel.setVisibility(View.GONE);
		mContentView.setVisibility(View.VISIBLE);
		mContentView.animate().alphaBy(0f).alpha(1f).setDuration(1500).start();
		mTitleText.setText(TextUtils.isEmpty(book.titleJP) ? book.title : book.titleJP);
		if (isFromExternal) {
			new CoverTask().execute(book);
		}

		setUpShareAction();
		updatePreviewList();
		updateTagsContent();
	}

	private void updatePreviewList() {
		BookPreviewGridAdapter adapter = new BookPreviewGridAdapter(mPreviewList, book);
		adapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder holder) {
				GalleryActivity.launch(BookDetailsActivity.this, book, position);
			}
		});
		mPreviewList.setAdapter(adapter);
	}

	private void updateTagsContent() {
		int x = getResources().getDimensionPixelSize(R.dimen.tag_margin_x);
		int y = getResources().getDimensionPixelSize(R.dimen.tag_margin_y);
		int min_width = getResources().getDimensionPixelSize(R.dimen.tag_title_width);

		// Add Parodies Tags
		if (!TextUtils.isEmpty(book.parodies)) {
			LinearLayout tagGroupLayout = new LinearLayout(this);
			tagGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
			AutoWrapLayout tagLayout = new AutoWrapLayout(this);

			TextView groupNameView = new TextView(this);
			groupNameView.setMinWidth(min_width);
			groupNameView.setText(R.string.tag_type_parodies);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(x, y, x, y);
			lp.width = min_width;
			tagGroupLayout.addView(groupNameView, lp);

			TextView tagView = (TextView) View.inflate(getApplicationContext(), R.layout.layout_tag, null);
			tagView.setText(book.parodies);
			tagView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CategoryActivity.launch(
							BookDetailsActivity.this,
							new Category(Category.Type.PARODY, book.parodies)
					);
				}
			});
			AutoWrapLayout.LayoutParams alp = new AutoWrapLayout.LayoutParams();
			alp.setMargins(x, y, x, y);
			tagLayout.addView(tagView, alp);
			tagGroupLayout.addView(tagLayout);
			mTagsLayout.addView(tagGroupLayout);
		}

		// Add Characters
		if (!book.characters.isEmpty()) {
			LinearLayout tagGroupLayout = new LinearLayout(this);
			tagGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
			AutoWrapLayout tagLayout = new AutoWrapLayout(this);

			TextView groupNameView = new TextView(this);
			groupNameView.setMinWidth(min_width);
			groupNameView.setText(R.string.tag_type_characters);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(x, y, x, y);
			lp.width = min_width;
			tagGroupLayout.addView(groupNameView, lp);

			for (final String tag : book.characters) {
				TextView tagView = (TextView) View.inflate(getApplicationContext(), R.layout.layout_tag, null);
				tagView.setText(tag);
				tagView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						CategoryActivity.launch(
								BookDetailsActivity.this,
								new Category(Category.Type.CHARACTER, tag)
						);
					}
				});
				AutoWrapLayout.LayoutParams alp = new AutoWrapLayout.LayoutParams();
				alp.setMargins(x, y, x, y);
				tagLayout.addView(tagView, alp);
			}
			tagGroupLayout.addView(tagLayout);
			mTagsLayout.addView(tagGroupLayout);
		}

		// Add Tags
		if (!book.tags.isEmpty()) {
			LinearLayout tagGroupLayout = new LinearLayout(this);
			tagGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
			AutoWrapLayout tagLayout = new AutoWrapLayout(this);

			TextView groupNameView = new TextView(this);
			groupNameView.setMinWidth(min_width);
			groupNameView.setText(R.string.tag_type_tag);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(x, y, x, y);
			lp.width = min_width;
			tagGroupLayout.addView(groupNameView, lp);

			for (final String tag : book.tags) {
				TextView tagView = (TextView) View.inflate(getApplicationContext(), R.layout.layout_tag, null);
				tagView.setText(tag);
				tagView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						CategoryActivity.launch(
								BookDetailsActivity.this,
								new Category(Category.Type.TAG, tag)
						);
					}
				});
				AutoWrapLayout.LayoutParams alp = new AutoWrapLayout.LayoutParams();
				alp.setMargins(x, y, x, y);
				tagLayout.addView(tagView, alp);
			}
			tagGroupLayout.addView(tagLayout);
			mTagsLayout.addView(tagGroupLayout);
		}

		// Add Artist Tag
		if (!TextUtils.isEmpty(book.artist)) {
			LinearLayout tagGroupLayout = new LinearLayout(this);
			tagGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
			AutoWrapLayout tagLayout = new AutoWrapLayout(this);

			TextView groupNameView = new TextView(this);
			groupNameView.setMinWidth(min_width);
			groupNameView.setText(R.string.tag_type_artists);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(x, y, x, y);
			lp.width = min_width;
			tagGroupLayout.addView(groupNameView, lp);

			TextView tagView = (TextView) View.inflate(getApplicationContext(), R.layout.layout_tag, null);
			tagView.setText(book.artist);
			tagView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CategoryActivity.launch(
							BookDetailsActivity.this,
							new Category(Category.Type.ARTIST, book.artist)
					);
				}
			});
			AutoWrapLayout.LayoutParams alp = new AutoWrapLayout.LayoutParams();
			alp.setMargins(x, y, x, y);
			tagLayout.addView(tagView, alp);
			tagGroupLayout.addView(tagLayout);
			mTagsLayout.addView(tagGroupLayout);
		}

		// Add Groups Tag
		if (!TextUtils.isEmpty(book.group)) {
			LinearLayout tagGroupLayout = new LinearLayout(this);
			tagGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
			AutoWrapLayout tagLayout = new AutoWrapLayout(this);

			TextView groupNameView = new TextView(this);
			groupNameView.setMinWidth(min_width);
			groupNameView.setText(R.string.tag_type_group);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(x, y, x, y);
			lp.width = min_width;
			tagGroupLayout.addView(groupNameView, lp);

			TextView tagView = (TextView) View.inflate(getApplicationContext(), R.layout.layout_tag, null);
			tagView.setText(book.group);
			tagView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CategoryActivity.launch(
							BookDetailsActivity.this,
							new Category(Category.Type.GROUP, book.group)
					);
				}
			});
			AutoWrapLayout.LayoutParams alp = new AutoWrapLayout.LayoutParams();
			alp.setMargins(x, y, x, y);
			tagLayout.addView(tagView, alp);
			tagGroupLayout.addView(tagLayout);
			mTagsLayout.addView(tagGroupLayout);
		}

		// Add Language Tag
		if (!TextUtils.isEmpty(book.language)) {
			LinearLayout tagGroupLayout = new LinearLayout(this);
			tagGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
			AutoWrapLayout tagLayout = new AutoWrapLayout(this);

			TextView groupNameView = new TextView(this);
			groupNameView.setText(R.string.tag_type_language);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(x, y, x, y);
			lp.width = min_width;
			tagGroupLayout.addView(groupNameView, lp);

			TextView tagView = (TextView) View.inflate(getApplicationContext(), R.layout.layout_tag, null);
			tagView.setText(book.language);
			tagView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CategoryActivity.launch(
							BookDetailsActivity.this,
							new Category(Category.Type.LANGUAGE, book.language)
					);
				}
			});
			AutoWrapLayout.LayoutParams alp = new AutoWrapLayout.LayoutParams();
			alp.setMargins(x, y, x, y);
			tagLayout.addView(tagView, alp);
			tagGroupLayout.addView(tagLayout);
			mTagsLayout.addView(tagGroupLayout);
		}
	}

	private void setUpShareAction() {
		String sendingText = String.format(getString(R.string.action_share_send_text),
				book.titleJP != null ? book.titleJP : book.title,
				NHentaiUrl.getBookDetailsUrl(book.bookId)
		);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Intent.EXTRA_TEXT, sendingText);
		intent.setType("text/plain");
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");
			mShareActionProvider.setShareIntent(intent);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.menu_details, menu);

		MenuItem mFavItem = menu.findItem(R.id.action_favorite);
		mFavItem.setIcon(isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_outline_white_24dp);
		mFavItem.setTitle(isFavorite ? R.string.action_favorite_true : R.string.action_favorite_false);

		MenuItem mShareItem = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareItem);
		setUpShareAction();

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			this.onBackPressed();
			return true;
		}
		if (id == R.id.action_favorite) {
			FavoritesManager fm = FavoritesManager.getInstance(getApplicationContext());
			if (isFavorite) {
				fm.remove(fm.find(book));
			} else {
				fm.add(book);
			}
			fm.save();
			isFavorite = !isFavorite;
			Snackbar.make(
					$(R.id.main_content),
					isFavorite ? R.string.favorite_add_finished : R.string.favorite_remove_finished,
					Snackbar.LENGTH_LONG
			).show();
			invalidateOptionsMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (originFavorite != isFavorite && fromPosition >= 0) {
			Intent intent = new Intent();
			intent.putExtra("position", fromPosition);
			setResult(RESULT_HAVE_FAV, intent);
		}
		super.onBackPressed();
	}

	private void startBookGet() {
		mContentView.setVisibility(View.GONE);
		mProgressWheel.setVisibility(View.VISIBLE);
		mProgressWheel.spin();

		new BookGetTask().execute(book.bookId);
	}

	private class BookGetTask extends AsyncTask<String, Void, BaseMessage> {

		@Override
		protected BaseMessage doInBackground(String... params) {
			return BookApi.getBook(params[0]);
		}

		@Override
		protected void onPostExecute(BaseMessage result) {
			if (result.getCode() == 0) {
				book = result.getData();
				updateUIContent();
			} else {
				mProgressWheel.setVisibility(View.GONE);

				Snackbar.make(
						$(R.id.main_content),
						R.string.tips_network_error,
						Snackbar.LENGTH_LONG
				).setAction(
						R.string.snack_action_try_again,
						new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								startBookGet();
							}
				}).show();
			}
		}

	}
	
	private class CoverTask extends AsyncTask<Book, Void, File> {

		@Override
		protected File doInBackground(Book... params) {
			return BookApi.getCoverFile(BookDetailsActivity.this, params[0]);
		}

		@Override
		protected void onPostExecute(File result) {
			Picasso.with(getApplicationContext())
					.load(result)
					.into(mImageView);
		}
	}

	protected <T extends View> T $(int id) {
		return (T) findViewById(id);
	}

}
