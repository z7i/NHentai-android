package moe.feng.nhentai.ui.details

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import me.drakeet.multitype.MultiTypeAdapter
import moe.feng.nhentai.R
import moe.feng.nhentai.databinding.ActivityNewBookDetailsBinding
import moe.feng.nhentai.model.Book
import moe.feng.nhentai.ui.adapter.TagBinder
import moe.feng.nhentai.ui.common.NHBindingActivity
import moe.feng.nhentai.util.extension.jsonAsObject
import moe.feng.nhentai.util.extension.objectAsJson
import moe.feng.nhentai.util.extension.registerOne

class BookDetailsActivity: NHBindingActivity<ActivityNewBookDetailsBinding>() {

	override val LAYOUT_RES_ID: Int = R.layout.activity_new_book_details

	private val viewModel = BookDetailsViewModel()

	override fun onViewCreated(savedInstanceState: Bundle?) {
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		binding.fab.setOnClickListener {
			// TODO onClick
		}

		binding.tagsList.layoutManager = ChipsLayoutManager.newBuilder(this)
				.setChildGravity(Gravity.START)
				.setScrollingEnabled(false)
				.build()
		binding.tagsList.adapter = MultiTypeAdapter().apply { registerOne(TagBinder()) }

		binding.vm = viewModel

		viewModel.data.set(intent.getStringExtra(EXTRA_BOOK_JSON).jsonAsObject())
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_details, menu)
		return super.onCreateOptionsMenu(menu)
	}

	companion object {

		const val EXTRA_BOOK_JSON = "book_json"

		fun launch(context: Context, book: Book) {
			Intent(context, BookDetailsActivity::class.java).apply {
				flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					Intent.FLAG_ACTIVITY_NEW_DOCUMENT
				} else {
					Intent.FLAG_ACTIVITY_NEW_TASK
				}
				putExtra(EXTRA_BOOK_JSON, book.objectAsJson())
			}.run(context::startActivity)
		}

	}

}