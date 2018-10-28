package net.komunan.komunantw.service

import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import net.komunan.komunantw.repository.entity.Credential
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
}
