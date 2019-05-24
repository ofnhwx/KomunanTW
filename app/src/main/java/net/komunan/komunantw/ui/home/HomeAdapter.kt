package net.komunan.komunantw.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.ui.home.tab.HomeTabFragment

class HomeAdapter(fm: FragmentManager, private val timelines: List<Timeline>) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return timelines.size
    }

    override fun getItem(position: Int): Fragment {
        return HomeTabFragment.create(timelines[position])
    }

    override fun getItemId(position: Int): Long {
        return timelines[position].id
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position >= 0 && position < timelines.size) {
            timelines[position].name
        } else {
            null
        }
    }
}
