package net.komunan.komunantw.ui.timeline

import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.komunan.komunantw.common.BaseViewModel
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Tweet

internal class TimelineViewModel(val timelineId: Long): BaseViewModel() {
    fun sources() = Source.findByTimelineIdAsync(timelineId)

    fun tweets() = Transformations.switchMap(sources()) { sources ->
        Tweet.findBySourceIdsAsync(sources.map { it.id })
    }!!

    class Factory(private val timelineId: Long): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == TimelineViewModel::class.java) {
                @Suppress("UNCHECKED_CAST")
                return TimelineViewModel(timelineId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}.")
        }
    }
}
