package net.komunan.komunantw.ui.timeline.list

import io.objectbox.android.ObjectBoxLiveData
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.core.repository.entity.Timeline_
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class TimelineListViewModel : TWBaseViewModel() {
    val timelines = ObjectBoxLiveData(Timeline.query().apply {
        order(Timeline_.position)
    }.build())

    fun addTimeline() {
        Timeline(string[R.string.fragment_timeline_list_new_name]()).save()
    }
}
