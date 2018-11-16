package net.komunan.komunantw.ui.main.sources

import androidx.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.ui.common.base.TWBaseViewModel

class SourcesViewModel: TWBaseViewModel() {
    val sources: LiveData<List<Source>>
        get() = Source.dao.findAllAsync()
}
