package moe.feng.nhentai.ui.adapter;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import moe.feng.nhentai.R;
import moe.feng.nhentai.dao.FavoriteCategoriesManager;
import moe.feng.nhentai.model.Category;
import moe.feng.nhentai.ui.common.AbsRecyclerViewAdapter;
import moe.feng.nhentai.util.ColorGenerator;
import moe.feng.nhentai.util.ColorUtil;

public class FavoriteCategoriesRecyclerAdapter extends AbsRecyclerViewAdapter {

	private FavoriteCategoriesManager fm;

	private ColorGenerator mColorGenerator;

	public static final String TAG = FavoriteCategoriesRecyclerAdapter.class.getSimpleName();

	public FavoriteCategoriesRecyclerAdapter(RecyclerView recyclerView, FavoriteCategoriesManager fm) {
		super(recyclerView);
		this.fm = fm;
		mColorGenerator = ColorGenerator.MATERIAL;
	}

	@Override
	public ClickableViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		bindContext(viewGroup.getContext());
		View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_category, viewGroup, false);
		return new ViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(ClickableViewHolder holder, final int position) {
		super.onBindViewHolder(holder, position);
		if (holder instanceof ViewHolder) {
			ViewHolder mHolder = (ViewHolder) holder;
			Category category = fm.get(position);

			mHolder.mTitleTextView.setText(category.name);
			int color = mColorGenerator.getColor(category.type + category.name);
			mHolder.mCard.setCardBackgroundColor(ColorUtil.getMiddleColor(color, Color.BLACK, 0.2f));

			String typeShowName;
			switch (category.type) {
				case Category.Type.ARTIST:
					typeShowName = getContext().getString(R.string.tag_type_artists);
					break;
				case Category.Type.CHARACTER:
					typeShowName = getContext().getString(R.string.tag_type_characters);
					break;
				case Category.Type.GROUP:
					typeShowName = getContext().getString(R.string.tag_type_group);
					break;
				case Category.Type.PARODY:
					typeShowName = getContext().getString(R.string.tag_type_parodies);
					break;
				case Category.Type.LANGUAGE:
					typeShowName = getContext().getString(R.string.tag_type_language);
					break;
				case Category.Type.TAG:
				default:
					typeShowName = getContext().getString(R.string.tag_type_tag);
					break;
			}
			mHolder.mTypeTextView.setText(typeShowName);
		}
	}

	@Override
	public int getItemCount() {
		return fm.size();
	}

	public class ViewHolder extends ClickableViewHolder {

		public TextView mTitleTextView, mTypeTextView;
		public CardView mCard;

		public ViewHolder(View itemView) {
			super(itemView);
			mTitleTextView = (TextView) itemView.findViewById(R.id.book_title);
			mTypeTextView = (TextView) itemView.findViewById(R.id.book_type);
			mCard = (CardView) itemView.findViewById(R.id.book_card);

			itemView.setTag(this);
		}

	}

}
