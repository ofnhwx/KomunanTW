package net.komunan.komunantw.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.R
import net.komunan.komunantw.ui.common.TWBaseViewModel
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.repository.entity.Timeline
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.viewPager

class HomeFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = HomeFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(HomeViewModel::class.java) }
    private val ui = HomeUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.run {
            columns().observeOnNotNull(this@HomeFragment) { columns ->
                ui.timelines.adapter = HomeAdapter(fragmentManager, columns)
            }
        }
    }

    private class HomeViewModel: TWBaseViewModel() {
        fun columns() = Timeline.findAllAsync()
    }

    private class HomeAdapter internal constructor(fm: FragmentManager?, private val timelines: List<Timeline>): FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return timelines.size
        }

        override fun getItem(position: Int): Fragment {
            return HomeTabFragment.create(timelines[position])
        }
    }

    private class HomeUI: AnkoComponent<HomeFragment> {
        lateinit var timelines: ViewPager

        override fun createView(ui: AnkoContext<HomeFragment>): View = with(ui) {
            timelines = viewPager {
                id = R.id.timelines
            }
            return@with timelines
        }
    }
}
