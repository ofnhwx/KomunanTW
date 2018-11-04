package net.komunan.komunantw.ui.main.timelines

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.edit_timeline.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.observeOnNotNull
import net.komunan.komunantw.ui.common.TWBaseFragment

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_timeline, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.timeline.observeOnNotNull(this) {
            timeline_name_show.text = it.name
        }
        viewModel.sources.observeOnNotNull(this) {
            sources_container.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            sources_container.adapter = TimelineEditAdapter(timelineId, it)
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
            setImageDrawable(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_check).color(Color.GREEN))
            setOnClickListener {
                GlobalScope.launch {  viewModel.updateName(timeline_name_edit.text.toString()) }
                viewModel.editMode.postValue(false)
            }
        }
        timeline_name_edit_cancel.run {
            setImageDrawable(IconicsDrawable(context).icon(GoogleMaterial.Icon.gmd_cancel).color(Color.RED))
            setOnClickListener {
                viewModel.editMode.postValue(false)
            }
        }
    }

    private fun makeViewModel(timelineId: Long): TimelineEditViewModel {
        val factory = TimelineEditViewModel.Factory(timelineId)
        return ViewModelProviders.of(this, factory).get(TimelineEditViewModel::class.java)
    }
}
