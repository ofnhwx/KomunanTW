package net.komunan.komunantw.ui.main.timelines

import androidx.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.TWBaseViewModel

class TimelinesViewModel: TWBaseViewModel() {
    val timelines: LiveData<List<Timeline>>
        get() = Timeline.findAllAsync()
}
