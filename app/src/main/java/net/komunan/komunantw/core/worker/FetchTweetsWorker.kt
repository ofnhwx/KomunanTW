package net.komunan.komunantw.core.worker

import android.content.Context
import androidx.work.*
import com.github.ajalt.timberkt.i
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.commaSeparated
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.core.repository.entity.cache.Tweet
import net.komunan.komunantw.core.repository.entity.cache.User
import net.komunan.komunantw.core.service.TwitterService
import twitter4j.Paging as TwitterPaging
import twitter4j.Query as TwitterQuery
import twitter4j.QueryResult as TwitterQueryResult
import twitter4j.ResponseList as TwitterResponseList
import twitter4j.Status as TwitterStatus

class FetchTweetsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        private const val PARAMETER_SOURCE_ID = "FetchTweetsWorker.PARAMETER_SOURCE_ID"
        private const val PARAMETER_IS_INTERACTIVE = "FetchTweetsWorker.PARAMETER_IS_INTERACTIVE"
        private const val PARAMETER_TARGET_TWEET_ID = "FetchTweetsWorker.PARAMETER_TARGET_TWEET_ID"

        @JvmStatic
        fun request(sourceId: Long, fetchTarget: Long, isInteractive: Boolean): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(FetchTweetsWorker::class.java)
                    .setConstraints(Constraints.Builder().apply {
                        setRequiredNetworkType(NetworkType.CONNECTED)
                    }.build())
                    .setInputData(Data.Builder().apply {
                        putLong(PARAMETER_SOURCE_ID, sourceId)
                        putBoolean(PARAMETER_IS_INTERACTIVE, isInteractive)
                        putLong(PARAMETER_TARGET_TWEET_ID, fetchTarget)
                    }.build()).build()
        }
    }

    private enum class FetchType {
        FIRST,
        NEW,
        OLD,
        MISS,
    }

    private val sourceId by lazy { inputData.getLong(PARAMETER_SOURCE_ID, 0L) }
    private val source by lazy { Source.box.get(sourceId) }
    private val isInteractive by lazy { inputData.getBoolean(PARAMETER_IS_INTERACTIVE, false) }
    private val maxTweetId by lazy { source.newestTweetId() }
    private val minTweetId by lazy { source.oldestTweetId() }
    private val markTweetId by lazy { inputData.getLong(PARAMETER_TARGET_TWEET_ID, 0L) }
    private val prevTweetId by lazy { source.previousTweetId(markTweetId) }

    private lateinit var fetchType: FetchType

    override fun doWork(): Result {
        when {
            sourceId == 0L || source == null -> {
                w { "fetch($sourceId): invalid source id." }
                return Result.failure()
            }
            // 自動更新で指定値以上の間隔が空いていない場合はスキップ
            !isInteractive && !source.requireAutoFetch -> {
                val elapsed = (System.currentTimeMillis() - source.fetchedAt).commaSeparated()
                val required = (Preference.fetchIntervalMillis * Preference.fetchIntervalThreshold).toLong().commaSeparated()
                i { "fetch($sourceId): skip={ elapsed=$elapsed, required=$required }" }
                return Result.success()
            }
            // 初回の取得
            source.tweetCount() == 0L -> {
                fetchType = FetchType.FIRST
                i { "fetch($sourceId): type=FIRST" }
            }
            // 新規ツイートの取得
            markTweetId == Tweet.INVALID_ID -> {
                fetchType = FetchType.NEW
                i { "fetch($sourceId): type=NEW, tweetId={ since(${maxTweetId.commaSeparated()}) < target }" }
            }
            // 古いツイートの取得
            prevTweetId == Tweet.INVALID_ID -> {
                fetchType = FetchType.OLD
                i { "fetch($sourceId): type=OLD, tweetId={ target <= max(${minTweetId.commaSeparated()}) }" }
            }
            // 取得漏れの取得
            else -> {
                fetchType = FetchType.MISS
                i { "fetch($sourceId): type=MISS, tweetId={ since(${prevTweetId.commaSeparated()}) < target <= max(${markTweetId.commaSeparated()}) }" }
            }
        }

        val statuses = fetchTweets()
        if (statuses.isEmpty()) {
            i { "fetch(${source.id}): count=0" }
        } else {
            i { "fetch(${source.id}): count=${statuses.count()}, id={ ${statuses.last().id.commaSeparated()} ～ ${statuses.first().id.commaSeparated()} }" }
        }

        ObjectBox.get().runInTx {
            // 未取得のマークを削除
            if (markTweetId != Tweet.INVALID_ID) {
                source.delMissing(markTweetId)
            }

            // 各種データを保存
            Tweet.createCache(source, statuses)
            User.createCache(statuses)
            if (fetchType == FetchType.FIRST || fetchType == FetchType.NEW) {
                source.apply { fetchedAt = System.currentTimeMillis() }.save()
            }

            // 未取得のマークを設定
            statuses.lastOrNull()?.let { status ->
                when (fetchType) {
                    FetchType.FIRST -> source.addMissing(status.id)
                    FetchType.OLD -> source.addMissing(status.id)
                    FetchType.NEW -> if (status.id > maxTweetId) source.addMissing(status.id)
                    FetchType.MISS -> if (status.id > prevTweetId) source.addMissing(status.id)
                }
            }
        }

        return Result.success()
    }

    private fun fetchTweets(): List<TwitterStatus> {
        val credential = source.account.target.credentials.first()
        val twitter = TwitterService.twitter(credential)
        return when (source.type) {
            Source.Type.HOME -> {
                checkResult(twitter.getHomeTimeline(makePaging()))
            }
            Source.Type.MENTION -> {
                checkResult(twitter.getMentionsTimeline(makePaging()))
            }
            Source.Type.USER -> {
                checkResult(twitter.getUserTimeline(makePaging()))
            }
            Source.Type.LIKE -> {
                checkResult(twitter.getFavorites(makePaging()))
            }
            Source.Type.LIST -> {
                checkResult(twitter.getUserListStatuses(source.listId, makePaging()))
            }
            Source.Type.SEARCH -> {
                checkResult(twitter.search(makeQuery()))
            }
        }
    }

    private fun checkResult(result: TwitterResponseList<TwitterStatus>): List<TwitterStatus> {
        return result
    }

    private fun checkResult(result: TwitterQueryResult): List<TwitterStatus> {
        return result.tweets
    }

    private fun makePaging() = TwitterPaging().apply {
        count = Preference.fetchCount
        makeRange().also { (sinceId, maxId) ->
            if (sinceId != null) {
                this.sinceId = sinceId
            }
            if (maxId != null) {
                this.maxId = maxId
            }
        }
        i { "fetch(${source.id}): parameter=$this" }
    }

    private fun makeQuery() = TwitterQuery().apply {
        query = source.query
        count = Preference.fetchCount
        makeRange().also { (sinceId, maxId) ->
            if (sinceId != null) {
                this.sinceId = sinceId
            }
            if (maxId != null) {
                this.maxId = maxId
            }
        }
        i { "fetch(${source.id}): parameter=$this" }
    }

    /**
     * @return sinceId, maxId
     */
    private fun makeRange(): Pair<Long?, Long?> {
        return when (fetchType) {
            // 初回取得のため指定不要
            FetchType.FIRST -> Pair(null, null)
            // since: ${maxTweetId - 1} を渡すことで ${maxTweetId} のツイートを取得し番兵として使用する
            FetchType.NEW -> Pair(maxTweetId - 1, null)
            // max  : ${markTweetId} より小さい値を取得するため -1 して使用する
            FetchType.OLD -> Pair(null, markTweetId - 1)
            // since: ${prevTweetId - 1} を渡すことで ${prevTweetId} のツィートを取得し番兵として使用する
            // max  : ${markTweetId} より小さい値を取得するため-1して使用する
            FetchType.MISS -> Pair(prevTweetId - 1, markTweetId - 1)
        }
    }
}
