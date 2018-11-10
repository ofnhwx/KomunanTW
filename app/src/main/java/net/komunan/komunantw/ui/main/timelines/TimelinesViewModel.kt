package net.komunan.komunantw.ui.main.timelines

import androidx.lifecycle.LiveData
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.common.TWBaseViewModel
import net.komunan.komunantw.extension.string

class TimelinesViewModel: TWBaseViewModel() {
    val timelines: LiveData<List<Timeline>>
        get() = Timeline.dao.findAllAsync()

    fun addTimeline() {
        Timeline(string[R.string.new_timeline]()).save()
    }
}
