package net.komunan.komunantw.worker

import android.content.Context
import androidx.work.*
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.Preference
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.service.TwitterService
import twitter4j.*

class FetchTweetsWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    companion object {
        private const val PARAMETER_SOURCE_ID = "FetchTweetsWorker.PARAMETER_SOURCE_ID"
        private const val PARAMETER_FETCH_TYPE = "FetchTweetsWorker.PARAMETER_FETCH_TYPE"
        private const val PARAMETER_IS_INTERACTIVE = "FetchTweetsWorker.PARAMETER_IS_INTERACTIVE"
        private const val PARAMETER_TARGET_TWEET_ID = "FetchTweetsWorker.PARAMETER_TARGET_TWEET_ID"

        @JvmStatic
        fun request(sourceId: Long, fetchType: FetchType = FetchType.NEW, isInteractive: Boolean = false, targetTweetId: Long = 0): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(FetchTweetsWorker::class.java)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
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

    private val source        by lazy { Source.find(inputData.getLong(PARAMETER_SOURCE_ID, 0))!! }
    private val fetchType     by lazy { FetchType.valueOf(inputData.getString(PARAMETER_FETCH_TYPE)!!) }
    private val isInteractive by lazy { inputData.getBoolean(PARAMETER_IS_INTERACTIVE, false) }

    private val tweetsEmpty by lazy { source.tweetCount() == 0L }
    private val maxTweetId  by lazy { source.maxTweetId() }
    private val minTweetId  by lazy { source.minTweetId() }
    private val missTweetId by lazy { inputData.getLong(PARAMETER_TARGET_TWEET_ID, 0) }
    private val prevTweetId by lazy { source.prevTweetId(missTweetId) }

    override fun doWork(): Result {
        when {
            // 空の場合は<code>FetchType.New</code>のみ指定可能
            tweetsEmpty && fetchType != FetchType.NEW -> {
                w { "fetch(${source.id}): If the tweet associated with the source is empty, only FetchType.NEW is allowed."}
                return Result.FAILURE
            }
            // 自動更新で指定値以上の間隔が空いていない場合はスキップ
            !isInteractive && !source.requireAutoFetch() -> {
                d { "fetch(${source.id}): skip=[elapsed=${System.currentTimeMillis() - source.fetchAt}, required=${Preference.fetchIntervalMillis * 0.8}]" }
                return Result.SUCCESS
            }
            // 初回の取得
            tweetsEmpty -> {
                d { "fetch(${source.id}): first fetch." }
            }
            // 取得漏れ部分の取得
            fetchType == FetchType.MISSING -> {
                d { "fetch(${source.id}): type=$fetchType, tweetId={ target=$missTweetId, prev=$prevTweetId }"}
            }
            // その他(通常の取得)
            else -> {
                d { "fetch(${source.id}): type=$fetchType, tweetId={ max=$maxTweetId, min=$minTweetId }" }
            }
        }

        val (tweets, users) = fetchTweets()
        if (tweets.isEmpty()) {
            d { "fetch(${source.id}): count=0"}
        } else {
            d { "fetch(${source.id}): count=${tweets.count()}, id={ ${tweets.first().id} - ${tweets.last().id} }"}
        }

        transaction {
            if (fetchType == FetchType.MISSING && tweets.isEmpty()) {
                Tweet.removeMissingMark(source, missTweetId)
            }

            Tweet.save(source, tweets)
            User.save(users)
            source.updateFetchAt()

            when (fetchType) {
                FetchType.NEW     -> addMissingMark(tweets.lastOrNull(), maxTweetId)
                FetchType.MISSING -> addMissingMark(tweets.lastOrNull(), prevTweetId)
                FetchType.OLDER   -> {} // do nothing
            }
        }

        return Result.SUCCESS
    }

    private fun addMissingMark(tweet: Tweet?, target: Long) {
        if (tweet == null || tweet.id == target) {
            return
        }
        Tweet.addMissingMark(source, tweet.id - 1)
    }

    private fun fetchTweets(): Pair<List<Tweet>, List<User>> {
        val twitter = TwitterService.twitter(source.account()!!.credential())
        val result = when (Source.SourceType.valueOf(source.type)) {
            Source.SourceType.HOME -> {
                checkResult(twitter.getHomeTimeline(makePaging()))
            }
            Source.SourceType.MENTION -> {
                checkResult(twitter.getMentionsTimeline(makePaging()))
            }
            Source.SourceType.RETWEET -> {
                checkResult(twitter.getRetweetsOfMe(makePaging()))
            }
            Source.SourceType.USER -> {
                checkResult(twitter.getUserTimeline(makePaging()))
            }
            Source.SourceType.LIST -> {
                checkResult(twitter.getUserListStatuses(source.listId, makePaging()))
            }
            Source.SourceType.SEARCH -> {
                checkResult(twitter.search(makeQuery()))
            }
        }

        val tweets = result.map { Tweet(it) }
        val users = mutableListOf<User>()
        users.addAll(result.map { User(it.user) }.distinctBy { it.id })
        users.addAll(result.mapNotNull { it.retweetedStatus }.map { User(it.user) }.distinctBy { it.id })
        return Pair(tweets, users)
    }

    private fun checkResult(result: ResponseList<Status>): List<Status> {
        return result
    }

    private fun checkResult(result: QueryResult): List<Status> {
        return result.tweets
    }

    private fun makePaging() = Paging().apply {
        // count  : 取得する件数
        // maxId  : この値より小さいIDのツィートを取得
        // sinceId: この値より大きいIDのツィートを取得
        count = Preference.fetchCount
        when (fetchType) {
            FetchType.NEW -> {
                if (tweetsEmpty) {
                    // 初回取得のため指定不要
                } else {
                    // ${maxTweetId - 1}を渡すことで${maxTweetId}のツィートを取得し番兵として使用する
                    sinceId = maxTweetId - 1
                }
            }
            FetchType.MISSING -> {
                // maxId  : ${missTweetId}は実際には取得できていないID(取得できているツィートのID-1)となっているため+1して使用する
                // sinceId: ${prevTweetId - 1}を渡すことで${prevTweetId}のツィートを取得し番兵として使用する
                maxId = missTweetId + 1
                sinceId = prevTweetId - 1
            }
            FetchType.OLDER -> {
                // 古いツィートはそのまま取得できればよし
                maxId = minTweetId
            }
        }
        d { "fetch(${source.id}): parameter=$this" }
    }

    private fun makeQuery() = Query().apply {
        // count  : 取得する件数
        // maxId  : この値より小さいIDのツィートを取得
        // sinceId: この値より大きいIDのツィートを取得
        query = source.query
        count = Preference.fetchCount
        when (fetchType) {
            FetchType.NEW -> {
                if (tweetsEmpty) {
                    // 初回取得のため指定不要
                } else {
                    // ${maxTweetId - 1}を渡すことで${maxTweetId}のツィートを取得し番兵として使用する
                    sinceId = maxTweetId - 1
                }
            }
            FetchType.MISSING -> {
                // maxId  : ${missTweetId}は実際には取得できていないID(取得できているツィートのID-1)となっているため+1して使用する
                // sinceId: ${prevTweetId - 1}を渡すことで${prevTweetId}のツィートを取得し番兵として使用する
                maxId = missTweetId + 1
                sinceId = prevTweetId - 1
            }
            FetchType.OLDER -> {
                // 古いツィートはそのまま取得できればよし
                maxId = minTweetId
            }
        }
        d { "fetch(${source.id}): parameter=$this" }
    }
}
