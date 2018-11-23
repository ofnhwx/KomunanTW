package net.komunan.komunantw.ui.timeline.list

import androidx.lifecycle.LiveData
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.base.TWBaseViewModel
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.ext.TimelineExt

class TimelineListViewModel: TWBaseViewModel() {
    val timelines: LiveData<List<TimelineExt>>
        get() = Timeline.dao.findAllAsync()

    fun addTimeline() {
        Timeline(string[R.string.fragment_timeline_list_new_name]()).save()
    }
}
