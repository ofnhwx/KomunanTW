package net.komunan.komunantw.ui.main.home

import androidx.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class HomeViewModel: TWBaseViewModel() {
    val timelines: LiveData<List<Timeline>>
        get() = Timeline.dao.findAllAsync()
}
