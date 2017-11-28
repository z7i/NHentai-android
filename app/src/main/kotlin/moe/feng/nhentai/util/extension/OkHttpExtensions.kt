package moe.feng.nhentai.util.extension

import okhttp3.Response
import okhttp3.ResponseBody

fun Response.readAsString(): String? = this.body()?.tryRun(ResponseBody::string)

inline fun <reified T> Response.readAsJsonObject(): T? = this.readAsString()?.jsonAsObject()