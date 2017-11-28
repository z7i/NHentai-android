package moe.feng.nhentai.api.rx

import io.reactivex.Single
import moe.feng.nhentai.api.BookApi
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.util.extension.io2mainThread

object RxBookApi {

	fun getBook(bookId: String): Single<Book?> = Single.just(bookId)
			.map(BookApi::getBook)
			.io2mainThread()

}