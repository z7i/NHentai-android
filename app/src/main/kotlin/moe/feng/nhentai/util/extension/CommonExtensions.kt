package moe.feng.nhentai.util.extension

import android.util.Log
import kotlin.reflect.KFunction

inline fun <reified OBJ, reified RESULT> OBJ.tryRun(method: OBJ.() -> RESULT): RESULT? =
		try {
			method(this)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}

inline fun <reified METHOD: KFunction<RESULT>, reified RESULT>
		METHOD.tryCall(vararg args: Any?): RESULT? =
		try {
			this.call(*args)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}

fun <T> T.printAsJson(): T = apply { Log.i("Debug", objectAsJson()) }