package net.komunan.komunantw.worker

import android.content.Context
import androidx.work.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.Preference
import net.komunan.komunantw.extension.transaction
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.service.TwitterService
import twitter4j.*

class FetchTweetsWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    companion object {
        private const val PARAMETER_SOURCE_ID = "FetchTweetsWorker.PARAMETER_SOURCE_ID"
        private const val PARAMETER_IS_INTERACTIVE = "FetchTweetsWorker.PARAMETER_IS_INTERACTIVE"
        private const val PARAMETER_TARGET_TWEET_ID = "FetchTweetsWorker.PARAMETER_TARGET_TWEET_ID"

        @JvmStatic
        fun request(sourceId: Long, fetchTarget: Long, isInteractive: Boolean): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(FetchTweetsWorker::class.java)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
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

    private val source        by lazy { Source.dao.find(inputData.getLong(PARAMETER_SOURCE_ID, 0))!! }
    private val isInteractive by lazy { inputData.getBoolean(PARAMETER_IS_INTERACTIVE, false) }

    private val tweetsEmpty by lazy { Tweet.sourceDao.countBySourceId(source.id) == 0L }
    private val maxTweetId  by lazy { Tweet.sourceDao.maxIdBySourceId(source.id) }
    private val minTweetId  by lazy { Tweet.sourceDao.minIdBySourceId(source.id) }
    private val markTweetId by lazy { inputData.getLong(PARAMETER_TARGET_TWEET_ID, 0) }
    private val prevTweetId by lazy { Tweet.sourceDao.prevIdBySourceId(source.id, markTweetId) }

    private lateinit var fetchType: FetchType

    override fun doWork(): Result {
        when {
            // 自動更新で指定値以上の間隔が空いていない場合はスキップ
            !isInteractive && !source.requireAutoFetch() -> {
                d { "fetch(${source.id}): skip=[elapsed=${System.currentTimeMillis() - source.fetchAt}, required=${Preference.fetchIntervalMillis * 0.8}]" }
                return Result.SUCCESS
            }
            // 初回の取得
            tweetsEmpty -> {
                fetchType = FetchType.FIRST
                d { "fetch(${source.id}): first fetch." }
            }
            // 新規ツイートの取得
            markTweetId == Tweet.INVALID_ID -> {
                fetchType = FetchType.NEW
                d { "fetch(${source.id}): type=NEW, tweetId={ max=$maxTweetId, *min=$minTweetId }" }
            }
            // 古いツイートの取得
            prevTweetId == Tweet.INVALID_ID -> {
                fetchType = FetchType.OLD
                d { "fetch(${source.id}): type=OLD, tweetId={ max=$maxTweetId, *min=$minTweetId }" }
            }
            // 取得漏れの取得
            else -> {
                fetchType = FetchType.MISS
                d { "fetch(${source.id}): type=MISS, tweetId={ *target=$markTweetId, *prev=$prevTweetId }"}
            }
        }

        val statuses = fetchTweets()
        if (statuses.isEmpty()) {
            d { "fetch(${source.id}): count=0"}
        } else {
            d { "fetch(${source.id}): count=${statuses.count()}, id={ ${statuses.first().id} ～ ${statuses.last().id} }"}
        }

        transaction {
            // 未取得のマークを削除
//            if (markTweetId != Tweet.INVALID_ID) {
//                Tweet.removeMissingMark(source, markTweetId)
//            }

            // 各種データを保存
            Tweet.createCache(source, statuses)
            User.createCache(statuses)
            source.apply { fetchAt = System.currentTimeMillis() }.save()

            // 未取得のマークを設定
//            statuses.lastOrNull()?.let { status ->
//                when (fetchType) {
//                    FetchType.FIRST -> Tweet.addMissingMark(source, status.id - 1)
//                    FetchType.NEW -> {
//                        if (status.id > (maxTweetId + 1)) {
//                            Tweet.addMissingMark(source, status.id - 1)
//                        }
//                    }
//                    FetchType.OLD -> Tweet.addMissingMark(source, status.id - 1)
//                    FetchType.MISS -> {
//                        if (status.id > (prevTweetId + 1)) {
//                            Tweet.addMissingMark(source, status.id - 1)
//                        }
//                    }
//                }
//            }
        }

        return Result.SUCCESS
    }

    private fun fetchTweets(): List<Status> {
        val credential = Credential.dao.findByAccountId(source.accountId).first()
        val twitter = TwitterService.twitter(credential)
        return when (Source.Type.valueOf(source.type)) {
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
            FetchType.FIRST -> {
                // 初回取得のため指定不要
            }
            FetchType.NEW -> {
                // ${maxTweetId - 1}を渡すことで${maxTweetId}のツィートを取得し番兵として使用する
                sinceId = maxTweetId - 1
            }
            FetchType.OLD -> {
                // maxId  : ${markTweetId}は実際には取得できていないID(取得できているツィートのID-1)となっているため+1して使用する
                maxId = markTweetId + 1
            }
            FetchType.MISS -> {
                // maxId  : ${markTweetId}は実際には取得できていないID(取得できているツィートのID-1)となっているため+1して使用する
                // sinceId: ${prevTweetId - 1}を渡すことで${prevTweetId}のツィートを取得し番兵として使用する
                maxId = markTweetId + 1
                sinceId = prevTweetId - 1
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
            FetchType.FIRST -> {
                // 初回取得のため指定不要
            }
            FetchType.NEW -> {
                // ${maxTweetId - 1}を渡すことで${maxTweetId}のツィートを取得し番兵として使用する
                sinceId = maxTweetId - 1
            }
            FetchType.OLD -> {
                // maxId  : ${markTweetId}は実際には取得できていないID(取得できているツィートのID-1)となっているため+1して使用する
                maxId = markTweetId + 1
            }
            FetchType.MISS -> {
                // maxId  : ${markTweetId}は実際には取得できていないID(取得できているツィートのID-1)となっているため+1して使用する
                // sinceId: ${prevTweetId - 1}を渡すことで${prevTweetId}のツィートを取得し番兵として使用する
                maxId = markTweetId + 1
                sinceId = prevTweetId - 1
            }
        }
        d { "fetch(${source.id}): parameter=$this" }
    }
}
