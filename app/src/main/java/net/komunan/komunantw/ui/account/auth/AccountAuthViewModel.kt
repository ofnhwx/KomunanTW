package net.komunan.komunantw.ui.account.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.common.Preference
import net.komunan.komunantw.common.combineLatest
import net.komunan.komunantw.common.openUrl
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.core.repository.entity.Account
import net.komunan.komunantw.core.repository.entity.Consumer
import net.komunan.komunantw.core.repository.entity.Credential
import net.komunan.komunantw.core.service.TwitterService
import net.komunan.komunantw.ui.common.base.TWBaseViewModel
import twitter4j.TwitterException

class AccountAuthViewModel : TWBaseViewModel() {
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

    suspend fun startOAuth(consumerKeySecret: Consumer): Unit = process {
        val requestToken = TwitterService.twitter(consumerKeySecret).oAuthRequestToken
        Preference.requestToken = requestToken
        Preference.consumer = consumerKeySecret
        tokenValid.postValue(true)
        requestToken.authorizationURL.openUrl()
    }

    suspend fun finishOAuth(): Boolean = process {
        val consumer = Preference.consumer
        val requestToken = Preference.requestToken
        if (consumer == null || requestToken == null) {
            w { "ConsumerKeySecret or RequestToken is null: ConsumerKeySecret=$consumer, RequestToken=$requestToken" }
            return@process false
        }

        val twitter = TwitterService.twitter(consumer)
        try {
            val accessToken = twitter.getOAuthAccessToken(requestToken, pin.value)
            ObjectBox.get().runInTx {
                val account = Account(twitter.showUser(accessToken.userId)).save()
                Credential(account, consumer, accessToken).save()
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
