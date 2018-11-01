package net.komunan.komunantw.ui.main.home.tab

import android.arch.lifecycle.*
import android.arch.paging.PagedList
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.ui.common.TWBaseViewModel

class HomeTabViewModel(private val timelineId: Long): TWBaseViewModel() {
    private val sources: LiveData<List<Source>>
        get() = Source.findByTimelineIdAsync(timelineId)
    private val sourceIds: LiveData<List<Long>>
        get() = Transformations.map(sources) { sources -> sources.map { it.id } }

    val tweets: LiveData<PagedList<TweetDetail>>
        get() = Transformations.switchMap(sourceIds) { Tweet.findBySourceIdsAsync(it) }

    class Factory(private val timelineId: Long): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == HomeTabViewModel::class.java) {
                @Suppress("UNCHECKED_CAST")
                return HomeTabViewModel(timelineId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}.")
        }
    }
}
