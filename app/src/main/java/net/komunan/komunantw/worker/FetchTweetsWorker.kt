package net.komunan.komunantw.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.Preference
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.service.TwitterService
import twitter4j.Paging
import twitter4j.Query

class FetchTweetsWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    companion object {
        private const val PARAMETER_SOURCE_ID = "FetchTweetsWorker.PARAMETER_SOURCE_ID"
        private const val PARAMETER_FETCH_TYPE = "FetchTweetsWorker.PARAMETER_FETCH_TYPE"
        private const val PARAMETER_IS_INTERACTIVE = "FetchTweetsWorker.PARAMETER_IS_INTERACTIVE"
        private const val PARAMETER_TARGET_TWEET_ID = "FetchTweetsWorker.PARAMETER_TARGET_TWEET_ID"

        @JvmStatic
        fun request(sourceId: Long, fetchType: FetchType = FetchType.NEW, isInteractive: Boolean = false, targetTweetId: Long = 0): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(FetchTweetsWorker::class.java)
                    .setInputData(Data.Builder().apply {
                        putLong(PARAMETER_SOURCE_ID, sourceId)
                        putString(PARAMETER_FETCH_TYPE, fetchType.toString())
                        putBoolean(PARAMETER_IS_INTERACTIVE, isInteractive)
                        putLong(PARAMETER_TARGET_TWEET_ID, targetTweetId)
                    }.build()).build()
        }
    }

    enum class FetchType {
        NEW,
        MISSING,
        OLDER,
    }

    private val source by lazy { Source.find(inputData.getLong(PARAMETER_SOURCE_ID, 0))!! }
    private val fetchType by lazy { FetchType.valueOf(inputData.getString(PARAMETER_FETCH_TYPE)!!) }
    private val isInteractive by lazy { inputData.getBoolean(PARAMETER_IS_INTERACTIVE, false) }
    private val twitter by lazy { TwitterService.twitter(source.account().credential()) }

    private val tweetsEmpty by lazy { source.tweetCount() == 0L }
    private val maxTweetId by lazy { source.maxTweetId() }
    private val minTweetId by lazy { source.minTweetId() }
    private val targetTweetId by lazy { inputData.getLong(PARAMETER_TARGET_TWEET_ID, 0) }
    private val prevTweetId by lazy { source.prevTweetId(targetTweetId) }

    override fun doWork(): Result {
        d {
            when {
                tweetsEmpty -> "fetch(${source.id}): type=$fetchType, empty"
                targetTweetId != 0L -> "fetch(${source.id}): type=$fetchType, max=$maxTweetId, min=$minTweetId, target=$targetTweetId, prev=$prevTweetId"
                else -> "fetch(${source.id}): type=$fetchType, max=$maxTweetId, min=$minTweetId"
            }
        }

        if (tweetsEmpty && fetchType != FetchType.NEW) {
            w { "fetch(${source.id}): If the tweet associated with the source is empty, only FetchType.NEW is allowed."}
            return Result.FAILURE
        }

        if (!isInteractive && (source.fetchAt + Preference.fetchIntervalMillis * 0.8) > System.currentTimeMillis()) {
            d { "fetch(${source.id}): skip=[elapsed=${System.currentTimeMillis() - source.fetchAt}, required=${Preference.fetchIntervalMillis * 0.8}]" }
            return Result.SUCCESS
        }

        val result = when (Source.SourceType.valueOf(source.type)) {
            Source.SourceType.HOME -> twitter.getHomeTimeline(makePaging())
            Source.SourceType.MENTION -> twitter.getMentionsTimeline(makePaging())
            Source.SourceType.RETWEET -> twitter.getRetweetsOfMe(makePaging())
            Source.SourceType.USER -> twitter.getUserTimeline(makePaging())
            Source.SourceType.LIST -> twitter.getUserListStatuses(source.listId, makePaging())
            Source.SourceType.SEARCH -> twitter.search(makeQuery()).tweets
        }
        val tweets = result.map { Tweet(it) }
        val users = result.asSequence().map { User(it.user) }.distinctBy { it.id }.toList()

        if (tweets.isEmpty()) {
            d { "fetch(${source.id}): count=0"}
            if (fetchType == FetchType.MISSING) {
                Tweet.updateHasMissing(source.id, targetTweetId, false)
            }
            return Result.SUCCESS
        } else {
            d { "fetch(${source.id}): count=${tweets.count()}, id=[${tweets.first().id}, ${tweets.last().id}]"}
        }

        Tweet.save(tweets, source)
        User.save(users)
        source.updateFetchAt()

        when (fetchType) {
            FetchType.NEW -> {
                val tweet = tweets.last()
                val hasMissing = !tweetsEmpty && tweet.id != maxTweetId
                if (hasMissing) {
                    Tweet.updateHasMissing(source.id, tweet.id, true)
                }
            }
            FetchType.MISSING -> {
                val tweet = tweets.last()
                val hasMissing = tweet.id != prevTweetId
                if (hasMissing) {
                    Tweet.updateHasMissing(source.id, tweet.id, true)
                }
            }
            FetchType.OLDER -> {} // do nothing
        }

        return Result.SUCCESS
    }

    private fun makePaging() = Paging().apply {
        count = Preference.fetchCount
        when (fetchType) {
            FetchType.NEW -> {
                if (!tweetsEmpty) { sinceId = maxTweetId - 1 }
            }
            FetchType.MISSING -> {
                sinceId = prevTweetId - 1
                maxId = targetTweetId + 1
            }
            FetchType.OLDER -> {
                maxId = minTweetId + 1
            }
        }
        d { "fetch(${source.id}): paging=$this" }
    }

    private fun makeQuery() = Query().apply {
        query = source.query
        count = Preference.fetchCount
        when (fetchType) {
            FetchType.NEW -> {
                if (!tweetsEmpty) { sinceId = maxTweetId - 1 }
            }
            FetchType.MISSING -> {
                sinceId = prevTweetId - 1
                maxId = targetTweetId + 1
            }
            FetchType.OLDER -> {
                maxId = minTweetId + 1
            }
        }
        d { "fetch(${source.id}): query=$this" }
    }
}
