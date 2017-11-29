package moe.feng.nhentai.ui.common

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import me.drakeet.multitype.ItemViewBinder
import moe.feng.nhentai.BR

abstract class NHBindingItemViewBinder<M, DB: ViewDataBinding>:
		ItemViewBinder<M, NHBindingItemViewBinder.BindingHolder<DB>>() {

	abstract val LAYOUT_RES_ID: Int

	override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): BindingHolder<DB>
			= BindingHolder(DataBindingUtil.inflate(inflater, LAYOUT_RES_ID, parent, false))

	override fun onBindViewHolder(holder: BindingHolder<DB>, item: M) {
		Log.i("Binder", "onBindViewHolder")
		holder.binding.setVariable(BR.item, item)
		holder.binding.executePendingBindings()
	}

	class BindingHolder<out T: ViewDataBinding>(val binding: T)
		: RecyclerView.ViewHolder(binding.root)

}