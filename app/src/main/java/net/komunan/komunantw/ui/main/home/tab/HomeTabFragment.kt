package net.komunan.komunantw.ui.main.home.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.simple_recycler_view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.extension.observeOnNotNull
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.common.TWBaseFragment

class HomeTabFragment: TWBaseFragment() {
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

    private val viewModel: HomeTabViewModel by lazy { makeViewModel(arguments!!.getLong(PARAMETER_TIMELINE_ID)) }
    private val adapter = HomeTabAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        container.layoutManager = layoutManager
        container.adapter = adapter
        viewModel.tweets.observeOnNotNull(this) { tweets ->
            val positionIndex = layoutManager.findFirstVisibleItemPosition()
            val positionOffset = container.getChildAt(0)?.let { it.top - container.paddingTop } ?: 0
            adapter.submitList(tweets) {
                layoutManager.scrollToPositionWithOffset(positionIndex, positionOffset)
            }
        }
    }

    private fun makeViewModel(timelineId: Long): HomeTabViewModel {
        val factory = HomeTabViewModel.Factory(timelineId)
        return ViewModelProviders.of(this, factory).get(HomeTabViewModel::class.java)
    }

    override fun fragmentName(): String? {
        return null
    }
}
