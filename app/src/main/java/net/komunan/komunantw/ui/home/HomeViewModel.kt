package net.komunan.komunantw.ui.home

import androidx.lifecycle.LiveData
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.ext.TimelineExt
import net.komunan.komunantw.ui.common.base.TWBaseViewModel
import java.util.*

class HomeViewModel: TWBaseViewModel() {
    private var isRunning = false
    private var timer = Timer()

    val timelines: LiveData<List<TimelineExt>>
        get() = Timeline.dao.findAllAsync()

    fun startUpdate() {
        if (isRunning) {
            return
        }
        isRunning = true
        timer.schedule(object: TimerTask() {
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
