package moe.feng.nhentai.api

import moe.feng.nhentai.model.Book
import moe.feng.nhentai.util.HttpUtils
import moe.feng.nhentai.util.extension.readAsJsonObject

object BookApi {

	suspend fun getBook(bookId: String): Book? = ApiConstants.getBookDetailsUrl(bookId)
			.run(HttpUtils::requestUrl)
			.readAsJsonObject()

}