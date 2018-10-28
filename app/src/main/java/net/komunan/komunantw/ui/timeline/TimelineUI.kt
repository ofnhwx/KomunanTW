package net.komunan.komunantw.ui.timeline

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.recyclerview.v7.recyclerView

internal class TimelineUI: AnkoComponent<TimelineFragment> {
    lateinit var container: RecyclerView

    override fun createView(ui: AnkoContext<TimelineFragment>) = with(ui) {
        recyclerView {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            container = this
        }
    }
}
