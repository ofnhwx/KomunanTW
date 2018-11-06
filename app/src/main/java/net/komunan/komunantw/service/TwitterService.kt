package net.komunan.komunantw.service

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.Spanned
import androidx.work.WorkManager
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.worker.FetchTweetsWorker
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder

object TwitterService {
    private val factory by lazy { TwitterFactory(ConfigurationBuilder().apply { setTweetModeExtended(true) }.build()) }

    fun twitter(consumerKeySecret: ConsumerKeySecret): Twitter = factory.instance.apply {
        setOAuthConsumer(consumerKeySecret.consumerKey, consumerKeySecret.consumerSecret)
    }

    fun twitter(credential: Credential): Twitter = factory.instance.apply {
        setOAuthConsumer(credential.consumerKey, credential.consumerSecret)
        oAuthAccessToken = AccessToken(credential.token, credential.tokenSecret)
    }

    fun fetchTweets() {
        val requests = Source.findEnabled().map { FetchTweetsWorker.request(it.id, FetchTweetsWorker.FetchType.NEW) }
        if (requests.any()) {
            WorkManager.getInstance().enqueue(requests)
        }
    }

    fun doOfficialTweet(tweetId: Long? = null) {
        if (tweetId == null) {
            doOfficialAction(buildTwitterUri { it.path("intent/tweet") })
        } else {
            doOfficialAction(buildTwitterUri { it.path("intent/tweet").appendQueryParameter("tweet_id", tweetId.toString()) })
        }
    }

    fun doOfficialRetweet(tweetId: Long) {
        doOfficialAction(buildTwitterUri { it.path("intent/retweet").appendQueryParameter("tweet_id", tweetId.toString()) })
    }

    fun doOfficialLike(tweetId: Long) {
        doOfficialAction(buildTwitterUri { it.path("intent/like").appendQueryParameter("tweet_id", tweetId.toString()) })
    }

    fun doOfficialProfile(userId: Long) {
        doOfficialAction(buildTwitterUri { it.path("intent/user").appendQueryParameter("user_id", userId.toString()) })
    }

    fun buildTwitterUri(body: (Uri.Builder) -> Uri.Builder): Uri {
        val builder = Uri.Builder().scheme("https").authority("twitter.com")
        return body.invoke(builder).build()
    }

    private fun doOfficialAction(uri: Uri) {
        ReleaseApplication.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    @Suppress("DEPRECATION")
    fun makeStatusPermalink(text: String?, screenName: String?, tweetId: Long?): Spanned? {
        if (text == null || screenName == null || tweetId == null) {
            return null
        }
        return Html.fromHtml("""<a href="https://twitter.com/%s/status/%s">%s</a>""".format(screenName, tweetId, text))
    }
}
