package net.komunan.komunantw.ui.timeline.list

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_timeline.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.string
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
        override fun bind(item: Timeline) {
            GlobalScope.launch(Dispatchers.Main) {
                val sourceCount = withContext(Dispatchers.Default) { "${Timeline.sourceDao.countByTimelineId(item.id)}" }
                itemView.timeline_name.text = item.name
                itemView.timeline_source_count.text = string[R.string.fragment_timeline_list_source_count](sourceCount)
                itemView.setOnClickListener { onClickEvent?.invoke(item.id) }
            }
        }
    }
}
