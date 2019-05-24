package net.komunan.komunantw.ui.home.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import io.objectbox.android.ObjectBoxDataSource
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.inValues
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.core.repository.entity.Source_
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.core.repository.entity.Timeline_
import net.komunan.komunantw.core.repository.entity.cache.Tweet
import net.komunan.komunantw.core.repository.entity.cache.Tweet_
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class HomeTabViewModel(private val timelineId: Long) : TWBaseViewModel() {
    private val timeline: LiveData<Timeline?>
        get() = Transformations.map(ObjectBoxLiveData(Timeline.query().apply {
            equal(Timeline_.id, timelineId)
        }.build()), List<Timeline>::firstOrNull)

    private val sourceIds: LiveData<List<Long>>
        get() = Transformations.map(timeline) { it?.sources?.map(Source::id) ?: emptyList() }

    val tweets: LiveData<PagedList<Tweet>>
        get() = Transformations.switchMap(sourceIds) {
            LivePagedListBuilder(ObjectBoxDataSource.Factory(Tweet.query().apply {
                link(Tweet_.sources).inValues(Source_.id, it.toLongArray())
                orderDesc(Tweet_.id)
            }.build()), Preference.pageSize).build()
        }

    class Factory(private val timelineId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == HomeTabViewModel::class.java) {
                @Suppress("UNCHECKED_CAST")
                return HomeTabViewModel(timelineId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}.")
        }
    }
}
