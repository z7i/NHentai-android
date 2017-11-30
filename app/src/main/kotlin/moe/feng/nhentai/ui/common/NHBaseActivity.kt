package moe.feng.nhentai.ui.common

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import moe.feng.nhentai.R
import moe.feng.nhentai.dao.AppDatabase
import moe.feng.nhentai.util.extension.*
import org.jetbrains.anko.AnkoLogger

abstract class NHBaseActivity: AppCompatActivity(), AnkoLogger {

	internal val database by lazy { AppDatabase.INSTANCE ?: AppDatabase.init(this) }
	internal val toolbar: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }

	override fun setContentView(layoutResID: Int) {
		super.setContentView(layoutResID)
		this::setSupportActionBar.tryCall(toolbar)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		if (item?.itemId == android.R.id.home) {
			onBackPressed()
			return true
		}
		return super.onOptionsItemSelected(item)
	}

}