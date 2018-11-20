package net.komunan.komunantw.ui.timeline.edit

import android.os.Bundle
import android.view.*
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
import net.komunan.komunantw.common.extension.make
import net.komunan.komunantw.common.extension.observeOnNotNull
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.base.TWBaseFragment

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

    override val name: String?
        get() = string[R.string.fragment_timeline_edit]()

    val timelineId: Long
        get() = arguments!!.getLong(PARAMETER_TIMELINE_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_timeline, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = viewModel()
        val adapter = TimelineEditAdapter().apply {
            onClickEvent = { source ->
                GlobalScope.launch(Dispatchers.Default) {
                    if (source.isActive) {
                        Timeline.dao.find(timelineId)?.delSource(source)
                    } else {
                        Timeline.dao.find(timelineId)?.addSource(source)
                    }
                }
            }
        }

        sources_container.apply {
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            this.adapter = adapter
        }

        viewModel.apply {
            timeline.observeOnNotNull(this@TimelineEditFragment) {
                timeline_name_show.text = it?.name
            }
            sources.observeOnNotNull(this@TimelineEditFragment) {
                adapter.submitList(it)
            }
            editMode.observeOnNotNull(this@TimelineEditFragment) {
                timeline_name_show_container.visibility = if (it) View.GONE else View.VISIBLE
                timeline_name_edit_container.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        timeline_name_show_edit.apply {
            setImageDrawable(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_edit))
            setOnClickListener {
                timeline_name_edit.setText(timeline_name_show.text)
                viewModel.startEdit()
            }
        }

        timeline_name_edit_save.apply {
            setImageDrawable(GoogleMaterial.Icon.gmd_check.make(context).color(AppColor.GREEN))
            setOnClickListener {
                GlobalScope.launch(Dispatchers.Default) { viewModel.updateName(timeline_name_edit.text.toString()) }
                viewModel.finishEdit()
            }
        }

        timeline_name_edit_cancel.apply {
            setImageDrawable(GoogleMaterial.Icon.gmd_cancel.make(context).color(AppColor.RED))
            setOnClickListener { viewModel.finishEdit() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (menu == null) {
            return
        }
        menu.add(0, R.string.delete, 1, R.string.delete)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.string.delete -> MaterialDialog(context!!).apply {
                message(R.string.confirm_delete_timeline)
                positiveButton(R.string.do_delete) {
                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.Default) { Timeline.dao.find(timelineId)?.delete() }
                        activity?.finish()
                    }
                }
                negativeButton(R.string.do_not_delete)
            }.show()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun viewModel(): TimelineEditViewModel {
        val factory = TimelineEditViewModel.Factory(timelineId)
        return viewModel(TimelineEditViewModel::class.java, factory)
    }
}
