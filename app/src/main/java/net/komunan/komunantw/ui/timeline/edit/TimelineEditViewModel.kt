package net.komunan.komunantw.ui.timeline.edit

import androidx.lifecycle.*
import io.objectbox.android.ObjectBoxLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.komunan.komunantw.common.combineLatest
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.core.repository.entity.Source_
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.core.repository.entity.Timeline_
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class TimelineEditViewModel(timelineId: Long) : TWBaseViewModel() {
    val timeline: LiveData<Timeline?> = Transformations.map(ObjectBoxLiveData(Timeline.query().apply {
        equal(Timeline_.id, timelineId)
    }.build()), List<Timeline>::firstOrNull)
    val sources = combineLatest(timeline, ObjectBoxLiveData(Source.query().apply {
        order(Source_.accountName)
        order(Source_.ordinal)
        order(Source_.label)
    }.build())) { timeline, sources ->
        return@combineLatest if (sources == null) {
            emptyList<Source>()
        } else {
            val actives = timeline?.sources?.map(Source::id)?.toHashSet() ?: emptySet<Long>()
            sources.map { source ->
                source.isActive = actives.contains(source.id)
                return@map source
            }
        }
    }
    val editMode = MutableLiveData<Boolean>()

    init {
        editMode.postValue(false)
    }

    fun updateName(name: String) = GlobalScope.launch(Dispatchers.Main) {
        timeline.value?.also { timeline ->
            timeline.name = name
            timeline.save()
        }
    }

    fun startEdit() {
        editMode.postValue(true)
    }

    fun finishEdit() {
        editMode.postValue(false)
    }

    class Factory(private val timelineId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == TimelineEditViewModel::class.java) {
                @Suppress("UNCHECKED_CAST")
                return TimelineEditViewModel(timelineId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}.")
        }
    }
}
