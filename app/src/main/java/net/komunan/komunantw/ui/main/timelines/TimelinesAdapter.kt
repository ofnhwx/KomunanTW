package net.komunan.komunantw.ui.main.timelines

import android.databinding.DataBindingUtil
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.databinding.ItemTimelineBinding
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.string
import net.komunan.komunantw.ui.common.SimpleListAdapter

class TimelinesAdapter(timelines: List<Timeline>): SimpleListAdapter<Timeline>(timelines) {
    override fun newView(position: Int, parent: ViewGroup): View {
        return ItemTimelineBinding.inflate(inflater, parent, false).root
    }

    override fun bindView(view: View, position: Int) {
        DataBindingUtil.bind<ItemTimelineBinding>(view)?.let {
            GlobalScope.launch(Dispatchers.Main) {
                val timeline = getItem(position)
                val sourceCount = GlobalScope.async { timeline.sourceCount().toString() }.await()
                it.timelineName.text = timeline.name
                it.sourceCount.text = R.string.format_timeline_srouce_count.string(sourceCount)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }
}
