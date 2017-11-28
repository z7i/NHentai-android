package moe.feng.nhentai.util

import com.google.gson.GsonBuilder

object GsonUtils {

	private val gson = GsonBuilder().create()

	fun toJson(obj: Any): String = gson.toJson(obj)

	fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)

	inline fun <reified T> fromJson(json: String): T = fromJson(json, T::class.java)

}