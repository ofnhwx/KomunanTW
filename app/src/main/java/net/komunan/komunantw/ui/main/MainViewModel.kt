package net.komunan.komunantw.ui.main

import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.ui.common.base.TWBaseViewModel
import java.util.*

class MainViewModel: TWBaseViewModel() {
    private var isRunning = false
    private var timer = Timer()

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
