package net.komunan.komunantw.ui.home.tab

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.ui.common.base.TWBaseViewModel
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.repository.entity.ext.TweetSourceExt

class HomeTabViewModel(private val timelineId: Long): TWBaseViewModel() {
    private val timeline: LiveData<Timeline?>
        get() = Timeline.dao.findAsync(timelineId)
    private val sources: LiveData<List<TimelineSource>>
        get() = Transformations.switchMap(timeline) {
            if (it == null) {
                MutableLiveData<List<TimelineSource>>()
            } else {
                Timeline.sourceDao.findByTimelineIdAsync(it.id)
            }
        }
    private val sourceIds: LiveData<List<Long>>
        get() = Transformations.map(sources) { it.map(TimelineSource::sourceId) }

    val tweetSources: LiveData<PagedList<TweetSourceExt>>
        get() = Transformations.switchMap(sourceIds) {
            LivePagedListBuilder(Tweet.sourceDao.findBySourceIdsAsync(it), Preference.pageSize).build()
        }

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
