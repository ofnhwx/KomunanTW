package net.komunan.komunantw

import com.google.gson.Gson
import com.marcinmoskala.kotlinpreferences.PreferenceHolder
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import twitter4j.auth.RequestToken

object Preference: PreferenceHolder() {
    private val gson = Gson()

    var useInMemoryDatabase: Boolean by bindToPreferenceField(false)
    var fetchCount: Int by bindToPreferenceField(200)
    var fetchInterval: Long by bindToPreferenceField(120)

    var fetchIntervalMillis: Long
        get() = fetchInterval * 1000
        set(value) {
            fetchInterval = value / 1000
        }

    private var _requestToken: String? by bindToPreferenceFieldNullable()
    private data class RequestTokenHolder(val token: String, val tokenSecret: String) {
        companion object {
            fun from(requestToken: RequestToken) = RequestTokenHolder(requestToken.token, requestToken.tokenSecret)
        }
        fun to() = RequestToken(token, tokenSecret)
    }
    var requestToken: RequestToken?
        get() = _requestToken?.let { gson.fromJson(it, RequestTokenHolder::class.java).to() }
        set(value) {
            _requestToken = value?.let { gson.toJson(RequestTokenHolder.from(it)).toString() }
        }

    private var _consumerKeySecret: String? by bindToPreferenceFieldNullable()
    var consumerKeySecret: ConsumerKeySecret?
        get() = _consumerKeySecret?.let { gson.fromJson(it, ConsumerKeySecret::class.java) }
        set(value) {
            _consumerKeySecret = value?.let { gson.toJson(value).toString() }
        }
}
