package net.komunan.komunantw.ui.timeline

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.repository.entity.Column

class TimelineViewModel(app: Application): AndroidViewModel(app) {
    val columns: LiveData<List<Column>>
        get() = TWDatabase.instance.columnDao().findAll()
}
