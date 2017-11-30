package moe.feng.nhentai.ui.adapter

import moe.feng.nhentai.R
import moe.feng.nhentai.databinding.ListItemBookCardBinding
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.ui.common.NHBindingItemViewBinder
import moe.feng.nhentai.ui.details.BookDetailsActivity

class BookCardBinder: NHBindingItemViewBinder<Book, ListItemBookCardBinding>() {

	override val LAYOUT_RES_ID: Int = R.layout.list_item_book_card

	override fun onViewHolderCreated
			(holder: NHBindingItemViewBinder.BindingHolder<Book, ListItemBookCardBinding>) {
		holder.itemView.setOnClickListener {
			holder.currentItem?.let { BookDetailsActivity.launch(holder.context, it) }
		}
	}

}