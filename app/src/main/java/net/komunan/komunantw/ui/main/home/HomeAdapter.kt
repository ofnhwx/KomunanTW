package net.komunan.komunantw.ui.main.home

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.main.home.tab.HomeTabFragment

class HomeAdapter(fm: FragmentManager?, private val timelines: List<Timeline>): FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return timelines.size
    }

    override fun getItem(position: Int): Fragment {
        return HomeTabFragment.create(timelines[position])
    }
}