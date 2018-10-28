package net.komunan.komunantw.ui.timelines

import android.widget.ListView
import net.komunan.komunantw.repository.entity.Timeline
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.listView

internal class TimelinesUI: AnkoComponent<TimelinesFragment> {
    lateinit var timelines: ListView

    override fun createView(ui: AnkoContext<TimelinesFragment>) = with(ui) {
        listView {
            timelines = this
        }
    }

    fun bind(timelines: List<Timeline>) {
        this.timelines.adapter = TimelinesAdapter(timelines)
    }
}
