package net.komunan.komunantw.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.simple_view_pager.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.ui.common.base.TWBaseFragment

class HomeFragment : TWBaseFragment() {
    companion object {
        @JvmStatic
        fun create() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_view_pager, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel(HomeViewModel::class.java).also { viewModel ->
            viewModel.timelines.observe(this, Observer { timelines ->
                container.adapter = HomeAdapter(childFragmentManager, timelines)
                changePage(Preference.currentPage)
            })
            viewModel.startUpdate()
        }

        activityViewModel(HomeActivityViewModel::class.java).also { aViewModel ->
            aViewModel.currentPage.observe(this, Observer { currentPage ->
                changePage(currentPage)
            })
            container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    aViewModel.setPage(position)
                }
            })
        }
    }

    private fun changePage(page: Int) {
        container.adapter?.also { adapter ->
            if (page >= 0 && page < adapter.count) {
                if (container.currentItem != page) {
                    container.setCurrentItem(page, false)
                }
                activity?.title = adapter.getPageTitle(page)
            }
        }
    }
}
