package moe.feng.nhentai.ui.main.fragment

import android.databinding.ObservableField
import android.support.v4.widget.SwipeRefreshLayout
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import moe.feng.nhentai.api.PageApi
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.ui.common.NHViewModel
import org.jetbrains.anko.*

class HomeViewModel: NHViewModel(), SwipeRefreshLayout.OnRefreshListener {

	var updateTime = ObservableField("")
	var isRefreshing = ObservableField(false)
	var currentPage = ObservableField(1)
	var items = ObservableField(mutableListOf<Book>())

	override fun onRefresh() {
		async(UI) {
			if (!isRefreshing.get()) {
				isRefreshing.set(true)

				val result = PageApi.getHomePageListAsync(1).await()?.result
				if (result?.isNotEmpty() == true) {
					items.set(result.toMutableList())
					currentPage.set(1)
				} else {
					// TODO Error
					error("Error")
				}

				// TODO Set update time
				isRefreshing.set(false)
			}
		}
	}

	fun onNext() {
		async(UI) {
			if (!isRefreshing.get()) {
				isRefreshing.set(true)

				val result = PageApi.getHomePageListAsync(currentPage.get() + 1).await()?.result
				if (result?.isNotEmpty() == true) {
					items.set((items.get() + result).toMutableList())
					currentPage.set(currentPage.get() + 1)
				} else {
					// TODO Error
					error("Error")
				}

				// TODO Set update time
				isRefreshing.set(false)
			}
		}
	}

}