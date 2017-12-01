package moe.feng.nhentai.ui.details

import android.databinding.ObservableField
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.*
import moe.feng.nhentai.api.BookApi
import moe.feng.nhentai.api.PageApi
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.ui.common.NHViewModel

class BookDetailsViewModel: NHViewModel() {

	val data: ObservableField<Book> = ObservableField()
	val relatedBooks: ObservableField<List<Book>?> = ObservableField()

	fun loadBookDataIfNecessary() {
		if (data.get().pageCount <= 0) {
			async(UI) {
				val bookResult = BookApi.getBookAsync(data.get().bookId).await()
				data.set(bookResult)
				loadRelatedBooks()
			}
		} else {
			loadRelatedBooks()
		}
	}

	private fun loadRelatedBooks() {
		async(UI) {
			val pageResult = PageApi.getPageListAsync(data.get().relatedApiUrl).await()
			pageResult?.result?.let(relatedBooks::set)
		}
	}

}