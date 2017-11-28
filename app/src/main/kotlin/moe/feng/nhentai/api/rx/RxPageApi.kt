package moe.feng.nhentai.api.rx

import io.reactivex.Single
import moe.feng.nhentai.api.PageApi
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.util.extension.io2mainThread

object RxPageApi {

	fun getPageList(url: String): Single<List<Book>?> = Single.just(url)
			.map(PageApi::getPageList)
			.io2mainThread()

	fun getHomePageList(pageNum: Int): Single<List<Book>?> = Single.just(pageNum)
			.map(PageApi::getHomePageList)
			.io2mainThread()

	fun getSearchPageList(keyword: String, pageNum: Int): Single<List<Book>?> = Single
			.just(keyword to pageNum)
			.map { (keyword, pageNum) -> PageApi.getSearchPageList(keyword, pageNum) }
			.io2mainThread()

}