package net.komunan.komunantw.ui.auth

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.experimental.async
import net.komunan.komunantw.Preference
import net.komunan.komunantw.common.twitter
import net.komunan.komunantw.repository.database.TWDatabase
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import net.komunan.komunantw.repository.entity.Credential

class AuthViewModel(app: Application): AndroidViewModel(app) {
    val consumerKeys: LiveData<List<ConsumerKeySecret>>
        get() = TWDatabase.instance.consumerKeySecretDao().findAll()

    fun startOAuth(consumerKeySecret: ConsumerKeySecret) = async {
        twitter(consumerKeySecret).oAuthRequestToken.let {
            Preference.consumerKeySecret = consumerKeySecret
            Preference.requestToken = it
            getApplication<Application>().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.authorizationURL)))
        }
    }

    fun finishOAuth(pin: String) = async {
        twitter().getOAuthAccessToken(Preference.requestToken, pin).let { accessToken ->
            val consumerKeySecret = Preference.consumerKeySecret!!
            Account().apply {
                id = accessToken.userId
                screenName = accessToken.screenName
            }.save()
            Credential().apply {
                accountId = accessToken.userId
                consumerKey = consumerKeySecret.consumerKey
                consumerSecret = consumerKeySecret.consumerSecret
                token = accessToken.token
                tokenSecret = accessToken.tokenSecret
            }.save()
            Preference.consumerKeySecret = null
            Preference.requestToken = null
        }
    }
}
