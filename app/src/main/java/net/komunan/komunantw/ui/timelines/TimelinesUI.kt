package net.komunan.komunantw.ui.timelines

import android.support.v4.view.ViewPager
import net.komunan.komunantw.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.viewPager

internal class TimelinesUI: AnkoComponent<TimelinesFragment> {
    lateinit var timelines: ViewPager

    override fun createView(ui: AnkoContext<TimelinesFragment>) = with(ui) {
        viewPager {
            timelines = this
            id = R.id.timelines
        }
    }
}
