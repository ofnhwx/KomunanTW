package net.komunan.komunantw.ui.auth

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.Intent
import android.net.Uri
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.Preference
import net.komunan.komunantw.combineLatest
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.ui.common.TWBaseViewModel
import twitter4j.TwitterException

class AuthViewModel: TWBaseViewModel() {
    private val tokenValid = MutableLiveData<Boolean>()

    val openBrowserEnabled: LiveData<Boolean> = isIdle

    val pin = MutableLiveData<String>()
    val pinEnabled: LiveData<Boolean> = tokenValid
    private val pinValid: LiveData<Boolean> = Transformations.map(pin) { it.length == 7 }

    val authenticationEnabled: LiveData<Boolean> = combineLatest(isIdle, pinValid) { isIdle, pinValid -> (isIdle ?: false) && (pinValid ?: false) }

    init {
        tokenValid.postValue(Preference.requestToken != null && Preference.consumerKeySecret != null)
    }

    suspend fun startOAuth(consumerKeySecret: ConsumerKeySecret): Unit = process {
        val requestToken = TwitterService.twitter(consumerKeySecret).oAuthRequestToken
        Preference.requestToken = requestToken
        Preference.consumerKeySecret = consumerKeySecret
        tokenValid.postValue(true)
        application.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.authorizationURL)))
    }

    suspend fun finishOAuth(): Boolean = process {
        val consumerKeySecret = Preference.consumerKeySecret
        val requestToken = Preference.requestToken
        if (consumerKeySecret == null || requestToken == null) {
            w { "ConsumerKeySecret or RequestToken is null: ConsumerKeySecret=$consumerKeySecret, RequestToken=$requestToken" }
            return@process false
        }

        val twitter = TwitterService.twitter(consumerKeySecret)
        try {
            val accessToken = twitter.getOAuthAccessToken(requestToken, pin.value)
            transaction {
                val account = Account(twitter.showUser(accessToken.userId)).save()
                Credential.findByAccountId(account.id).forEach { it.delete() }
                Credential(account, consumerKeySecret, accessToken).save()
                Source.update(account)
                Timeline.firstSetup(account)
            }
            Preference.consumerKeySecret = null
            Preference.requestToken = null
            tokenValid.postValue(false)
            return@process true
        } catch (e: TwitterException) {
            w(e)
            return@process false
        }
    }
}
