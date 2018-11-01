package net.komunan.komunantw.ui.main.home

import android.arch.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.TWBaseViewModel

class HomeViewModel: TWBaseViewModel() {
    val timelines: LiveData<List<Timeline>>
        get() = Timeline.findAllAsync()
}
