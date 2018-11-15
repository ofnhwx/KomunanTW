package net.komunan.komunantw.service

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.Spanned
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.extension.enqueueSequentilly
import net.komunan.komunantw.repository.entity.Consumer
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.ext.TweetSourceExt
import net.komunan.komunantw.worker.FetchTweetsWorker
import net.komunan.komunantw.worker.GarbageCleaningWorker
import net.komunan.komunantw.worker.UpdateSourcesWorker
import twitter4j.*
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.util.*

object TwitterService {
    private val factory by lazy { TwitterFactory(ConfigurationBuilder().apply { setTweetModeExtended(true) }.build()) }

    fun twitter(consumer: Consumer): Twitter = factory.instance.apply {
        setOAuthConsumer(consumer.key, consumer.secret)
    }

    fun twitter(credential: Credential): Twitter = factory.instance.apply {
        setOAuthConsumer(credential.consumerKey, credential.consumerSecret)
        oAuthAccessToken = AccessToken(credential.token, credential.tokenSecret)
    }

    fun fetchTweets(isInteractive: Boolean = false): List<UUID> {
        val workManager = WorkManager.getInstance()
        val sourceIds = Timeline.sourceDao.findAll().map { it.sourceId }.distinct()
        val requests = sourceIds.map { FetchTweetsWorker.request(it, Tweet.INVALID_ID, isInteractive) }
        return workManager.enqueueSequentilly("TwitterService.FETCH_TWEETS_ALL", ExistingWorkPolicy.KEEP, requests)
    }

    fun fetchTweets(mark: TweetSourceExt, isInteractive: Boolean = false): List<UUID> {
        val workManager = WorkManager.getInstance()
        val requests = mark.sourceIds().map { FetchTweetsWorker.request(it, mark.tweetId, isInteractive) }
        return workManager.enqueueSequentilly("TwitterService.FETCH_TWEETS_MISSING", ExistingWorkPolicy.APPEND, requests)
    }

    fun garbageCleaning(): UUID {
        val workManager = WorkManager.getInstance()
        val request = GarbageCleaningWorker.request()
        workManager.enqueueUniqueWork("TwitterService.GARBAGE_CLEANING", ExistingWorkPolicy.KEEP, request)
        return request.id
    }

    fun updateSourceList(accountId: Long): UUID {
        val workManager = WorkManager.getInstance()
        val request = UpdateSourcesWorker.request(accountId)
        workManager.enqueue(request)
        return request.id
    }

    @Suppress("DEPRECATION")
    fun makeStatusPermalink(text: String?, screenName: String?, tweetId: Long?): Spanned? {
        if (text == null || screenName == null || tweetId == null) {
            return null
        }
        return Html.fromHtml("""<a href="https://twitter.com/%s/status/%s">%s</a>""".format(screenName, tweetId, text))
    }

    object Unofficial {
        /**
         * @throws TwitterException
         */
        fun doTweet(credential: Credential, text: String, files: List<File> = emptyList()): Status {
            val twitter = twitter(credential)
            val update = makeUpdate(twitter, text, files)
            return twitter.updateStatus(update)
        }

        /**
         * @throws TwitterException
         */
        fun doReply(credential: Credential, replyTo: Long, text: String, files: List<File> = emptyList()): Status {
            val twitter = twitter(credential)
            val reply = makeUpdate(twitter, text, files).inReplyToStatusId(replyTo)
            return twitter.updateStatus(reply)
        }

        /**
         * @throws TwitterException
         */
        fun doRetweet(credential: Credential, statusId: Long, cancel: Boolean = false): Status {
            val twitter = twitter(credential)
            return if (cancel) {
                // TODO: リツイート元のツイートからリツイートフラグを削除
                // TODO: リツイートを削除
                twitter.unRetweetStatus(statusId)
            } else {
                // TODO: リツイート元のツイートにリツイートフラグを追加
                twitter.retweetStatus(statusId)
            }
        }

        /**
         * @throws TwitterException
         */
        fun doLike(credential: Credential, statusId: Long, cancel: Boolean = false): Status {
            val twitter = twitter(credential)
            return if (cancel) {
                // TODO: お気に入り元のツイートからお気に入りフラグを削除
                // TODO: お気に入りしたツイートをお気に入りのソースから削除
                twitter.destroyFavorite(statusId)
            } else {
                // TODO: お気に入り元のツイートにお気に入りフラグを追加
                // TODO: お気に入りしたツイートをお気に入りのソースに追加(リロードで登録されるパターンがあるので必要に応じて)
                twitter.createFavorite(statusId)
            }
        }

        /**
         * @throws TwitterException
         */
        private fun makeUpdate(twitter: Twitter, text: String, files: List<File>): StatusUpdate {
            return StatusUpdate(text).apply {
                if (files.any()) {
                    val medias = files.map(twitter::uploadMedia)
                    val mediaIds = medias.map(UploadedMedia::getMediaId).toLongArray()
                    setMediaIds(*mediaIds)
                }
            }
        }
    }

    object Official {
        private const val SCHEME = "https"
        private const val AUTHORITY = "twitter.com"

        private const val INTENT_TWEET   = "intent/tweet"
        private const val INTENT_RETWEET = "intent/retweet"
        private const val INTENT_LIKE    = "intent/like"
        private const val INTENT_USER    = "intent/user"

        private const val PATH_HASH_TAG = "hashtag/%s"

        private const val PARAM_IN_REPLY_TO = "in_reply_to"
        private const val PARAM_TWEET_ID    = "tweet_id"
        private const val PARAM_USER_ID     = "user_id"
        private const val PARAM_SCREEN_NAME = "screen_name"

        fun doTweet(tweetId: Long? = null) {
            if (tweetId == null) {
                action { it.path(INTENT_TWEET) }
            } else {
                action { it.path(INTENT_TWEET).appendQueryParameter(PARAM_IN_REPLY_TO, tweetId.toString()) }
            }
        }

        fun doRetweet(tweetId: Long) {
            action { it.path(INTENT_RETWEET).appendQueryParameter(PARAM_TWEET_ID, tweetId.toString()) }
        }

        fun doLike(tweetId: Long) {
            action { it.path(INTENT_LIKE).appendQueryParameter(PARAM_TWEET_ID, tweetId.toString()) }
        }

        fun showProfile(userId: Long) {
            action { it.path(INTENT_USER).appendQueryParameter(PARAM_USER_ID, userId.toString()) }
        }

        fun showProfile(screenName: String) {
            action { it.path(INTENT_USER).appendQueryParameter(PARAM_SCREEN_NAME, screenName.trim('@')) }
        }

        fun showHashTag(hashTag: String) {
            action { it.path(PATH_HASH_TAG.format(hashTag.trim('#'))) }
        }

        private fun action(body: (Uri.Builder) -> Uri.Builder) {
            val builder = Uri.Builder().scheme(SCHEME).authority(AUTHORITY)
            TWContext.startActivity(Intent(Intent.ACTION_VIEW, body.invoke(builder).build()))
        }
    }
}
