package net.komunan.komunantw.ui.timeline.list

import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import net.komunan.komunantw.R
import net.komunan.komunantw.databinding.ItemTimelineBinding
import net.komunan.komunantw.repository.entity.ext.TimelineExt
import net.komunan.komunantw.ui.common.base.TWBaseDraggableListAdapter
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class TimelineListAdapter: TWBaseDraggableListAdapter<TimelineExt, TimelineListAdapter.ViewHolder>() {
    var onClickEvent: ((timelineId: Long) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_timeline, parent))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    inner class ViewHolder(itemView: View) : TWBaseListAdapter.ViewHolder<TimelineExt>(itemView) {
        private val binding by lazy { DataBindingUtil.bind<ItemTimelineBinding>(itemView)!! }

        override fun bind(item: TimelineExt) {
            binding.timeline = item
            binding.onClickEvent = { onClickEvent?.invoke(item.id) }
        }
    }
}
