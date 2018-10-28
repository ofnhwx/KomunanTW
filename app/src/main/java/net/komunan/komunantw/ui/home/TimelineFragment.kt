package net.komunan.komunantw.ui.home

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.work.WorkManager
import net.komunan.komunantw.common.observeOnNotNull
import net.komunan.komunantw.repository.entity.Timeline
import org.jetbrains.anko.AnkoContext

class TimelineFragment: Fragment() {
    companion object {
        private const val PARAMETER_COLUMN_ID = "TimelineFragment.PARAMETER_COLUMN_ID"

        @JvmStatic
        fun create(timeline: Timeline)= TimelineFragment().apply {
            arguments = Bundle().apply {
                putLong(PARAMETER_COLUMN_ID, timeline.id)
            }
        }
    }

    private val viewModel by lazy {
        val timelineId: Long = arguments!!.getLong(PARAMETER_COLUMN_ID)
        val factory = TimelineViewModel.Factory(timelineId)
        ViewModelProviders.of(this, factory).get(TimelineViewModel::class.java)
    }
    private val ui = TimelineUI()
    private val adapter = TimelineAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ui.run {
            container.adapter = adapter
        }
        viewModel.run {
            tweets().observeOnNotNull(this@TimelineFragment) { tweets ->
                adapter.submitList(tweets)
            }
        }
        WorkManager.getInstance()
    }
}
