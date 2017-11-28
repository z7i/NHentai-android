package moe.feng.nhentai.util.extension

inline fun <reified OBJ, reified RESULT> OBJ.tryRun(method: OBJ.() -> RESULT): RESULT? =
		try {
			method(this)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}