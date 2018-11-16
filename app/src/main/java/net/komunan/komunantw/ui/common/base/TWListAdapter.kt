package net.komunan.komunantw.ui.common.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class TWListAdapter<T: Diffable, VH : RecyclerView.ViewHolder>: ListAdapter<T, VH>(ItemCallback<T>()) {
    private class ItemCallback<T: Diffable>: DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem.isTheSame(newItem)
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem.isContentsTheSame(newItem)
    }

    protected fun inflate(@LayoutRes layout: Int, parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }
}
