package moe.feng.nhentai.ui.common;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import moe.feng.nhentai.R;
import moe.feng.nhentai.util.Settings;
import moe.feng.nhentai.util.Utility;

public abstract class AbsActivity extends AppCompatActivity {

	protected Toolbar mToolbar;
	protected ActionBar mActionBar;
	protected Settings mSets;

	protected int statusBarHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SetTranslucent(true);
		mSets = Settings.getInstance(getApplicationContext());
		super.onCreate(savedInstanceState);

	}

	protected void SetTranslucent (boolean statusBarTranslucent) {
		/** Set up translucent status bar */
		if (statusBarTranslucent) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				statusBarHeight = Utility.getStatusBarHeight(getApplicationContext());
				getWindow().setStatusBarColor(Color.TRANSPARENT);
				getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.deep_purple_800));
			}
		}

	}

	protected abstract void setUpViews();

	@Override
	public void setContentView(@LayoutRes int layoutResId) {
		super.setContentView(layoutResId);

		try {
			View statusHeaderView = $(R.id.status_bar_header);
			statusHeaderView.getLayoutParams().height = statusBarHeight;
		} catch (NullPointerException e) {
			Log.e("setContentView", "Cannot find status header.");
		}

		try {
			mToolbar = $(R.id.toolbar);
			setSupportActionBar(mToolbar);
		} catch (Exception e) {
			Log.e("setContentView", "Cannot find toolbar.");
		}
		mActionBar = getSupportActionBar();

		setUpViews();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	protected <T extends View> T $(int id) {
		return (T) findViewById(id);
	}

}
