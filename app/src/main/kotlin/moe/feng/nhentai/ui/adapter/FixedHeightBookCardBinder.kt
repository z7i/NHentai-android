package moe.feng.nhentai.ui.adapter

import moe.feng.nhentai.R
import moe.feng.nhentai.databinding.ItemBookCardFixedHeightBinding
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.ui.common.NHBindingItemViewBinder
import moe.feng.nhentai.ui.details.BookDetailsActivity

class FixedHeightBookCardBinder: NHBindingItemViewBinder<Book, ItemBookCardFixedHeightBinding>() {

	override val LAYOUT_RES_ID: Int get() = R.layout.item_book_card_fixed_height

	override fun onViewHolderCreated(holder: BindingHolder<Book, ItemBookCardFixedHeightBinding>) {
		holder.binding.bookCard.setOnClickListener {
			holder.currentItem?.let { BookDetailsActivity.launch(holder.context, it) }
		}
	}

}