package net.komunan.komunantw.ui.main.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
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
