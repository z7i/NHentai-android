package moe.feng.nhentai.util.extension

import moe.feng.nhentai.util.GsonUtils

fun Any.objectAsJson(): String = GsonUtils.toJson(this)

inline fun <reified T> String.jsonAsObject(): T = GsonUtils.fromJson(this)