package net.komunan.komunantw.ui.timelines

import android.view.ViewGroup
import android.widget.TextView
import net.komunan.komunantw.repository.entity.Timeline
import org.jetbrains.anko.*

internal class TimelineUI: AnkoComponent<ViewGroup> {
    lateinit var name: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            name = textView().lparams(matchParent, wrapContent)
        }
    }

    fun bind(timeline: Timeline) {
        name.text = timeline.name
    }
}