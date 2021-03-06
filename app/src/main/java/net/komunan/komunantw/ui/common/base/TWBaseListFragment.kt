package net.komunan.komunantw.ui.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import net.komunan.komunantw.R

abstract class TWBaseListFragment<T : Diffable, VH : TWBaseListAdapter.ViewHolder<T>> : TWBaseFragment() {
    protected abstract fun adapter(): TWBaseListAdapter<T, VH>
    protected abstract fun items(): LiveData<List<T>>
    protected abstract fun layoutManager(): RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<RecyclerView>(R.id.container)?.also { container ->
            container.layoutManager = layoutManager()
            adapter().also { adapter ->
                container.adapter = adapter
                items().observe(this, Observer { items ->
                    adapter.submitList(items)
                })
            }
        }
    }
}
