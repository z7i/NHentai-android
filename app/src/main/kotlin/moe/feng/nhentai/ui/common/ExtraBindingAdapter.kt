package moe.feng.nhentai.ui.common

import android.databinding.BindingAdapter
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import me.drakeet.multitype.MultiTypeAdapter
import org.jetbrains.anko.*

object ExtraBindingAdapter: AnkoLogger {

	@BindingAdapter("bind:items")
	@JvmStatic fun setMultiTypeAdapterItems(view: RecyclerView, list: List<*>) {
		info("setMultiTypeAdapterItems")
		(view.adapter as? MultiTypeAdapter)?.items = list
		view.adapter?.notifyDataSetChanged()
	}

	@BindingAdapter("bind:isRefreshing")
	@JvmStatic fun setIsRefreshing(view: SwipeRefreshLayout, boolean: Boolean) {
		info("setIsRefreshing=$boolean")
		view.isRefreshing = boolean
	}

	@BindingAdapter("android:src")
	@JvmStatic fun setImageViewResource(imageView: ImageView, resource: Int) {
		imageView.setImageResource(resource)
	}

}