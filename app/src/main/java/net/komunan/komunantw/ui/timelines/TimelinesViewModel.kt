package net.komunan.komunantw.ui.timelines

import net.komunan.komunantw.common.BaseViewModel
import net.komunan.komunantw.repository.entity.Timeline

internal class TimelinesViewModel: BaseViewModel() {
    fun timelines() = Timeline.findAllAsync()
}
