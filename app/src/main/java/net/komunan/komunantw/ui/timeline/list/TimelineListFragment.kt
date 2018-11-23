package net.komunan.komunantw.ui.timeline.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.simple_recycler_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.ext.TimelineExt
import net.komunan.komunantw.ui.common.base.TWBaseListAdapter
import net.komunan.komunantw.ui.common.base.TWBaseListFragment
import net.komunan.komunantw.ui.timeline.edit.TimelineEditActivity

class TimelineListFragment: TWBaseListFragment<TimelineExt, TimelineListAdapter.ViewHolder>() {
    companion object {
        @JvmStatic
        fun create() = TimelineListFragment()
    }

    override val name: String?
        get() = string[R.string.fragment_timeline_list]()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu == null) {
            return
        }
        menu.add(0, R.string.add, 1, R.string.add)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.string.add -> GlobalScope.launch(Dispatchers.Default) {
                viewModel(TimelineListViewModel::class.java).addTimeline()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun adapter(): TWBaseListAdapter<TimelineExt, TimelineListAdapter.ViewHolder> {
        return TimelineListAdapter().apply {
            activateItemTouchHelper(container) { itemId, _, to ->
                withContext(Dispatchers.Default) {
                    Timeline.dao.find(itemId)?.run { moveTo(to) }
                }
            }
            onClickEvent = { timelineId ->
                startActivity(TimelineEditActivity.createIntent(timelineId))
            }
        }
    }

    override fun layoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    override fun items(): LiveData<List<TimelineExt>> {
        return viewModel(TimelineListViewModel::class.java).timelines
    }
}
