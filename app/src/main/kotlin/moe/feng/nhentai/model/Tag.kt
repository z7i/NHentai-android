package moe.feng.nhentai.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import moe.feng.nhentai.R

@Entity(tableName = Tag.TAG) class Tag @JvmOverloads constructor(
		@PrimaryKey val id: Int = 0,
		val type: String = "category",
		val name: String = "",
		val url: String = "",
		val count: Int = 0,
        val isFavourite: Boolean = false
): History.KeyContainer<Tag.HistoryKey> {

	override fun getHistoryAction(): Int = History.ACTION_READ_TAGS

	override fun getHistoryKey(): HistoryKey
			= HistoryKey(id, type, name)

	val typeIcon: Int get() = when (type) {
		TYPE_LANGUAGE -> R.drawable.ic_language_white_24dp
		TYPE_ARTIST -> R.drawable.ic_account_circle_white_24dp
		TYPE_CATEGORY -> R.drawable.ic_loyalty_white_24dp
		TYPE_CHARACTER -> R.drawable.ic_wc_white_24dp
		TYPE_GROUP -> R.drawable.ic_group_work_white_24dp
		TYPE_PARODY -> R.drawable.ic_movie_creation_white_24dp
		TYPE_TAG -> R.drawable.ic_label_white_24dp
		else -> 0
	}

	class HistoryKey(
			val id: Int,
	        val type: String,
	        val name: String
	): History.Key {
		override fun id(): String = id.toString()
	}

	companion object {

		const val TAG = "Tag"

		const val TYPE_ARTIST = "artist"
		const val TYPE_LANGUAGE = "language"
		const val TYPE_CATEGORY = "category"
		const val TYPE_CHARACTER = "character"
		const val TYPE_TAG = "tag"
		const val TYPE_GROUP = "group"
		const val TYPE_PARODY = "parody"

		const val LANG_CHINESE = "chinese"
		const val LANG_ENGLISH = "english"
		const val LANG_JAPANESE = "japanese"
		const val LANG_TRANSLATED = "translated"

	}

}