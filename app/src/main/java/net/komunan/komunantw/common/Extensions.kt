package net.komunan.komunantw.common

import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import net.komunan.komunantw.repository.entity.Credential
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.RequestToken

fun Int.string(): String = ReleaseApplication.context.getString(this)

fun twitter(): Twitter = TwitterFactory.getSingleton()

fun twitter(consumerKeySecret: ConsumerKeySecret): Twitter = TwitterFactory.getSingleton().also {
    it.setOAuthConsumer(consumerKeySecret.consumerKey, consumerKeySecret.consumerSecret)
}

fun twitter(credential: Credential): Twitter = TwitterFactory.getSingleton().also {
    it.setOAuthConsumer(credential.consumerKey, credential.consumerSecret)
    it.oAuthAccessToken = credential.accessToken
}
