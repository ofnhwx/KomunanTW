package net.komunan.komunantw.ui.home

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.home.TimelineFragment

internal class HomeAdapter internal constructor(fm: FragmentManager?, private val timelines: List<Timeline>): FragmentPagerAdapter(fm) {
    override fun getCount(): Int = timelines.size
    override fun getItem(position: Int): Fragment = TimelineFragment.create(timelines[position])
}
