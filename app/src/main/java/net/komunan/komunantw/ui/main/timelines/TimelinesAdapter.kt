package net.komunan.komunantw.ui.main.timelines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_timeline.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.TWListAdapter
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.string

class TimelinesAdapter: TWListAdapter<Timeline, TimelinesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_timeline, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(timeline: Timeline) {
            GlobalScope.launch(Dispatchers.Main) {
                val sourceCount = withContext(Dispatchers.Default) { timeline.sourceCount().toString() }
                itemView.timeline_name.text = timeline.name
                itemView.timeline_source_count.text = R.string.format_timeline_source_count.string(sourceCount)
                itemView.setOnClickListener {
                    Transition.execute(Transition.Target.TIMELINE_EDIT, timeline.id)
                }
            }
        }
    }
}
