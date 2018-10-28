package net.komunan.komunantw.ui.auth

import android.content.Intent
import android.net.Uri
import net.komunan.komunantw.Preference
import net.komunan.komunantw.common.BaseViewModel
import net.komunan.komunantw.event.Transition
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.service.TwitterService.twitter

internal class AuthViewModel: BaseViewModel() {
    fun consumerKeys() = ConsumerKeySecret.findAllAsync()

    suspend fun startOAuth(consumerKeySecret: ConsumerKeySecret) = process {
        val requestToken = twitter(consumerKeySecret).oAuthRequestToken
        Preference.requestToken = requestToken
        Preference.consumerKeySecret = consumerKeySecret
        application.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.authorizationURL)))
    }

    suspend fun finishOAuth(pin: String) = process {
        val consumerKeySecret = Preference.consumerKeySecret!!
        val requestToken = Preference.requestToken!!
        val twitter = twitter(consumerKeySecret)
        val accessToken = twitter.getOAuthAccessToken(requestToken, pin)

        transaction {
            val account = Account(twitter.showUser(accessToken.userId)).save()
            Credential(account, consumerKeySecret, accessToken).save()
            Source.update(account)
            Timeline.firstSetup(account)
        }

        Preference.consumerKeySecret = null
        Preference.requestToken = null

        Transition.execute(Transition.Target.BACK)
    }
}
