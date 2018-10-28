package net.komunan.komunantw.ui.timelines

import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.common.SimpleListAdapter
import net.komunan.komunantw.repository.entity.Timeline
import org.jetbrains.anko.AnkoContext

internal class TimelinesAdapter internal constructor(timelines: List<Timeline>): SimpleListAdapter<Timeline>(timelines) {
    override fun newView(position: Int, parent: ViewGroup): View {
        TimelineUI().let { ui ->
            return ui.createView(AnkoContext.create(parent.context, parent)).also { view ->
                view.tag = ui
            }
        }
    }

    override fun bindView(view: View, position: Int) {
        (view.tag as TimelineUI).bind(items[position])
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }
}