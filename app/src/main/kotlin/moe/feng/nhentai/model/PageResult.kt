package moe.feng.nhentai.model

import com.google.gson.annotations.SerializedName

data class PageResult(
		var result: List<Book>?,
		@SerializedName("num_pages") var totalPages: Int,
		@SerializedName("per_page") var perPageCount: Int
)