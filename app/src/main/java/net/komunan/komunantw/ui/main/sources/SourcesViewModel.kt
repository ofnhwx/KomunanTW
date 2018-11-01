package net.komunan.komunantw.ui.main.sources

import android.arch.lifecycle.LiveData
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.ui.common.TWBaseViewModel

class SourcesViewModel: TWBaseViewModel() {
    val sources: LiveData<List<Source>>
        get() = Source.findAllAsync()
}
