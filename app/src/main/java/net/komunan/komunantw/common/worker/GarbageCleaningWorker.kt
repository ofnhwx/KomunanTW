package net.komunan.komunantw.common.worker

import android.content.Context
import androidx.work.*
import com.github.ajalt.timberkt.i
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.extension.TransactionTarget
import net.komunan.komunantw.common.extension.transaction
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.User

class GarbageCleaningWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    companion object {
        @JvmStatic
        fun request(): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(GarbageCleaningWorker::class.java).build()
        }
    }

    override fun doWork(): Result {
        deleteUnnecessaryTweetSource() // 存在しなくなったソースに紐づく情報を削除
        deleteOldTweetSource()         // ソース毎に古いツイートを削除
        deleteUnnecessaryTweet()       // 他から参照されなくなったツイートを削除
        deleteUnnecessaryUser()        // 他から参照されなくなったユーザを削除
        addMissingMarks()              // 未取得ツイートのマークを追加
        return Result.SUCCESS
    }

    private fun deleteUnnecessaryTweetSource() = transaction(TransactionTarget.WITH_CACHE) {
        val validIds = Source.dao.findSourceIds().toSet()
        for (sourceId in Tweet.sourceDao.findSourceIds()) {
            if (!validIds.contains(sourceId)) {
                val deleted = Tweet.sourceDao.deleteBySourceId(sourceId)
                i { "delete: tweet_source { source_id=$sourceId, count=$deleted }" }
            }
        }
    }

    private fun deleteOldTweetSource() = transaction(TransactionTarget.CACHE_ONLY) {
        for (sourceId in Tweet.sourceDao.findSourceIds()) {
            val deleted = Tweet.sourceDao.deleteOld(sourceId, Preference.fetchCount)
            i { "delete: tweet(old) { source_id=$sourceId, count=$deleted }" }
        }
    }

    private fun deleteUnnecessaryTweet() = transaction(TransactionTarget.CACHE_ONLY) {
        val deleted = Tweet.dao.deleteUnnecessary()
        i { "delete: tweet(unnecessary) { count=$deleted }" }
    }

    private fun deleteUnnecessaryUser() = transaction(TransactionTarget.CACHE_ONLY) {
        val deleted = User.dao.deleteUnnecessary()
        i { "delete: user(unnecessary) { count=$deleted }" }
    }

    private fun addMissingMarks() = transaction(TransactionTarget.CACHE_ONLY) {
        for (sourceId in Tweet.sourceDao.findSourceIds()) {
            val minId = Tweet.sourceDao.minIdBySourceId(sourceId)
            Tweet.sourceDao.addMissing(sourceId, minId)
            i { "add: tweet_source(missing) { source_id=$sourceId, tweet_id=$minId }" }
        }
    }
}
