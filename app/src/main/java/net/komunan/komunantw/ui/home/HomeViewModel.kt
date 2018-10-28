package net.komunan.komunantw.ui.home

import net.komunan.komunantw.common.BaseViewModel
import net.komunan.komunantw.repository.entity.Timeline

internal class HomeViewModel: BaseViewModel() {
    fun columns() = Timeline.findAllAsync()
}
