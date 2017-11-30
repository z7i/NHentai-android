package moe.feng.nhentai.api.model

import moe.feng.nhentai.api.ApiConstants
import moe.feng.nhentai.model.Book

val Book.requestUrl: String get() = ApiConstants.getBookDetailsUrl(bookId)
val Book.thumbUrl: String get() = ApiConstants.getBookThumbUrl(galleryId, images.cover?.fileType)
val Book.bigCoverUrl: String get() = ApiConstants.getBigCoverUrl(galleryId)
val Book.pagePictures: BookPageGetter get() = BookPageGetter(this, false)
val Book.pageThumbnails: BookPageGetter get() = BookPageGetter(this, true)

class BookPageGetter internal constructor(private val book: Book, private val thumb: Boolean) {

	operator fun get(pageNum: Int): String =
			(if (thumb) ApiConstants::getPictureUrl else ApiConstants::getThumbPictureUrl)(
					book.galleryId,
					pageNum.toString(),
					book.images.pages[pageNum].fileType
			)

}