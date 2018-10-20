package net.komunan.komunantw.ui.timeline

import android.arch.lifecycle.Observer
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
import net.komunan.komunantw.repository.entity.Column
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.viewPager

class TimelineFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create() = TimelineFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(TimelineViewModel::class.java) }
    private val ui = TimelineUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.columns.observe(this, Observer { columns ->
            columns?.let {
                ui.timelines.adapter = TimelineAdapter(fragmentManager, columns)
            }
        })
    }

    private class TimelineUI: AnkoComponent<TimelineFragment> {
        lateinit var timelines: ViewPager

        override fun createView(ui: AnkoContext<TimelineFragment>) = with(ui) {
            viewPager {
                timelines = this
                id = R.id.timelines
            }
        }
    }

    private class TimelineAdapter internal constructor(fm: FragmentManager?, private val columns: List<Column>): FragmentPagerAdapter(fm) {
        override fun getCount(): Int = columns.size
        override fun getItem(position: Int): Fragment = TimelineColumnFragment.create(columns[position])
    }
}
