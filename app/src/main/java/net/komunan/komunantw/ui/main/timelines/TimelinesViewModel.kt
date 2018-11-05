package net.komunan.komunantw.ui.main.timelines

import androidx.lifecycle.LiveData
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.string
import net.komunan.komunantw.common.TWBaseViewModel

class TimelinesViewModel: TWBaseViewModel() {
    val timelines: LiveData<List<Timeline>>
        get() = Timeline.findAllAsync()

    fun addTimeline() {
        Timeline(R.string.new_timeline.string()).save()
    }
}
