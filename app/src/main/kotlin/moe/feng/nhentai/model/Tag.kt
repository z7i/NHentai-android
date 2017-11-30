package moe.feng.nhentai.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

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