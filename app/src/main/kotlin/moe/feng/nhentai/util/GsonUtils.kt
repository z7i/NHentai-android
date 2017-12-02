package moe.feng.nhentai.util

import com.google.gson.GsonBuilder

object GsonUtils {

	private val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

	fun <T> toJson(obj: T): String = gson.toJson(obj)

	fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)

	inline fun <reified T> fromJson(json: String): T = fromJson(json, T::class.java)

}