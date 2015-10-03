package moe.feng.nhentai.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import moe.feng.nhentai.R;
import moe.feng.nhentai.ui.common.AbsActivity;

public class SearchActivity extends AbsActivity {

	private CardView mTopBar;
	private AppCompatEditText mEditText;

	private static final String TRANSITION_NAME_CARD = "card_view";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);

		ViewCompat.setTransitionName(mTopBar, TRANSITION_NAME_CARD);
	}

	@Override
	protected void setUpViews() {
		mTopBar = $(R.id.top_card_layout);
		mEditText = new AppCompatEditText(this);

		mEditText.setTextAppearance(this, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
		mEditText.setSingleLine(true);
		mEditText.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mEditText.setHint(R.string.search_text_hint);
		mEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == EditorInfo.IME_ACTION_SEARCH) {
					if (mEditText.getText() == null) {
						Snackbar.make(
								$(R.id.root_layout),
								R.string.tips_search_text_empty,
								Snackbar.LENGTH_SHORT
						).show();
						return false;
					}
					if (TextUtils.isEmpty(mEditText.getText().toString())) {
						Snackbar.make(
								$(R.id.root_layout),
								R.string.tips_search_text_empty,
								Snackbar.LENGTH_SHORT
						).show();
						return false;
					}
					SearchResultActivity.launch(SearchActivity.this, mEditText.getText().toString());
					return true;
				}
				return false;
			}
		});

		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mEditText.setLayoutParams(lp);

		ActionBar.LayoutParams lp2 = new ActionBar.LayoutParams(lp);
		mActionBar.setCustomView(mEditText, lp2);
	}

	public static void launch(AppCompatActivity activity, View sharedCardView) {
		ActivityOptionsCompat options = ActivityOptionsCompat
				.makeSceneTransitionAnimation(activity, sharedCardView, TRANSITION_NAME_CARD);
		Intent intent = new Intent(activity, SearchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		activity.startActivity(intent, options.toBundle());
	}

}
