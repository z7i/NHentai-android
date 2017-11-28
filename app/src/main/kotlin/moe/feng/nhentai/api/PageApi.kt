package moe.feng.nhentai.api

import moe.feng.nhentai.model.Book
import moe.feng.nhentai.util.HttpUtils
import moe.feng.nhentai.util.extension.readAsJsonObject

object PageApi {

	fun getPageList(url: String): List<Book>? = HttpUtils.requestUrl(url)
			.readAsJsonObject()

	fun getHomePageList(pageNum: Int): List<Book>? = ApiConstants.getHomePageUrl(pageNum)
			.run(HttpUtils::requestUrl)
			.readAsJsonObject()

	fun getSearchPageList(keyword: String, pageNum: Int): List<Book>? = ApiConstants
			.getSearchUrl(keyword, pageNum)
			.run(HttpUtils::requestUrl)
			.readAsJsonObject()

}