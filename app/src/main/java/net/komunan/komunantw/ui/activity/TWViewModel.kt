package net.komunan.komunantw.ui.activity

import androidx.work.WorkManager
import net.komunan.komunantw.Preference
import net.komunan.komunantw.common.BaseViewModel
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.worker.FetchTweetsWorker
import java.util.*

class TWViewModel: BaseViewModel() {
    private var timer = Timer()
    fun startUpdate() {
        timer.schedule(object: TimerTask() {
            override fun run() {
                val requests = Source.findEnabled().map {
                    FetchTweetsWorker.request(it.id, FetchTweetsWorker.FetchType.NEW)
                }
                if (requests.any()) {
                    WorkManager.getInstance().enqueue(requests)
                }
            }
        }, 0, Preference.fetchIntervalMillis)
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}
