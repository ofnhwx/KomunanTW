package net.komunan.komunantw.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.simple_view_pager.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.extension.observeOnNotNull
import net.komunan.komunantw.ui.common.base.TWBaseFragment

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

        val activityViewModel = activityViewModel(HomeActivityViewModel::class.java)

        viewModel(HomeViewModel::class.java).also { viewModel ->
            viewModel.timelines.observeOnNotNull(this@HomeFragment) { timelines ->
                container.adapter = HomeAdapter(childFragmentManager, timelines)
                val currentPage = Preference.currentPage
                if (currentPage > 0 && currentPage < timelines.size) {
                    activityViewModel.setPage(currentPage)
                }
            }
            viewModel.startUpdate()
        }

        activityViewModel.currentPage.observeOnNotNull(this) { currentPage ->
            container.setCurrentItem(currentPage, false)
            activity?.title = container.adapter?.getPageTitle(currentPage)
        }

        container.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) { activityViewModel.setPage(position) }
        })
    }
}
