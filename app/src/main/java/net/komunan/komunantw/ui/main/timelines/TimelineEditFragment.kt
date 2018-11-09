package net.komunan.komunantw.ui.main.timelines

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.edit_timeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.TWBaseFragment
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.extension.make
import net.komunan.komunantw.extension.observeOnNotNull
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.repository.entity.Timeline

class TimelineEditFragment: TWBaseFragment() {
    companion object {
        private const val PARAMETER_TIMELINE_ID = "TimelineEditFragment.PARAMETER_TIMELINE_ID"

        @JvmStatic
        fun create(timelineId: Long): TimelineEditFragment = TimelineEditFragment().apply {
            arguments = Bundle().apply {
                putLong(PARAMETER_TIMELINE_ID, timelineId)
            }
        }
    }

    private val timelineId by lazy { arguments!!.getLong(PARAMETER_TIMELINE_ID) }
    private val viewModel by lazy { makeViewModel(timelineId) }
    private val adapter by lazy { TimelineEditAdapter(timelineId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_timeline, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sources_container.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        sources_container.adapter = adapter
        viewModel.timeline.observeOnNotNull(this) {
            timeline_name_show.text = it?.name
        }
        viewModel.sources.observeOnNotNull(this) {
            adapter.submitList(it)
        }
        viewModel.editMode.observeOnNotNull(this) {
            timeline_name_show_container.visibility = if (it) View.GONE else View.VISIBLE
            timeline_name_edit_container.visibility = if (it) View.VISIBLE else View.GONE
        }
        timeline_name_show_edit.run {
            setImageDrawable(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_edit))
            setOnClickListener {
                timeline_name_edit.setText(timeline_name_show.text)
                viewModel.editMode.postValue(true)
            }
        }
        timeline_name_edit_save.run {
            setImageDrawable(GoogleMaterial.Icon.gmd_check.make(context).color(AppColor.GREEN))
            setOnClickListener {
                GlobalScope.launch {
                    Timeline.find(timelineId)?.apply { name = timeline_name_edit.text.toString() }?.save()
                }
                viewModel.editMode.postValue(false)
            }
        }
        timeline_name_edit_cancel.run {
            setImageDrawable(GoogleMaterial.Icon.gmd_cancel.make(context).color(AppColor.RED))
            setOnClickListener {
                viewModel.editMode.postValue(false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu == null) {
            return
        }
        menu.add(0, R.string.delete, 1, R.string.delete)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.string.delete -> {
                MaterialDialog(context!!)
                        .message(R.string.confirm_delete_timeline)
                        .positiveButton(R.string.do_delete) {
                            GlobalScope.launch(Dispatchers.Main) {
                                withContext(Dispatchers.Default) { Timeline.find(timelineId)?.delete() }
                                Transition.execute(Transition.Target.BACK)
                            }
                        }
                        .negativeButton(R.string.do_not_delete)
                        .show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun fragmentName(): String? {
        return string[R.string.timeline_edit]()
    }

    private fun makeViewModel(timelineId: Long): TimelineEditViewModel {
        val factory = TimelineEditViewModel.Factory(timelineId)
        return ViewModelProviders.of(this, factory).get(TimelineEditViewModel::class.java)
    }
}
