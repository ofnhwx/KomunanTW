package net.komunan.komunantw.common

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class TWListAdapter<T: Diffable, VH : RecyclerView.ViewHolder>: ListAdapter<T, VH>(ItemCallback<T>()) {
    private class ItemCallback<T: Diffable>: DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem.isTheSame(newItem)
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem.isContentsTheSame(newItem)
    }
}
