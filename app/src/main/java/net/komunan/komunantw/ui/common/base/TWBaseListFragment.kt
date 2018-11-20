package net.komunan.komunantw.ui.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.simple_recycler_view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.observeOnNotNull

abstract class TWBaseListFragment<T: Diffable, VH: TWBaseListAdapter.ViewHolder<T>>: TWBaseFragment() {
    protected abstract fun adapter(): TWBaseListAdapter<T, VH>
    protected abstract fun items(): LiveData<List<T>>
    protected abstract fun layoutManager(): RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        container.layoutManager = layoutManager()
        adapter().also { adapter ->
            container.adapter = adapter
            items().observeOnNotNull(this) { items ->
                adapter.submitList(items)
            }
        }
    }
}
