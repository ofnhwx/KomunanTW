package net.komunan.komunantw.ui.main.home.tab

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.komunan.komunantw.databinding.SimpleRecyclerViewBinding
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
    private lateinit var binding: SimpleRecyclerViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return SimpleRecyclerViewBinding.inflate(inflater, container, false).apply {
            binding = this
            binding.container.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.container.adapter = adapter
        }.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
