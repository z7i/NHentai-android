package moe.feng.nhentai.model

import com.google.gson.annotations.SerializedName

class Picture {

	@SerializedName("h") var height: Int = 0
	@SerializedName("w") var width: Int = 0
	private @SerializedName("t") var type: String = "j"
	val isJpg: Boolean get() = type == "j"
	val isPng: Boolean get() = type == "p"

	fun setIsJpg() {
		type = "j"
	}

	fun setIsPng() {
		type = "p"
	}

}