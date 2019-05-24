package net.komunan.komunantw.ui.home

import androidx.lifecycle.LiveData
import io.objectbox.android.ObjectBoxLiveData
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.core.repository.entity.Timeline_
import net.komunan.komunantw.core.service.TwitterService
import net.komunan.komunantw.ui.common.base.TWBaseViewModel
import java.util.*

class HomeViewModel : TWBaseViewModel() {
    private var isRunning = false
    private var timer = Timer()

    val timelines: LiveData<List<Timeline>>
        get() = ObjectBoxLiveData(Timeline.box.query().order(Timeline_.position).build())

    fun startUpdate() {
        if (isRunning) {
            return
        }
        isRunning = true
        timer.schedule(object : TimerTask() {
            override fun run() {
                TwitterService.fetchTweets()
            }
        }, 0, Preference.fetchIntervalMillis)
    }

    override fun onCleared() {
        super.onCleared()
        if (isRunning) {
            timer.cancel()
        }
    }
}
