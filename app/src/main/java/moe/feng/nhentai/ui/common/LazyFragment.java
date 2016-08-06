package moe.feng.nhentai.ui.common;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import moe.feng.nhentai.util.Settings;

public abstract class LazyFragment extends Fragment {

	private View parentView;
	private Activity activity;
	protected Handler mHandler;
	protected Settings mSets;

	public abstract @LayoutRes int getLayoutResId();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		parentView = inflater.inflate(getLayoutResId(), container, false);
		activity = getActivity();
		mSets = Settings.getInstance(getApplicationContext());
		finishCreateView(state);
		return parentView;
	}

	public abstract void finishCreateView(Bundle state);

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (context instanceof Activity){
			this.activity=(Activity) context;
		}

		if (mSets == null) {
			mSets = Settings.getInstance(getApplicationContext());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.activity = null;
	}

	public Context getApplicationContext() {
		return this.activity == null ?
				(getActivity() == null ? null : getActivity().getApplicationContext()) :
				this.activity.getApplicationContext();
	}

	public <T extends View> T $(int id) {
		return (T) parentView.findViewById(id);
	}

	protected void setHandler(Handler handler) {
		this.mHandler = handler;
	}

	public Handler getHandler() {
		return mHandler;
	}

}
