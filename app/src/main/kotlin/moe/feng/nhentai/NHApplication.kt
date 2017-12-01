package moe.feng.nhentai

import android.app.Application
import com.orhanobut.hawk.Hawk

class NHApplication: Application() {

	override fun onCreate() {
		super.onCreate()
		Hawk.init(this).build()
	}

}