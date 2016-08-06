package moe.feng.nhentai.ui.adapter;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import moe.feng.nhentai.R;
import moe.feng.nhentai.dao.SearchHistoryManager;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;

public class HistoryRecyclerAdapter extends AbsRecyclerViewAdapter {

	private SearchHistoryManager mHM;

	private AddTextListener mAddTextListener;

	public HistoryRecyclerAdapter(RecyclerView recyclerView, SearchHistoryManager hm) {
		super(recyclerView);
		this.mHM = hm;
	}

	@SuppressWarnings("unused")
	public AddTextListener getAddTextListener() {
		return this.mAddTextListener;
	}

	public void setAddTextListener(AddTextListener listener) {
		this.mAddTextListener = listener;
	}

	@Override
	public ClickableViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		bindContext(viewGroup.getContext());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_history, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ClickableViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		if (holder instanceof ViewHolder) {
			ViewHolder mHolder = (ViewHolder) holder;
			mHolder.mTextView.setText(mHM.get(position));
		}
	}

	@Override
	public int getItemCount() {
		return mHM.getAll().size();
	}

	public class ViewHolder extends ClickableViewHolder {

		AppCompatTextView mTextView;
		ImageButton mBtnAdd;

		public ViewHolder(View itemView) {
			super(itemView);
			this.mTextView = (AppCompatTextView) itemView.findViewById(R.id.history_title);
			this.mBtnAdd = (ImageButton) itemView.findViewById(R.id.btn_add);
			this.mBtnAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mAddTextListener != null && mTextView.getText() != null) {
						mAddTextListener.onTextAdd(mTextView.getText().toString());
					}
				}
			});
		}

	}

	public interface AddTextListener {

		void onTextAdd(String string);

	}

}
