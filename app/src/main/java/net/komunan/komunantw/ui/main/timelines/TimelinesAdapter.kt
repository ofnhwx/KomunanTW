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
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.string

class TimelinesAdapter(private val timelines: List<Timeline>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_timeline, parent, false))
    }

    override fun getItemCount(): Int {
        return timelines.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(timelines[position])
    }

    override fun getItemId(position: Int): Long {
        return timelines[position].id
    }

    private class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
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
