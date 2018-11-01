package net.komunan.komunantw.ui.main

import net.komunan.komunantw.Preference
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.ui.common.TWBaseViewModel
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
