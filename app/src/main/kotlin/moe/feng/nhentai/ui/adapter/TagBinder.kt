package moe.feng.nhentai.ui.adapter

import moe.feng.nhentai.R
import moe.feng.nhentai.databinding.ItemTagBinding
import moe.feng.nhentai.model.Tag
import moe.feng.nhentai.ui.common.NHBindingItemViewBinder

class TagBinder: NHBindingItemViewBinder<Tag, ItemTagBinding>() {

	override val LAYOUT_RES_ID: Int = R.layout.item_tag

	override fun onViewHolderCreated(holder: BindingHolder<Tag, ItemTagBinding>) {
		holder.itemView.setOnClickListener {
			// TODO onClick
		}
	}

}