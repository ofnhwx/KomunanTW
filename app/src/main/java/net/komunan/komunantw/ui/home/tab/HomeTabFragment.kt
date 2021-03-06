package net.komunan.komunantw.ui.home.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home_tab.*
import net.komunan.komunantw.R
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.ui.common.base.TWBaseFragment
import net.komunan.komunantw.ui.home.HomeActivity

class HomeTabFragment : TWBaseFragment() {
    companion object {
        private const val PARAMETER_TIMELINE_ID = "HomeTabFragment.PARAMETER_TIMELINE_ID"

        @JvmStatic
        fun create(timeline: Timeline): HomeTabFragment {
            return HomeTabFragment().apply {
                arguments = Bundle().apply {
                    putLong(PARAMETER_TIMELINE_ID, timeline.id)
                }
            }
        }
    }

    val timelineId: Long
        get() = arguments!!.getLong(PARAMETER_TIMELINE_ID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_tab, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        HomeTabAdapter().also { adapter ->
            container.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false).apply {
                recycleChildrenOnDetach = true
            }
            container.setRecycledViewPool((activity as? HomeActivity)?.viewPool)
            container.adapter = adapter
            viewModel().tweets.observe(this, Observer { tweets ->
                adapter.submitList(tweets)
            })
        }
    }

    private fun viewModel(): HomeTabViewModel {
        val factory = HomeTabViewModel.Factory(timelineId)
        return ViewModelProviders.of(this, factory).get(HomeTabViewModel::class.java)
    }
}
