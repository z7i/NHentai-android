package moe.feng.nhentai.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import moe.feng.nhentai.util.extension.*
import java.util.*

@Entity(tableName = History.TAG) class History(
		val action: Int,
		@PrimaryKey var time: Long = System.currentTimeMillis(),
		var key: String = ""
) {

	fun getDate(): Date {
		val date = Date()
		date.time = time
		return date
	}

	interface KeyContainer<out T: Key> {
		fun getHistoryAction(): Int
		fun getHistoryKey(): T
	}

	interface Key {

		fun id(): String

	}

	companion object {

		const val TAG = "History"

		const val ACTION_READ_BOOK = 0
		const val ACTION_READ_TAGS = 1
		const val ACTION_SEARCH = 2

		fun <T: Key> from(item: KeyContainer<T>): History
				= History(item.getHistoryAction(), key = item.getHistoryKey().id())

	}

}