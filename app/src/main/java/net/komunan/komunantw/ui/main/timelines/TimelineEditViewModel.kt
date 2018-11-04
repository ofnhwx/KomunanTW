package net.komunan.komunantw.ui.main.timelines

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.ui.common.TWBaseViewModel

class TimelineEditViewModel(private val timelineId: Long): TWBaseViewModel() {
    val timeline = Timeline.findAsync(timelineId)
    val sources = Transformations.switchMap(timeline) { Source.findForTimelineEditAsync(it) }
    val editMode = MutableLiveData<Boolean>()

    init {
        editMode.postValue(false)
    }

    suspend fun updateName(name: String) = process {
        timeline.value?.also {
            it.name = name
        }?.save()
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
