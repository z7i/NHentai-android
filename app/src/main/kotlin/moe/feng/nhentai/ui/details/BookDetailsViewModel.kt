package moe.feng.nhentai.ui.details

import android.databinding.ObservableField
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.ui.common.NHViewModel

class BookDetailsViewModel: NHViewModel() {

	val data: ObservableField<Book> = ObservableField()

}