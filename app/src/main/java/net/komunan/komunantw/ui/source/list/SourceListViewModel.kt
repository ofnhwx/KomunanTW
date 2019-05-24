package net.komunan.komunantw.ui.source.list

import androidx.lifecycle.LiveData
import io.objectbox.android.ObjectBoxLiveData
import net.komunan.komunantw.core.repository.entity.Account_
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.core.repository.entity.Source_
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class SourceListViewModel : TWBaseViewModel() {
    val sources: LiveData<List<Source>>
        get() {
            return ObjectBoxLiveData(Source.query().apply {
                order(Source_.accountName)
                order(Source_.ordinal)
                order(Source_.label)
            }.build())
        }
}
