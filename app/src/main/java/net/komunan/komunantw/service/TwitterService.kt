package net.komunan.komunantw.service

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.Spanned
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.repository.entity.ext.TweetSourceExt
import net.komunan.komunantw.worker.FetchTweetsWorker
import net.komunan.komunantw.worker.UpdateSourcesWorker
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder
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
        val sourceIds = Timeline.sourceDao.findAll().map { it.sourceId }.distinct()
        val requests = sourceIds.map { FetchTweetsWorker.request(it, Tweet.INVALID_ID, isInteractive) }
        return fetchTweetsInternal("TwitterService.FETCH_TWEETS_ALL", ExistingWorkPolicy.KEEP, requests)
    }

    fun fetchTweets(mark: TweetSourceExt, isInteractive: Boolean = false): List<UUID> {
        val requests = mark.sourceIds().map { FetchTweetsWorker.request(it, mark.tweetId, isInteractive) }
        return fetchTweetsInternal("TwitterService.FETCH_TWEETS_MISSING", ExistingWorkPolicy.APPEND, requests)
    }

    private fun fetchTweetsInternal(name: String, policy: ExistingWorkPolicy, requests: List<OneTimeWorkRequest>): List<UUID> {
        return if (requests.any()) {
            var continuous = WorkManager.getInstance().beginUniqueWork(name, policy, requests.first())
            for (request in requests.drop(1)) {
                continuous = continuous.then(request)
            }
            continuous.enqueue()
            requests.map(WorkRequest::getId)
        } else {
            emptyList()
        }
    }

    fun updateSourceList(accountId: Long) {
        val request = UpdateSourcesWorker.request(accountId)
        WorkManager.getInstance().enqueue(request)
    }

    @Suppress("DEPRECATION")
    fun makeStatusPermalink(text: String?, screenName: String?, tweetId: Long?): Spanned? {
        if (text == null || screenName == null || tweetId == null) {
            return null
        }
        return Html.fromHtml("""<a href="https://twitter.com/%s/status/%s">%s</a>""".format(screenName, tweetId, text))
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
