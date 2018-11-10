package net.komunan.komunantw.ui.main.timelines

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.d
import kotlinx.android.synthetic.main.simple_recycler_view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.TWBaseFragment
import net.komunan.komunantw.extension.observeOnNotNull
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.repository.entity.Timeline

class TimelinesFragment: TWBaseFragment() {
    companion object {
        @JvmStatic
        fun create() = TimelinesFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(TimelinesViewModel::class.java) }
    private val adapter = TimelinesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.simple_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        container.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        container.adapter = adapter
        viewModel.timelines.observeOnNotNull(this) { timelines ->
            adapter.submitList(timelines.toMutableList())
        }
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            var dragFrom: Int = -1
            var dragTo: Int = -1
            var timelineId: Long = 0

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (dragFrom == -1) {
                    dragFrom = fromPosition
                    timelineId = container.adapter!!.getItemId(dragFrom)
                }
                dragTo = toPosition
                d { "Timeline moving. id=$timelineId position={ from=${dragFrom + 1}, to=${toPosition + 1} }" }
                adapter.onItemMoved(fromPosition, toPosition)
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.Default) {
                            Timeline.dao.find(timelineId)?.run { moveTo(dragTo + 1) }
                        }
                        dragFrom = -1
                        dragTo = -1
                    }
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }).attachToRecyclerView(container)
    }
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu == null) {
            return
        }
        menu.add(0, R.string.add, 1, R.string.add)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.string.add -> {
                GlobalScope.launch {
                    viewModel.addTimeline()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun fragmentName(): String? {
        return string[R.string.timeline_list]()
    }
}
