package net.komunan.komunantw.ui.timeline.list

import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.databinding.ItemTimelineBinding
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.base.TWBaseDraggableListAdapter
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter

class TimelineListAdapter: TWBaseDraggableListAdapter<Timeline, TimelineListAdapter.ViewHolder>() {
    var onClickEvent: ((timelineId: Long) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(R.layout.item_timeline, parent))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    inner class ViewHolder(itemView: View) : TWBaseListAdapter.ViewHolder<Timeline>(itemView) {
        private val binding by lazy { DataBindingUtil.bind<ItemTimelineBinding>(itemView)!! }

        override fun bind(item: Timeline) {
            GlobalScope.launch(Dispatchers.Main) {
                val sourceCount = withContext(Dispatchers.Default) { "${Timeline.sourceDao.countByTimelineId(item.id)}" }
                binding.sourceCount = string[R.string.fragment_timeline_list_source_count](sourceCount)
            }
            binding.timeline = item
            binding.onClickEvent = { onClickEvent?.invoke(item.id) }
        }
    }
}
