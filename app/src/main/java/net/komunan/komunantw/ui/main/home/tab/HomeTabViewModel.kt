package net.komunan.komunantw.ui.main.home.tab

import androidx.lifecycle.*
import androidx.paging.PagedList
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.common.TWBaseViewModel

class HomeTabViewModel(private val timelineId: Long): TWBaseViewModel() {
    private val timeline: LiveData<Timeline?>
        get() = Timeline.findAsync(timelineId)
    private val sources: LiveData<List<Source>>
        get() = Transformations.switchMap(timeline) {
            if (it == null) {
                MutableLiveData<List<Source>>()
            } else {
                Source.findByTimelineAsync(it)
            }
        }

    val tweets: LiveData<PagedList<TweetDetail>>
        get() = Transformations.switchMap(sources) { Tweet.findBySourcesAsync(it) }

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
