package net.komunan.komunantw.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.simple_view_pager.*
import net.komunan.komunantw.Preference
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.ui.common.TWBaseFragment

class HomeFragment: TWBaseFragment() {
    companion object {
        @JvmStatic
        fun create() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_view_pager, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.timelines.observeOnNotNull(this) { timelines ->
            val currentPage = Preference.currentPage
            pager.adapter = HomeAdapter(childFragmentManager, timelines)
            if (currentPage > 0 && currentPage <= timelines.size) {
                pager.setCurrentItem(currentPage, false)
            }
            updateTitle(currentPage)
        }
        pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Preference.currentPage = position
                updateTitle(position)
            }
        })
    }

    private fun updateTitle(page: Int) {
        activity?.title = pager.adapter?.getPageTitle(page)
    }
}
