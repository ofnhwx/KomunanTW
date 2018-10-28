package net.komunan.komunantw.ui.home

import android.support.v4.view.ViewPager
import net.komunan.komunantw.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.viewPager

internal class HomeUI: AnkoComponent<HomeFragment> {
    lateinit var timelines: ViewPager

    override fun createView(ui: AnkoContext<HomeFragment>) = with(ui) {
        viewPager {
            timelines = this
            id = R.id.timelines
        }
    }
}
