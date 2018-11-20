package net.komunan.komunantw.ui.source.list

import androidx.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class SourceListViewModel: TWBaseViewModel() {
    val sources: LiveData<List<Source>>
        get() = Source.dao.findAllAsync()
}
