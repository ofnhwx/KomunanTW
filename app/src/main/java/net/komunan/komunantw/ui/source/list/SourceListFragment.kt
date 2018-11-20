package net.komunan.komunantw.ui.source.list

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter
import net.komunan.komunantw.ui.common.base.TWBaseListFragment

class SourceListFragment: TWBaseListFragment<Source, SourceListAdapter.ViewHolder>() {
    companion object {
        @JvmStatic
        fun create() = SourceListFragment()
    }

    override val name: String?
        get() = string[R.string.fragment_source_list]()

    override fun adapter(): TWBaseListAdapter<Source, SourceListAdapter.ViewHolder> {
        return SourceListAdapter()
    }

    override fun layoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    override fun items(): LiveData<List<Source>> {
        return viewModel(SourceListViewModel::class.java).sources
    }
}
