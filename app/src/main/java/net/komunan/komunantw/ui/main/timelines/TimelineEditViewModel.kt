package net.komunan.komunantw.ui.main.timelines

import androidx.lifecycle.*
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.common.TWBaseViewModel

class TimelineEditViewModel(timelineId: Long): TWBaseViewModel() {
    val timeline = Timeline.dao.findAsync(timelineId)
    val sources = Source.dao.findAllWithActiveAsync(timelineId)
    val editMode = MutableLiveData<Boolean>()

    init {
        editMode.postValue(false)
    }

    class Factory(private val timelineId: Long): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == TimelineEditViewModel::class.java) {
                @Suppress("UNCHECKED_CAST")
                return TimelineEditViewModel(timelineId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}.")
        }
    }
}
