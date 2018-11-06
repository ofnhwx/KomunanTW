package net.komunan.komunantw.ui.main.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_home.*
import net.komunan.komunantw.Preference
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.common.TWBaseFragment
import net.komunan.komunantw.service.TwitterService

class HomeFragment: TWBaseFragment() {
    companion object {
        @JvmStatic
        fun create() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
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
        action_tweet.run {
            setImageDrawable(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_edit).color(Color.GRAY))
            setOnClickListener { TwitterService.doOfficialTweet() }
        }
    }

    private fun updateTitle(page: Int) {
        activity?.title = pager.adapter?.getPageTitle(page)
    }
}
