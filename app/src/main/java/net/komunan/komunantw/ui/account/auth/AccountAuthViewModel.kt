package net.komunan.komunantw.ui.account.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.extension.combineLatest
import net.komunan.komunantw.common.extension.intentActionView
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.ui.common.base.TWBaseViewModel
import net.komunan.komunantw.common.extension.transaction
import twitter4j.TwitterException

class AccountAuthViewModel: TWBaseViewModel() {
    private val tokenValid = MutableLiveData<Boolean>()

    val openBrowserEnabled: LiveData<Boolean> = isIdle

    val pin = MutableLiveData<String>()
    val pinEnabled: LiveData<Boolean> = tokenValid
    private val pinValid: LiveData<Boolean> = Transformations.map(pin) { it.length == 7 }

    val authenticationEnabled: LiveData<Boolean> = combineLatest(isIdle, pinValid) { isIdle, pinValid ->
        (isIdle ?: false) && (pinValid ?: false)
    }

    init {
        tokenValid.postValue(Preference.requestToken != null && Preference.consumer != null)
    }

    suspend fun startOAuth(context: Context, consumerKeySecret: Consumer): Unit = process {
        val requestToken = TwitterService.twitter(consumerKeySecret).oAuthRequestToken
        Preference.requestToken = requestToken
        Preference.consumer = consumerKeySecret
        tokenValid.postValue(true)
        requestToken.authorizationURL.intentActionView()
    }

    suspend fun finishOAuth(): Boolean = process {
        val consumerKeySecret = Preference.consumer
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
                Credential.dao.findByAccountId(account.id).forEach { it.delete() }
                Credential(account, consumerKeySecret, accessToken).save()
                TwitterService.updateSourceList(account.id)
            }
            Preference.consumer = null
            Preference.requestToken = null
            tokenValid.postValue(false)
            return@process true
        } catch (e: TwitterException) {
            w(e)
            return@process false
        }
    }
}
