package moe.feng.nhentai.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import moe.feng.nhentai.R;
import moe.feng.nhentai.api.common.NHentaiUrl;
import moe.feng.nhentai.dao.FavoriteCategoriesManager;
import moe.feng.nhentai.model.Category;
import moe.feng.nhentai.ui.common.AbsActivity;
import moe.feng.nhentai.ui.fragment.PageListFragment;

public class CategoryActivity extends AbsActivity {

	private FavoriteCategoriesManager mFCM;

	private ShareActionProvider mShareActionProvider;

	private ViewPager mPager;

	private Category category;

	private boolean isFavorite = false;

	private static final String EXTRA_CATEGORY_JSON = "category_json";

	public static final String TAG = CategoryActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		category = new Gson().fromJson(intent.getStringExtra(EXTRA_CATEGORY_JSON), Category.class);
		setContentView(R.layout.activity_search_result);

		mFCM = FavoriteCategoriesManager.getInstance(getApplicationContext());
		isFavorite = mFCM.contains(category);

		mActionBar.setDisplayHomeAsUpEnabled(true);

		String title = "";
		switch (category.type) {
			case Category.Type.ARTIST:
				title += getString(R.string.tag_type_artists);
				break;
			case Category.Type.CHARACTER:
				title += getString(R.string.tag_type_characters);
				break;
			case Category.Type.GROUP:
				title += getString(R.string.tag_type_group);
				break;
			case Category.Type.PARODY:
				title += getString(R.string.tag_type_parodies);
				break;
			case Category.Type.TAG:
				title += getString(R.string.tag_type_tag);
				break;
			case Category.Type.LANGUAGE:
				title += getString(R.string.tag_type_language);
				break;
		}
		title += category.name;

		mActionBar.setTitle(title);
		setUpShareAction();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mActionBar.setElevation(getResources().getDimension(R.dimen.appbar_elevation));
		}
		supportInvalidateOptionsMenu();
	}

	@Override
	public void onBackPressed(){
		mPager.setAdapter(null);
		super.onBackPressed();

	}

	@Override
	protected void setUpViews() {
		ViewCompat.setElevation($(R.id.appbar_container), getResources().getDimensionPixelSize(R.dimen.appbar_elevation));

		mPager = $(R.id.pager);
		TabLayout mTabLayout = $(R.id.tab_layout);
		mPager.setAdapter(new PagerAdapter(getFragmentManager()));
		mTabLayout.setupWithViewPager(mPager);
	}

	private void setUpShareAction() {
		String title = "";
		if (getSupportActionBar() != null && !TextUtils.isEmpty(getSupportActionBar().getTitle())) {
			title = getSupportActionBar().getTitle().toString().replace(":", "");
		}
		String sendingText = String.format(getString(R.string.action_share_send_category),
				title,
				NHentaiUrl.getCategoryUrl(category, false)
		);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Intent.EXTRA_TEXT, sendingText);
		intent.setType("text/plain");
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");
			mShareActionProvider.setShareIntent(intent);
		}
		Log.d("Me", "onPrepareOptionsMenu: Hey its not me 3");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.menu_category, menu);
		Log.d("Me", "onPrepareOptionsMenu: Hey its not me 1");
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
		Log.d("Me", "onPrepareOptionsMenu: Hey its not me 2");
		if (id == R.id.action_favorite) {
			if (isFavorite) {
				mFCM.remove(mFCM.find(category));
			} else {
				mFCM.add(category);
			}
			mFCM.save();
			isFavorite = !isFavorite;
			Snackbar.make(
					mPager,
					isFavorite ? R.string.favorite_categories_add_finished : R.string.favorite_categories_remove_finished,
					Snackbar.LENGTH_LONG
			).show();
			supportInvalidateOptionsMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void launch(AppCompatActivity activity, Category category) {
		Intent intent = new Intent(activity, CategoryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(EXTRA_CATEGORY_JSON, category.toJSONString());
		activity.startActivity(intent);
	}

	private class PagerAdapter extends FragmentPagerAdapter {

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return PageListFragment.newInstance(NHentaiUrl.getCategoryUrl(category, position == 1));
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getResources().getStringArray(R.array.category_tabs_title) [position];
		}

	}

}
