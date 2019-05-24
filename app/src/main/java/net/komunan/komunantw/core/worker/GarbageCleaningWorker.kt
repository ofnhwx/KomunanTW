package net.komunan.komunantw.core.worker

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.ajalt.timberkt.i
import io.objectbox.kotlin.inValues
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.core.repository.entity.Source_
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.core.repository.entity.cache.Tweet
import net.komunan.komunantw.core.repository.entity.cache.Tweet_
import net.komunan.komunantw.core.repository.entity.cache.User
import net.komunan.komunantw.core.repository.entity.cache.User_
import kotlin.math.max

class GarbageCleaningWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        @JvmStatic
        fun request(): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(GarbageCleaningWorker::class.java).build()
        }
    }

    override fun doWork(): Result {
        i { "---------- (begin: GarbageCleaningWorker#doWork) ----------" }
        deleteInactiveTweet()
        deleteOldTweet()
        deleteUnnecessaryTweet()
        deleteUnnecessaryUser()
        addMissingMarks()
        i { "---------- (end  : GarbageCleaningWorker#doWork) ----------" }
        return Result.success()
    }

    /**
     * アクティブでないソースに紐づくツイートを削除
     */
    private fun deleteInactiveTweet() = ObjectBox.get().runInTx {
        for (source in inactiveSources()) {
            val query = Tweet.query().apply {
                link(Tweet_.sources).equal(Source_.id, source.id)
            }.build()
            val deleted = query.count()
            for (tweet in query.find()) {
                tweet.sources.remove(source)
                tweet.sources.applyChangesToDb()
            }
            i { "delete: tweet(source:inactive) { source=${source.id}, count=$deleted }" }
        }
    }

    /**
     * ソース毎に古いツイートを削除
     */
    private fun deleteOldTweet() = ObjectBox.get().runInTx {
        for (source in activeSources()) {
            val query = Tweet.query().apply {
                link(Tweet_.sources).equal(Source_.id, source.id)
            }.build()
            val count = query.count()
            val deleted = max(count - Preference.fetchCount, 0)
            for (tweet in query.find(Preference.fetchCount.toLong(), count)) {
                tweet.sources.remove(source)
                tweet.sources.applyChangesToDb()
            }
            i { "delete: tweet(source:old) { source=${source.id}, count=$deleted }" }
        }
    }

    /**
     * 他から参照されなくなったツイートを削除
     */
    private fun deleteUnnecessaryTweet() = ObjectBox.get().runInTx {
        val query = Tweet.query().apply {
            filter { tweet -> tweet.sources.isEmpty() }
        }.build()
        var deleted = 0
        for (tweet in query.find()) {
            Tweet.box.remove(tweet)
            deleted += 1
        }
        i { "delete: tweet(unnecessary) { count=$deleted }" }
    }

    /**
     * 他から参照されなくなったユーザーを削除
     */
    private fun deleteUnnecessaryUser() = ObjectBox.get().runInTx {
        val validIds = Tweet.query().build().property(Tweet_.userId).distinct().findLongs()
                .plus(Tweet.query().build().property(Tweet_.replyUserId).distinct().findLongs())
                .plus(Tweet.query().build().property(Tweet_.rtUserId).distinct().findLongs())
                .plus(Tweet.query().build().property(Tweet_.qtUserId).distinct().findLongs())
                .distinct()
                .toLongArray()
        val deleted = User.query().apply {
            notIn(User_.id, validIds)
        }.build().remove()
        i { "delete: user(unnecessary) { count=$deleted }" }
    }

    /**
     * 未取得ツイートのマークを追加
     */
    private fun addMissingMarks() = ObjectBox.get().runInTx {
        for (source in activeSources()) {
            val tweetId = source.oldestTweetId()
            if (tweetId != Tweet.INVALID_ID) {
                source.addMissing(tweetId)
                i { "set: missing { source=${source.id}, tweet=$tweetId }" }
            }
        }
    }

    private fun activeSources(): List<Source> {
        val activeIds = activeSourceIds().toLongArray()
        return Source.query().apply {
            inValues(Source_.id, activeIds)
        }.build().find()
    }

    private fun inactiveSources(): List<Source> {
        val activeIds = activeSourceIds().toLongArray()
        return Source.query().apply {
            notIn(Source_.id, activeIds)
        }.build().find()
    }

    private fun activeSourceIds(): List<Long> {
        return Timeline.query()
                .build()
                .find()
                .flatMap(Timeline::sources)
                .map(Source::id)
                .distinct()
    }
}
