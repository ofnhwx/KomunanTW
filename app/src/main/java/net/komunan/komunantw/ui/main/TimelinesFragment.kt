package net.komunan.komunantw.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.ui.common.SimpleListAdapter
import net.komunan.komunantw.ui.common.TWBaseViewModel
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.string
import net.komunan.komunantw.repository.entity.Timeline
import org.jetbrains.anko.*

class TimelinesFragment: Fragment() {
    companion object {
        @JvmStatic
        fun create(): Fragment = TimelinesFragment()
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(TimelinesViewModel::class.java) }
    private val ui = TimelinesUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ui.createView(AnkoContext.create(context!!, this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.run {
            timelines.observeOnNotNull(this@TimelinesFragment) { timelines ->
                ui.timelines.adapter = TimelinesAdapter(timelines)
            }
        }
    }

    private class TimelinesViewModel: TWBaseViewModel() {
        val timelines: LiveData<List<Timeline>>
            get() = Timeline.findAllAsync()
    }

    private class TimelinesAdapter internal constructor(timelines: List<Timeline>): SimpleListAdapter<Timeline>(timelines) {
        override fun newView(position: Int, parent: ViewGroup): View {
            TimelineUI().let { ui ->
                return ui.createView(AnkoContext.create(parent.context, parent)).also { view ->
                    view.tag = ui
                }
            }
        }

        override fun bindView(view: View, position: Int) {
            (view.tag as TimelineUI).bind(items[position])
        }

        override fun getItemId(position: Int): Long {
            return items[position].id
        }
    }

    private class TimelinesUI: AnkoComponent<TimelinesFragment> {
        lateinit var timelines: ListView

        override fun createView(ui: AnkoContext<TimelinesFragment>): View = with(ui) {
            timelines = listView {}
            return@with timelines
        }
    }

    private class TimelineUI: AnkoComponent<ViewGroup> {
        lateinit var name: TextView
        lateinit var description: TextView

        override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
            verticalLayout {
                name = textView().lparams(matchParent, wrapContent)
                description = textView().lparams(matchParent, wrapContent)
            }
        }

        fun bind(timeline: Timeline) {
            launch(UI) {
                val count = async(CommonPool) { timeline.sourceCount() }
                name.text = timeline.name
                description.text = R.string.format_timeline_srouce_count.string(count.await().toString())
            }
        }
    }
}
