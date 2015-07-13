package moe.feng.nhentai.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import moe.feng.nhentai.R;
import moe.feng.nhentai.ui.fragment.main.DownloadManagerFragment;
import moe.feng.nhentai.ui.fragment.main.FavoriteCategoryFragment;
import moe.feng.nhentai.ui.fragment.main.FavoriteFragment;
import moe.feng.nhentai.ui.fragment.main.HomeFragment;

public class HomePagerAdapter extends FragmentPagerAdapter {

	private HomeFragment homeFragment;
	private DownloadManagerFragment downloadManagerFragment;
	private FavoriteFragment favoriteFragment;
	private FavoriteCategoryFragment favoriteCategoryFragment;

	private String[] titles;

	public HomePagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		titles = context.getResources().getStringArray(R.array.page_titles);
		homeFragment = new HomeFragment();
		downloadManagerFragment = new DownloadManagerFragment();
		favoriteFragment = new FavoriteFragment();
		favoriteCategoryFragment = new FavoriteCategoryFragment();
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return homeFragment;
			case 1:
				return downloadManagerFragment;
			case 2:
				return favoriteFragment;
			case 3:
				return favoriteCategoryFragment;
			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		return titles.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	public void notifyFragmentsDataUpdated(int itemPosition) {
		homeFragment.onDataUpdate(itemPosition);
		downloadManagerFragment.onDataUpdate();
		favoriteFragment.onDataUpdate();
	}

}
