package net.komunan.komunantw.ui.main.home.tab

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.simple_recycler_view.*
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.repository.entity.Timeline

class HomeTabFragment: Fragment() {
    companion object {
        private const val PARAMETER_COLUMN_ID = "HomeTabFragment.PARAMETER_COLUMN_ID"

        @JvmStatic
        fun create(timeline: Timeline): Fragment {
            return HomeTabFragment().apply {
                arguments = Bundle().apply {
                    putLong(PARAMETER_COLUMN_ID, timeline.id)
                }
            }
        }
    }

    private val viewModel: HomeTabViewModel by lazy { makeViewModel() }
    private val adapter = HomeTabAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        container.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        container.adapter = adapter
        viewModel.tweets.observeOnNotNull(this) { tweets ->
            adapter.submitList(tweets)
        }
    }

    private fun makeViewModel(): HomeTabViewModel {
        val timelineId: Long = arguments!!.getLong(PARAMETER_COLUMN_ID)
        val factory = HomeTabViewModel.Factory(timelineId)
        return ViewModelProviders.of(this, factory).get(HomeTabViewModel::class.java)
    }
}
