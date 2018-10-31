package net.komunan.komunantw.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import net.komunan.komunantw.ReleaseApplication

abstract class SimpleListAdapter<out T>(protected val items: List<T>) : BaseAdapter() {
    protected val inflater: LayoutInflater = LayoutInflater.from(ReleaseApplication.context)

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): T {
        return items[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (convertView ?: newView(position, parent)).apply {
            bindView(this, position)
        }
    }

    abstract fun newView(position: Int, parent: ViewGroup): View
    abstract fun bindView(view: View, position: Int)
}
