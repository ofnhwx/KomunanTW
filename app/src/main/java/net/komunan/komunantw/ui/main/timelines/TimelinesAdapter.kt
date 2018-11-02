package net.komunan.komunantw.ui.main.timelines

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_timeline.view.*
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.string
import net.komunan.komunantw.ui.common.SimpleListAdapter

class TimelinesAdapter(timelines: List<Timeline>): SimpleListAdapter<Timeline>(timelines) {
    override fun newView(position: Int, parent: ViewGroup): View {
        return inflater.inflate(R.layout.item_timeline, parent, false)
    }

    override fun bindView(view: View, position: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            val timeline = getItem(position)
            val sourceCount = withContext(Dispatchers.Default) { timeline.sourceCount().toString() }
            view.timeline_name.text = timeline.name
            view.timeline_source_count.text = R.string.format_timeline_srouce_count.string(sourceCount)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }
}
