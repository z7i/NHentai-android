package moe.feng.nhentai.ui.main.fragment

import android.os.Bundle
import android.view.View
import me.drakeet.multitype.MultiTypeAdapter
import moe.feng.nhentai.R
import moe.feng.nhentai.databinding.FragmentNewHomeBinding
import moe.feng.nhentai.ui.adapter.BookCardBinder
import moe.feng.nhentai.ui.common.NHBindingFragment
import moe.feng.nhentai.ui.common.setOnLoadMoreListener
import moe.feng.nhentai.util.extension.*

class HomeFragment: NHBindingFragment<FragmentNewHomeBinding>() {

	override val LAYOUT_RES_ID: Int = R.layout.fragment_new_home

	private val adapter = MultiTypeAdapter()

	private val viewModel = HomeViewModel()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		binding?.vm = viewModel

		binding?.apply {
			adapter.registerOne(BookCardBinder())

			recyclerView.adapter = adapter
			recyclerView.setOnLoadMoreListener(viewModel::onNext)

			swipeRefreshLayout.setColorSchemeResources(R.color.deep_purple_500)
			swipeRefreshLayout.setOnRefreshListener(viewModel)
		}
	}

	override fun onPause() {
		super.onPause()
		viewModel.saveLatestBook()
	}

}